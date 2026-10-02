package nl.bertriksikken.sondehub;

import org.junit.Test;

import java.time.Instant;

public final class SondehubMessageTest {

    @Test
    public void testDummy() {
        new SondehubTelemetryMessage("uploader", Instant.now(), "payload",
                Instant.now(), 0.0, 0.0, 0.0);
    }

}
