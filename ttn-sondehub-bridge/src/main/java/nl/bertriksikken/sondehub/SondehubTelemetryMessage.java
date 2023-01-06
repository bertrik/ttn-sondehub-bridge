package nl.bertriksikken.sondehub;

import java.time.Instant;

/**
 * Representation of a message towards sondehub, can be serialized into JSON.
 *
 * See
 * https://github.com/projecthorus/sondehub-infra/wiki/%5BDRAFT%5D-Amateur-Balloon-Telemetry-Format
 */
public final class SondehubTelemetryMessage extends SondehubMessage {

    /**
     * Constructor with all mandatory fields.
     */
    SondehubTelemetryMessage(String uploaderCallsign, Instant uploaderTime, String payloadCallsign, Instant payloadTime,
            double latitude, double longitude, double altitude) {
        addProperty("uploader_callsign", uploaderCallsign);
        addProperty("time_received", uploaderTime.toString());
        addProperty("payload_callsign", payloadCallsign);
        addProperty("datetime", payloadTime.toString());
        addProperty("lat", latitude);
        addProperty("lon", longitude);
        addProperty("alt", altitude);
    }

    void setFrameNumber(int frameNumber) {
        addProperty("frame", frameNumber);
    }

    void setTemperature(double temperatureC) {
        addProperty("temp", temperatureC);
    }

    void setHumidity(double relativeHumidity) {
        addProperty("humidity", relativeHumidity);
    }

    void setPressure(double hectoPascal) {
        addProperty("pressure", hectoPascal);
    }

    void setSats(int numberSats) {
        addProperty("sats", numberSats);
    }

    void setBattery(double voltage) {
        addProperty("batt", voltage);
    }

    void setTxFrequency(double frequencyMhz) {
        addProperty("tx_frequency", frequencyMhz);
    }

    void setRaw(String raw) {
        addProperty("raw", raw);
    }

    /**
     * Set optional radio metadata.
     */
    void setRadioMetadata(String modulation, double snr, double frequencyMHz, double rssi) {
        addProperty("modulation", modulation);
        addProperty("snr", snr);
        addProperty("frequency", frequencyMHz);
        addProperty("rssi", rssi);
    }

}
