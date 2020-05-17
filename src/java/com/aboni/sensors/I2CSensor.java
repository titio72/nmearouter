package com.aboni.sensors;

import com.aboni.utils.HWSettings;
import com.aboni.utils.ServerLog;

public abstract class I2CSensor implements Sensor {

    private String getMessage(String message) {
        return String.format("Sensor: name {%s} instance {%d} %s", getSensorName(), instance, message);
    }

    protected void log(String msg) {
        ServerLog.getLogger().info(getMessage(msg));
    }
    
    protected void error(String msg, Throwable t) {
        if (t==null) 
            ServerLog.getLogger().error(getMessage(msg));
        else 
            ServerLog.getLogger().error( getMessage(msg),  t);
    }

    private static final double LPF_ALPHA = 0.75;

    private static int instanceCounter;
    private final int instance;

    private double smoothingAlpha;

    private static final int MAX_FAILURES = 5;
    private final int maxFailures;
    private int failures;

    private boolean initialized;

    private long lastReadingTS;

    public I2CSensor() {
        setDefaultSmoothingAlpha(LPF_ALPHA);
        instanceCounter++;
        instance = instanceCounter;
        initialized = false;
        failures = 0;
        maxFailures = MAX_FAILURES;
    }

    @Override
    public final void init() throws SensorException {
        init(getBus());
    }

    public final void init(int bus) throws SensorException {
        log("Initializing bus {" + bus + "}");
        initSensor(bus);
        log("Initialized!");
        initialized = true;
    }

    protected abstract void initSensor(int bus) throws SensorException;

    protected final boolean isInitialized() {
        return initialized;
    }

    protected abstract void readSensor() throws SensorException;

    @Override
	public final void read() throws SensorException {
        if (isInitialized() && (failures<maxFailures)) {
            try {
                readSensor();
                lastReadingTS = System.currentTimeMillis();
            } catch (Exception e) {
                failures++;
                error("Error reading sensor {" + e.getMessage() + "} failure {" + failures + "/" + maxFailures + "}", e);
            }
        } else throw new SensorException("Sensor not initialized!");
    }

    public double getDefaultSmoothingAlpha() {
        return smoothingAlpha;
    }

    public void setDefaultSmoothingAlpha(double smoothingAlpha) {
        this.smoothingAlpha = smoothingAlpha;
    }

    protected int getBus() {
        return HWSettings.getPropertyAsInteger("bus", 1);
    }

    @Override
    public long getLastReadingTimestamp() {
        return lastReadingTS;
    }
}