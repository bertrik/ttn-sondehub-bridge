package nl.bertriksikken.bridge;

import com.fasterxml.jackson.annotation.JsonProperty;

import nl.bertriksikken.lorawan.MqttConfig;
import nl.bertriksikken.sondehub.SondehubConfig;

public final class ApplicationConfig {

    @JsonProperty("thethingsnetwork")
    public MqttConfig ttnConfig = new MqttConfig("tcp://eu1.cloud.thethings.network", "appname",
            "NNSXS.SIY7VBOR2KTIDBJY7QVTILSORMGIEQ63YNDNBIY.SECRET", "v3/+/devices/+/up");

    @JsonProperty("sondehub")
    public SondehubConfig sondehub = new SondehubConfig();

}
