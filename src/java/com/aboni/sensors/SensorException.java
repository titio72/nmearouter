package com.aboni.sensors;


public class SensorException extends Exception {

    private static final long serialVersionUID = -5584485460132915582L;

    public SensorException(String msg) {
        super(msg);
    }

    public SensorException(String msg, Throwable cause) {
        super(msg, cause);
    }
}