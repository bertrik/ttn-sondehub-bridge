package nl.bertriksikken.lorawan;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * LoRaWAN uplink message, stack independent
 */
public final class LoraWanUplinkMessage {

    private final String network;
    private final Instant time;
    private final String deviceId;
    private final int fcnt;
    private final int port;
    private final Map<String, Object> payloadFields = new HashMap<>();
    private final byte[] payloadRaw;
    private final List<GatewayInfo> gateways = new ArrayList<>();

    public LoraWanUplinkMessage(String network, Instant time, String deviceId, int fcnt, int port, byte[] payloadRaw) {
        this.network = network;
        this.time = time;
        this.deviceId = deviceId;
        this.fcnt = fcnt;
        this.port = port;
        this.payloadRaw = payloadRaw.clone();
    }

    public void addField(String name, Object value) {
        payloadFields.put(name, value);
    }

    public String getNetwork() {
        return network;
    }
    
    public Instant getTime() {
        return time;
    }

    public String getDevId() {
        return deviceId;
    }

    public int getFcnt() {
        return fcnt;
    }

    public byte[] getPayloadRaw() {
        return payloadRaw.clone();
    }

    public Map<String, Object> getPayloadFields() {
        return new HashMap<>(payloadFields);
    }

    public int getPort() {
        return port;
    }

    public void addGateway(String id, double lat, double lon, double alt) {
        gateways.add(new GatewayInfo(id, new Location(lat, lon, alt)));
    }

    public List<GatewayInfo> getGateways() {
        return List.copyOf(gateways);
    }

    public static final class GatewayInfo {

        private final String id;
        private final Location location;

        public GatewayInfo(String id, Location location) {
            this.id = id;
            this.location = location;
        }

        public String getId() {
            return id;
        }

        public Location getLocation() {
            return location;
        }

    }

    // interface for messages that can convert themselves to a LoraWanUplinkMessage
    public interface ILoraWanUplink {
        LoraWanUplinkMessage toLoraWanUplinkMessage();
    }
    
}
