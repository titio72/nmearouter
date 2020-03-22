package com.aboni.sensors;

import com.aboni.misc.DataFilter;
import com.aboni.misc.Utils;
import com.aboni.sensors.hw.HMC5883L;
import com.aboni.utils.ServerLog;
import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;

import java.io.IOException;

public class SensorHMC5883 extends I2CSensor {

    private static final int X = 0; 
    private static final int Y = 1; 
    private static final int Z = 2; 
    
    private HMC5883L hmc5883l;
    private double[] mag;

    public SensorHMC5883() {
        super();
        setDefaultSmoothingAlpha(0.66);
    }

    @Override
    protected void initSensor(int bus) throws IOException, UnsupportedBusNumberException {
        hmc5883l = new HMC5883L(new I2CInterface(bus, HMC5883L.HMC5883_I2C_ADDRESS));
        int i = 0;
        while (i < 3 && !doInit()) {
            i++;
            Utils.pause(5000);
        }
    }

    private boolean doInit() {
        try {
            Thread.sleep(500);
            hmc5883l.enable();
            Thread.sleep(500);
            hmc5883l.setContinuousMode();
            Thread.sleep(500);
            hmc5883l.setScale(HMC5883L.Scale.Gauss_1_30);
            return true;
        } catch (Exception e) {
            ServerLog.getLogger().error("Failed initialization HMC5883L", e);
            return false;
        }
    }

    @Override
    protected void readSensor() throws SensorException {
    	synchronized (this) {
    	    if (hmc5883l != null) {
    	        try {
    	            setMagReading(hmc5883l.getScaledMag());
                } catch (IOException e) {
    	            throw new SensorException("Error reading compass", e);
                }
    	    }
    	}
    }

    private void setMagReading(double[] m) {
		if (mag==null) {
		    mag = m; 
		} else {
		    mag = new double[]{
                    DataFilter.getLPFReading(getDefaultSmoothingAlpha(), mag[X], m[X]),
                    DataFilter.getLPFReading(getDefaultSmoothingAlpha(), mag[Y], m[Y]),
                    DataFilter.getLPFReading(getDefaultSmoothingAlpha(), mag[Z], m[Z])
            };
		}
	}

    public double[] getMagVector() {
        return mag;
    }

    @Override
    public String getSensorName() {
        return "HMC5883";
    }
}
