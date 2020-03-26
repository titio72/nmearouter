package com.aboni.nmea.router.data.meteo;

public class MeteoManagementException extends Exception {

    public MeteoManagementException(String message) {
        super(message);
    }

    public MeteoManagementException(String message, Throwable t) {
        super(message, t);
    }
}
