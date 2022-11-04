package nl.bertriksikken.sondehub;

import java.time.Duration;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(getterVisibility = Visibility.NONE)
public final class SondehubConfig {

    @JsonProperty("url")
    private final String url;

    @JsonProperty("timeout")
    private final int timeout;

    public SondehubConfig() {
        this("https://api.v2.sondehub.org/", 60);
    }

    public SondehubConfig(String url, int timeout) {
        this.url = url;
        this.timeout = timeout;
    }

    public String getUrl() {
        return url;
    }

    public Duration getTimeout() {
        return Duration.ofSeconds(timeout);
    }

}
