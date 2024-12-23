package nl.bertriksikken.lorawan;

import java.nio.charset.StandardCharsets;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import nl.bertriksikken.lorawan.LoraWanUplinkMessage.ILoraWanUplink;

/**
 * Listener process for receiving data from an MQTT server.
 */
public final class MqttListener {

    private static final Logger LOG = LoggerFactory.getLogger(MqttListener.class);
    private static final long DISCONNECT_TIMEOUT_MS = 3000;

    private final IMessageReceived callback;
    private final Class<? extends ILoraWanUplink> clazz;

    private final MqttClient mqttClient;
    private final MqttConnectOptions options;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Constructor.
     * 
     * @param callback the listener for a received message.
     * @param config the MQTT configuration
     * @param clazz the JSON class sent over MQTT
     */
    public MqttListener(IMessageReceived callback, MqttConfig config, Class<? extends ILoraWanUplink> clazz) {
        LOG.info("Creating client for MQTT server '{}' for app '{}'", config.getUrl(), config.getUser());
        try {
            this.mqttClient = new MqttClient(config.getUrl(), MqttClient.generateClientId(), new MemoryPersistence());
        } catch (MqttException e) {
            throw new IllegalArgumentException(e);
        }
        this.callback = callback;
        this.clazz = clazz;
        
        mqttClient.setCallback(new MqttCallbackHandler(mqttClient, config.getTopic(), this::handleMessage));

        // create connect options
        options = new MqttConnectOptions();
        options.setUserName(config.getUser());
        options.setPassword(config.getPass().toCharArray());
        options.setAutomaticReconnect(true);
    }

    // notify our caller in a thread safe manner
    private void handleMessage(String topic, String payload) {
        try {
            ILoraWanUplink uplink = objectMapper.readValue(payload, clazz);
            LoraWanUplinkMessage uplinkMessage = uplink.toLoraWanUplinkMessage();
            callback.messageReceived(uplinkMessage);
        } catch (JsonProcessingException e) {
            LOG.warn("Caught {}", e.getMessage());
        } catch (Throwable e) {
            // safety net
            LOG.error("Caught unhandled throwable", e);
        }
    }

    /**
     * Starts this module.
     * 
     * @throws MqttException in case something went wrong with MQTT
     */
    public void start() throws MqttException {
        LOG.info("Starting MQTT listener {}", mqttClient.getServerURI());
        mqttClient.connect(options);
    }

    public void stop() {
        LOG.info("Stopping MQTT listener");
        try {
            mqttClient.disconnect(DISCONNECT_TIMEOUT_MS);
        } catch (MqttException e) {
            // don't care, just log
            LOG.warn("Caught exception on disconnect: {}", e.getMessage());
        }
    }

    /**
     * MQTT callback handler, (re-)subscribes to the topic and forwards incoming
     * messages.
     */
    private static final class MqttCallbackHandler implements MqttCallbackExtended {

        private final MqttClient client;
        private final String topic;
        private final IMqttMessageArrived listener;

        private MqttCallbackHandler(MqttClient client, String topic, IMqttMessageArrived listener) {
            this.client = client;
            this.topic = topic;
            this.listener = listener;
        }

        @Override
        public void connectionLost(Throwable cause) {
            LOG.warn("Connection lost: {}", cause.getMessage());
        }

        @Override
        public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
            LOG.info("Message arrived on topic '{}'", topic);

            // notify our listener, in an exception safe manner
            try {
                String json = new String(mqttMessage.getPayload(), StandardCharsets.US_ASCII);
                listener.messageArrived(topic, json);
            } catch (Exception e) {
                LOG.trace("Caught exception", e);
                LOG.error("Caught exception in MQTT listener: {}", e.getMessage());
            }
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken token) {
            // nothing to do
        }

        @Override
        public void connectComplete(boolean reconnect, String serverURI) {
            LOG.info("Connected to '{}', subscribing to MQTT topic '{}'", serverURI, topic);
            try {
                client.subscribe(topic);
            } catch (MqttException e) {
                LOG.error("Caught exception while subscribing!", e);
            }
        }
    }

    interface IMqttMessageArrived {
        void messageArrived(String topic, String json);
    }

    /**
     * Interface of the callback from the TTN listener.
     */
    public interface IMessageReceived {

        /**
         * Indicates that a message was received.
         * @param message the message
         */
        void messageReceived(LoraWanUplinkMessage message);
        
    }
    
}
