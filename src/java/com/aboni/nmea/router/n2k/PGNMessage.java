package com.aboni.nmea.router.n2k;

import org.json.JSONObject;

public interface PGNMessage {
    int getPgn();

    JSONObject getFields();

    int getSource();
}