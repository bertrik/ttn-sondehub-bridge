package nl.bertriksikken.sondehub;

import java.util.List;

import com.fasterxml.jackson.databind.node.ObjectNode;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.PUT;

/**
 * Interface definition for payload telemetry and listener telemetry towards
 * sondehub.
 * <p>
 * https://github.com/projecthorus/sondehub-infra/wiki/API-(Beta)#amateurtelemetry
 */
public interface ISondehubRestApi {

    /**
     * @param telemetry list of JSON object nodes created from SondeHubMessage.
     * @return call result
     */
    @PUT("/amateur/telemetry")
    Call<String> uploadAmateurTelemetry(@Body List<ObjectNode> telemetry);

    /**
     * @param listener listener info
     * @return call result
     */
    @PUT("/amateur/listeners")
    Call<String> uploadAmateurListener(@Body ObjectNode listener);

}
