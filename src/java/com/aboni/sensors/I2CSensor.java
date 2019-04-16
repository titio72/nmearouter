package com.aboni.sensors;

import com.aboni.utils.HWSettings;
import com.aboni.utils.ServerLog;
import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;

import java.io.IOException;

public abstract class I2CSensor implements Sensor {

    @SuppressWarnings("unused")
    protected void debug(String msg) {
        ServerLog.getLogger().Debug( 
                "sensor {" + getSensorName() + "} instance {" + 
                instance + "} " + msg);
    }
    
    protected void log(String msg) {
        ServerLog.getLogger().Info(
                "sensor {" + getSensorName() + "} instance {" + 
                instance + "} " + msg);
    }
    
    @SuppressWarnings("unused")
    protected void warning(String msg, Throwable t) {
        if (t==null) 
            ServerLog.getLogger().Warning(
                "sensor {" + getSensorName() + "} instance {" + 
                instance + "} " + msg);
        else 
            ServerLog.getLogger().Error( 
                "sensor {" + getSensorName() + "} instance {" + 
                instance + "} " + msg,  t);
    }
    
    protected void error(String msg, Throwable t) {
        if (t==null) 
            ServerLog.getLogger().Error( 
                    "sensor {" + getSensorName() + "} instance {" + 
                    instance + "} " + msg);
        else 
            ServerLog.getLogger().Error( 
                    "sensor {" + getSensorName() + "} instance {" + 
                    instance + "} " + msg,  t);
    }

    private static final double LPF_ALPHA = 0.75;
    
    private static int instanceCounter;
    private final int instance;

    private double smootingAlpha;

    private static final int MAX_FAILURES = 5;
    private final int maxFailures;
    private int failures;
    
    private boolean initialized;
    
    public I2CSensor() {
        setDefaultSmootingAlpha(LPF_ALPHA);
        instanceCounter++;
        instance = instanceCounter;
        initialized = false;
        failures = 0;
        maxFailures = MAX_FAILURES;
    }

    /* (non-Javadoc)
	 * @see com.aboni.sensors.Sensor#init()
	 */
    @Override
	public final void init() throws IOException, UnsupportedBusNumberException {
        init(getBus());
    }

    public final void init(int bus) throws IOException, UnsupportedBusNumberException {
        log("Initializing bus {" + bus + "}");
        _init(bus);
        log("Initialized!");
        initialized = true;
    }

    protected abstract void _init(int bus) throws IOException, UnsupportedBusNumberException;

    /* (non-Javadoc)
	 * @see com.aboni.sensors.Sensor#getSensorName()
	 */
    @Override
	public abstract String getSensorName();

    protected final boolean isInitialized() {
        return initialized;
    }

    protected abstract void _read() throws Exception;

    /* (non-Javadoc)
	 * @see com.aboni.sensors.Sensor#read()
	 */
    @Override
	public final void read() throws SensorNotInititalizedException {
        if (isInitialized() && (failures<maxFailures)) {
            try {
            	_read();
            } catch (Exception e) {
            	failures++;
            	error("Error reading sensor {" + e.getMessage() + "} failure {" + failures + "/" + maxFailures + "}", e);
            }
        } else throw new SensorNotInititalizedException("Sensor not initialized!"); 
    }

	public double getDefaultSmootingAlpha() {
        return smootingAlpha;
    }

	public void setDefaultSmootingAlpha(double smootingAlpha) {
        this.smootingAlpha = smootingAlpha;
    }
    
    protected int getBus() {
        return HWSettings.getPropertyAsInteger("bus", 1);
    }
}