package com.aboni.sensors;

public class SensorNotInititalizedException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = -5584485460132915583L;

    public SensorNotInititalizedException() {
    }

    public SensorNotInititalizedException(String message) {
        super(message);
    }

    public SensorNotInititalizedException(Throwable cause) {
        super(cause);
    }

    public SensorNotInititalizedException(String message, Throwable cause) {
        super(message, cause);
    }

    public SensorNotInititalizedException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
