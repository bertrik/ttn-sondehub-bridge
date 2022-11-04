package nl.bertriksikken.sondehub;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Representation of a message towards sondehub, can be serialized into JSON.
 * 
 * See
 * https://github.com/projecthorus/sondehub-infra/wiki/%5BDRAFT%5D-Amateur-Balloon-Telemetry-Format
 */
public final class SondehubMessage {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final Map<String, Object> messageMap = new HashMap<>();

    /**
     * Constructor with all mandatory fields.
     */
    SondehubMessage(String uploaderCallsign, Instant uploaderTime, String payloadCallsign, Instant payloadTime,
            double latitude, double longitude, double altitude) {
        messageMap.put("software_name", "ttnhabbridge");
        messageMap.put("software_version", "development");
        messageMap.put("uploader_callsign", uploaderCallsign);
        messageMap.put("time_received", uploaderTime.toString());
        messageMap.put("payload_callsign", payloadCallsign);
        messageMap.put("datetime", payloadTime.toString());
        messageMap.put("lat", latitude);
        messageMap.put("lon", longitude);
        messageMap.put("alt", altitude);
    }
    
    void setFrameNumber(int frameNumber) {
        messageMap.put("frame", frameNumber);
    }
    
    void setTemperature(float temperatureC) {
        messageMap.put("temp", temperatureC);
    }
    
    void setHumidity(float relativeHumidity) {
        messageMap.put("humidity", relativeHumidity);
    }
    
    void setPressure(float hectoPascal) {
        messageMap.put("pressure", hectoPascal);
    }

    void setSats(int numberSats) {
        messageMap.put("sats", numberSats);
    }
    
    void setBattery(float voltage) {
        messageMap.put("batt", voltage);
    }
    
    void setTxFrequency(float frequencyMhz) {
        messageMap.put("tx_frequency", frequencyMhz);
    }
    
    void setRaw(String raw) {
        messageMap.put("raw", raw);
    }
    
    /**
     * Set optional radio metadata.
     */
    void setRadioMetadata(String modulation, float snr, float frequencyMHz, float rssi) {
        messageMap.put("modulation", modulation);
        messageMap.put("snr", snr);
        messageMap.put("frequency", frequencyMHz);
        messageMap.put("rssi", rssi);
    }
            
    /**
     * Set optional uploader metadata.
     */
    void setUploaderMetadata(float[] position, String antenna, String radio) {
        messageMap.put("uploader_position", position);
        messageMap.put("antenna", antenna);
        messageMap.put("radio", radio);
    }
    
    ObjectNode toObjectNode() {
        Instant now = Instant.now();
        messageMap.put("upload_time", now.toString());
        return MAPPER.valueToTree(messageMap);
    }

}
