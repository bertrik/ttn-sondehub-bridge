package nl.bertriksikken.bridge;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.PropertyConfigurator;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import nl.bertriksikken.lorawan.LoraWanUplinkMessage;
import nl.bertriksikken.lorawan.MqttListener;
import nl.bertriksikken.lorawan.Ttnv3UplinkMessage;

public final class Application {

    private static final Logger LOG = LoggerFactory.getLogger(Application.class);
    private static final String CONFIG_FILE = "configuration.yaml";

    private final ApplicationConfig config;
    private final List<MqttListener> mqttListeners = new ArrayList<>();

    private Application(ApplicationConfig config) {
        this.config = config;
    }

    /**
     * Main application entry point.
     * 
     * @param arguments application arguments (none taken)
     * @throws IOException   in case of a problem reading a config file
     * @throws MqttException in case of a problem starting MQTT client
     */
    public static void main(String[] arguments) throws IOException, MqttException {
        PropertyConfigurator.configure("log4j.properties");

        ApplicationConfig config = readConfig(new File(CONFIG_FILE));
        Application app = new Application(config);

        Thread.setDefaultUncaughtExceptionHandler(app::handleUncaughtException);

        app.start();
        Runtime.getRuntime().addShutdownHook(new Thread(app::stop));
    }

    private static ApplicationConfig readConfig(File file) throws IOException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        ApplicationConfig config = new ApplicationConfig();
        if (file.exists()) {
            try (FileInputStream fis = new FileInputStream(file)) {
                config = mapper.readValue(fis, ApplicationConfig.class);
            } catch (IOException e) {
                LOG.warn("Failed to load config {}, using defaults", file.getAbsoluteFile());
            }
        } else {
            LOG.info("Writing default configuration {}", file.getAbsoluteFile());
            mapper.writeValue(file, config);
        }
        return config;
    }

    /**
     * Handles uncaught exceptions: log it and stop the application.
     * 
     * @param t the thread
     * @param e the exception
     */
    private void handleUncaughtException(Thread t, Throwable e) {
        LOG.error("Caught unhandled exception, application will be stopped ...", e);
        stop();
    }

    /**
     * Starts the application.
     * 
     * @throws MqttException in case of a problem starting MQTT client
     */
    private void start() throws MqttException {
        LOG.info("Starting TTN-sondehub bridge application");

        MqttListener ttnListener = new MqttListener(this::messageReceived, config.ttnConfig, Ttnv3UplinkMessage.class);
        mqttListeners.add(ttnListener);

        // start sub-modules
//        habUploader.start();
        for (MqttListener listener : mqttListeners) {
            listener.start();
        }
    }

    private void messageReceived(LoraWanUplinkMessage message) {
        // TODO
    }
    
    /**
     * Stops the application.
     * 
     * @throws MqttException
     */
    private void stop() {
        for (MqttListener listener : mqttListeners) {
            listener.stop();
        }
//        habUploader.stop();
        LOG.info("Stopped TTN-sondehub bridge application");
    }
}
