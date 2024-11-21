package nl.bertriksikken.sondehub;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.node.ObjectNode;

import nl.bertriksikken.lorawan.Location;
import nl.bertriksikken.lorawan.LoraWanUplinkMessage;
import nl.bertriksikken.lorawan.LoraWanUplinkMessage.GatewayInfo;
import okhttp3.OkHttpClient;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public final class SondehubUploader {

    private static final Logger LOG = LoggerFactory.getLogger(SondehubUploader.class);

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Map<String, Instant> lastUploadTime = new HashMap<>();

    private final ISondehubRestApi restClient;

    /**
     * Creates a new habitat uploader.
     * 
     * @param config the configuration
     * @return the habitat uploader
     */
    public static SondehubUploader create(SondehubConfig config) {
        Duration timeout = config.getTimeout();
        LOG.info("Creating new sondehub REST client with timeout {} for {}", timeout, config.getUrl());
        OkHttpClient client = new OkHttpClient().newBuilder().connectTimeout(timeout).readTimeout(timeout)
                .writeTimeout(timeout).build();
        Retrofit retrofit = new Retrofit.Builder().baseUrl(config.getUrl())
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(JacksonConverterFactory.create()).client(client).build();
        ISondehubRestApi restClient = retrofit.create(ISondehubRestApi.class);
        return new SondehubUploader(restClient);
    }

    /**
     * Constructor.
     * 
     * @param restClient the REST client used for uploading
     */
    SondehubUploader(ISondehubRestApi restClient) {
        this.restClient = restClient;
    }

    /**
     * Starts the uploader process.
     */
    public void start() {
        LOG.info("Starting SondeHub uploader");
    }

    /**
     * Stops the uploader process.
     */
    public void stop() {
        LOG.info("Stopping SondeHub uploader");
        executor.shutdown();
        try {
            executor.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            LOG.error("Failed to stop uploader");
        }
    }

    public void scheduleTelemetryUpload(Instant now, LoraWanUplinkMessage uplink) {
        List<ObjectNode> list = new ArrayList<>();
        Instant uploaderTime = uplink.getTime();
        String payloadId = uplink.getDevId();

        for (GatewayInfo gw : uplink.getGateways()) {
            String uploaderCallsign = gw.getId();

            // listener
            Location location = gw.getLocation();
            double[] position = new double[] { location.getLat(), location.getLon(), location.getAlt() };
            String antenna = String.format(Locale.ROOT, "%.1fm", location.getAlt());
            SondehubListenerMessage listenerMessage = new SondehubListenerMessage(uploaderCallsign, position,
                    "TheThingsNetwork", antenna);
            executor.execute(() -> uploadListener(now, uploaderCallsign, listenerMessage));

            // payload
            double latitude = 52.0;
            double longitude = 4.7;
            double altitude = 1000.0;
            Instant payloadTime = now;
            SondehubTelemetryMessage message = new SondehubTelemetryMessage(uploaderCallsign, uploaderTime, payloadId,
                    payloadTime, latitude, longitude, altitude);
            message.setRadioMetadata("LoRa", 5.0, 868.1, -70.0);
            message.setFrameNumber(uplink.getFcnt());
            list.add(message.toObjectNode());
        }

        executor.execute(() -> uploadTelemetry(list));
    }

    private void uploadTelemetry(List<ObjectNode> telemetry) {
        try {
            Response<String> result = restClient.uploadAmateurTelemetry(telemetry).execute();
            if (result.isSuccessful()) {
                LOG.info("Telemetry upload success: {}", result.body());
            } else {
                LOG.warn("Telemetry upload failed ({}): {}", result.code(), result.errorBody().string());
            }
        } catch (IOException e) {
            LOG.warn("upload failed", e);
        }
    }

    private void uploadListener(Instant now, String callSign, SondehubListenerMessage listener) {
        // skip upload if it was already uploaded some time ago
        Instant lastListenerUpload = lastUploadTime.getOrDefault(callSign, Instant.MIN);
        if (Duration.between(lastListenerUpload, now).toSeconds() < 3600) {
            return;
        }

        try {
            Response<String> result = restClient.uploadAmateurListener(listener.toObjectNode()).execute();
            if (result.isSuccessful()) {
                lastUploadTime.put(callSign, now);
                LOG.info("Listener upload success: {}", result.body());
            } else {
                LOG.warn("Listener upload failed ({}): {}", result.code(), result.errorBody().string());
            }
        } catch (IOException e) {
            LOG.warn("upload failed", e);
        }
    }

}
