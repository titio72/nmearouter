package com.aboni.sensors;

import java.io.IOException;

import com.aboni.misc.Utils;
import com.aboni.sensors.hw.HMC5883L;
import com.aboni.utils.DataFilter;
import com.aboni.utils.ServerLog;
import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;

public class SensorHMC5883 extends I2CSensor {

    private static final int X = 0; 
    private static final int Y = 1; 
    private static final int Z = 2; 
    
    private HMC5883L hmc5883l;
    private double[] mag;

    public SensorHMC5883() {
        super();
        setDefaultSmootingAlpha(0.66);
    }

    @Override
    protected void _init(int bus) throws IOException, UnsupportedBusNumberException {
    	hmc5883l = new HMC5883L(new I2CInterface(bus, HMC5883L.HMC5883_I2CADDR));
    	int i = 0;
    	while (i<3 && !doInit()) { 
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
            ServerLog.getLogger().Error("Failed initialization HMC5883L", e);
            return false;
        }
    }

    @Override
    protected void _read() throws Exception {
    	synchronized (this) {
    	    if (hmc5883l != null) {
    	        setMagReading(hmc5883l.getScaledMag());
    	    }
    	}
    }

    private void setMagReading(double[] m) {
		if (mag==null) {
		    mag = m; 
		} else {
		    mag = new double[] {
		            DataFilter.getLPFReading(getDefaultSmootingAlpha(), mag[X], m[X]),
		            DataFilter.getLPFReading(getDefaultSmootingAlpha(), mag[Y], m[Y]),
		            DataFilter.getLPFReading(getDefaultSmootingAlpha(), mag[Z], m[Z])
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
