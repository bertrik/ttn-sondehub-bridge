package nl.bertriksikken.bridge;

import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.bertriksikken.lorawan.LoraWanUplinkMessage;
import nl.bertriksikken.sondehub.SondehubConfig;
import nl.bertriksikken.sondehub.SondehubUploader;

/**
 * Sends a (fake) telemetry package to sondehub, for testing.
 */
public final class RunTestUpload {

    private static final Logger LOG = LoggerFactory.getLogger(RunTestUpload.class);

    public static void main(String[] args) {
        RunTestUpload runner = new RunTestUpload();
        runner.run();
    }

    private void run() {
        SondehubConfig config = new SondehubConfig();
        SondehubUploader uploader = SondehubUploader.create(config);

        Instant time = Instant.now();
        LoraWanUplinkMessage uplink = new LoraWanUplinkMessage("TheThingsNetwork", time, "BERTRIK-TEST", 1, 1,
                new byte[] {});
        uplink.addGateway("BERTRIK", 52.02275845759663, 4.69205968296466, 0.0);

        try {
            uploader.start();
            uploader.scheduleTelemetryUpload(Instant.now(), uplink);
        } finally {
            uploader.stop();
        }
    }

}
