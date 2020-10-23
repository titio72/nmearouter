package com.aboni.nmea.router.message;

import org.json.JSONObject;

public interface MsgRateOfTurn extends Message {

    int getSID();

    double getRateOfTurn();

    @Override
    default JSONObject toJSON() {
        JSONObject j = new JSONObject();
        j.put("topic", "ROT");
        if (!Double.isNaN(getRateOfTurn())) j.put("rate", getRateOfTurn());
        return j;
    }
}
