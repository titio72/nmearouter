package com.aboni.nmea.router.message;

import org.json.JSONObject;

public interface Message {

    default JSONObject toJSON() {
        throw new UnsupportedOperationException();
    }

}
