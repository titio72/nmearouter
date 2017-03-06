package com.aboni.sensors;

import java.io.IOException;
import java.util.Properties;

import com.aboni.sensors.hw.ADS1115;
import com.aboni.utils.DataFilter;
import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;

public class SensorVoltage extends I2CSensor {
    
    private static final double MULTIPLIER = 5.0;

    private ADS1115 ads;
    private double[] v = new double[4];
    private int address;

    private double[] adj = new double[] { 1, 1, 1, 1 };
    
    public SensorVoltage(int address) {
        ads = null;
        this.address = address;
    }

    public SensorVoltage() {
    	super();
        ads = null;

        this.address = ADS1115.ADS1115_ADDRESS_0x48;
        
        Properties p = getProps().readConf();
        if (p!=null) {
            String s_address = p.getProperty("analog.voltage", "0x48");
            loadAdjustment();
            
            if (s_address.startsWith("0x")) {
                this.address = Integer.parseInt(s_address.substring(2), 16);
            } else {
                this.address = Integer.parseInt(s_address);
            }
        }
    }

	private void loadAdjustment() {
        Properties p = getProps().readConf();
		String[] s_adjust = new String[] { 
			p.getProperty("analog.voltage.adjust.0", "1"),
			p.getProperty("analog.voltage.adjust.1", "1"),
			p.getProperty("analog.voltage.adjust.2", "1"),
			p.getProperty("analog.voltage.adjust.3", "1") };
		
		for (int i = 0; i<4; i++) {
			adj[i] = Double.parseDouble(s_adjust[i]);
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
        	v[i] = DataFilter.getLPFReading(getDefaultSmootingAlpha(), v[i], _v);
        }
    }

    
}
