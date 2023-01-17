package com.aboni.nmea.router.utils;

public class SafeLog {

    private SafeLog() {}

    public static Log getSafeLog(Log log) {
        if (log==null) return new NullLog();
        else return log;
    }
}
