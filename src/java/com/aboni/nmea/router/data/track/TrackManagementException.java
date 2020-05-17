package com.aboni.nmea.router.data.track;

public class TrackManagementException extends Exception {
    public TrackManagementException(String msg) {
        super(msg);
    }

    public TrackManagementException(Throwable t) {
        super(t);
    }

    public TrackManagementException(String msg, Throwable t) {
        super(msg, t);
    }
}
