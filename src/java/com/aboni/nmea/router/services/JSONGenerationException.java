package com.aboni.nmea.router.services;

public class JSONGenerationException extends Exception {

    public JSONGenerationException(String msg) {
        super(msg);
    }

    public JSONGenerationException(String msg, Throwable t) {
        super(msg, t);
    }

    public JSONGenerationException(Throwable t) {
        super(t);
    }
}
