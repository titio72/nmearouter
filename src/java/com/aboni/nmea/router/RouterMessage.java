package com.aboni.nmea.router;

public interface RouterMessage {

    long getTimestamp();

    String getSource();

    Object getPayload();

}
