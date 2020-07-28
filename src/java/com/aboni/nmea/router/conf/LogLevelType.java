package com.aboni.nmea.router.conf;

public enum LogLevelType {

    DEBUG,
    INFO,
    WARNING,
    ERROR,
    NONE;

    public String value() {
        return name();
    }

    public static LogLevelType fromValue(String v) {
        return valueOf(v);
    }

}
