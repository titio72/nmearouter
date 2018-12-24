package com.aboni.sensors;

import java.io.IOException;

import com.aboni.sensors.hw.ADS1115;
import com.aboni.utils.DataFilter;
import com.aboni.utils.HWSettings;
import com.aboni.utils.ServerLog;
import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;

public class SensorVoltage extends I2CSensor {
    
    private static final double MULTIPLIER = 5.0;

    private ADS1115 ads;
    private final double[] v = new double[4];
    private int address;
    private double smoothing = 0.75; 
    private final double[] adj = new double[] { 1, 1, 1, 1 };
    
    public SensorVoltage(int address) {
        ads = null;
        this.address = address;
    }

    public SensorVoltage() {
    	super();
        ads = null;

        this.address = ADS1115.ADS1115_ADDRESS_0x48;
        
        String s_address = HWSettings.getProperty("analog.voltage", "0x48");
        
    	smoothing = getDefaultSmootingAlpha();
        String s_smoothing = HWSettings.getProperty("analog.voltage.smoothing", "");
        if (s_smoothing!=null && !s_smoothing.isEmpty()) {
        	try {
        		smoothing = Double.parseDouble(s_smoothing);
        	} catch (Exception e) {
        		ServerLog.getLogger().Error("Cannot parse voltage smoothing factor " + s_smoothing, e);
        	}
        }
        
        loadAdjustment();
        
        if (s_address.startsWith("0x")) {
            this.address = Integer.parseInt(s_address.substring(2), 16);
        } else {
            this.address = Integer.parseInt(s_address);
        }
        
        
    }

	private void loadAdjustment() {
		for (int i = 0; i<4; i++) {
			adj[i] = HWSettings.getPropertyAsDouble("analog.voltage.adjust." + i, 1.0);
		}
	}

    public double getVoltage0() {
        return v[0] * adj[0];
    }
    
    public double getVoltage1() {
        return v[1] * adj[1];
    }
    
    public double getVoltage2() {
        return v[2] * adj[2];
    }
    
    public double getVoltage3() {
        return v[3] * adj[3];
    }
    
    @Override
    protected void _init(int bus) throws IOException, UnsupportedBusNumberException {
        ads = new ADS1115(new I2CInterface(bus, address), MULTIPLIER);
    }

    @Override
    public String getSensorName() {
        return "ADS1115-15V";
    }

    @Override
    protected void _read() throws Exception {
    	loadAdjustment();
        for (int i = 0; i<4; i++) {
            double _v = ads.getVoltage(i);
        	v[i] = DataFilter.getLPFReading(smoothing, v[i], _v);
        }
    }
}
