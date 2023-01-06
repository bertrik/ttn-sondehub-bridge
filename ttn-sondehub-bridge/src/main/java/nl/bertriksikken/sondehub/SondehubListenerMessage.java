package nl.bertriksikken.sondehub;

public final class SondehubListenerMessage extends SondehubMessage {

    SondehubListenerMessage(String callSign, double[] position, String radio, String antenna) {
        addProperty("uploader_callsign", callSign);
        addProperty("uploader_position", position);
        addProperty("uploader_radio", radio);
        addProperty("uploader_antenna", antenna);
        addProperty("mobile", false);
    }

}
