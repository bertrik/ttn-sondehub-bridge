package nl.bertriksikken.sondehub;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.node.ObjectNode;

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
    private ISondehubRestApi restClient;

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
    }

    public void scheduleTelemetryUpload(LoraWanUplinkMessage uplink) {
        // convert uplink to list of SondeHubMessage
        List<ObjectNode> list = new ArrayList<>();

        Instant uploaderTime = Instant.now(); // TODO get from uplink message
        String payloadId = uplink.getDevId();
        Instant payloadTime = Instant.now(); // decode from payload!
        double latitude = 52.0;
        double longitude = 4.7;
        for (GatewayInfo gw : uplink.getGateways()) {
            SondehubMessage message = new SondehubMessage(gw.getId(), uploaderTime, payloadId, payloadTime, latitude,
                    longitude, 0);
            list.add(message.toObjectNode());
        }

        executor.execute(() -> uploadTelemetry(list));
    }

    private void uploadTelemetry(List<ObjectNode> objects) {
        try {
            Response<String> result = restClient.uploadAmateurTelemetry(objects).execute();
            LOG.info("result = {}", result);
        } catch (IOException e) {
            LOG.warn("upload failed", e);
        }
    }

}
