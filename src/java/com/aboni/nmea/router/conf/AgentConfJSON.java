package com.aboni.nmea.router.conf;

import org.json.JSONObject;

public interface AgentConfJSON {

    String getType();

    String getName();

    QOS getQos();

    JSONObject getConfiguration();

    default InOut getInOut() {
        if (getConfiguration() != null && getConfiguration().has("inout")) {
            return InOut.fromValue(getConfiguration().getString("inout"));
        } else {
            return InOut.INOUT;
        }
    }
}
