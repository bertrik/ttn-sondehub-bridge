package nl.bertriksikken.sondehub;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Representation of a message towards sondehub, can be serialized into JSON.
 */
public abstract class SondehubMessage {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private final Map<String, Object> messageMap = new HashMap<>();

    protected SondehubMessage() {
        messageMap.put("software_name", "ttn-sondehub-bridge");
        messageMap.put("software_version", "dev");
    }

    protected void addProperty(String name, Object value) {
        messageMap.put(name, value);
    }

    ObjectNode toObjectNode() {
        Instant now = Instant.now();
        messageMap.put("upload_time", now.toString());
        return MAPPER.valueToTree(messageMap);
    }
}
