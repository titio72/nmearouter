package com.aboni.nmea.router.conf;

public enum InOut {

    IN,
    OUT,
    INOUT;

    public String value() {
        return name();
    }

    public static InOut fromValue(String v) {
        return valueOf(v);
    }

}
