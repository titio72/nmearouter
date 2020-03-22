package com.aboni.sensors;

public class SensorNotInitializedException extends SensorException {

    private static final long serialVersionUID = -5584485460132915583L;

    public SensorNotInitializedException(String message) {
        super(message);
    }
}
