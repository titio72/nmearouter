package com.aboni.sensors;

import java.io.IOException;
import java.util.Properties;

import com.aboni.sensors.hw.ADS1115;
import com.aboni.sensors.hw.ADS1115FrequencyCounter;
import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;

public class SensorRPM extends I2CSensor {

    private ADS1115FrequencyCounter ads;
    private int address;
    
    private double frequency;
    
    public SensorRPM() {
    	super();
        address = ADS1115.ADS1115_ADDRESS_0x48;

        Properties p = getProps().readConf();
        if (p!=null) {
            String address = p.getProperty("analog.rpm", "0x48");
            if (address.startsWith("0x")) {
                this.address = Integer.parseInt(address.substring(2), 16);
            } else {
                this.address = Integer.parseInt(address);
            }
        }
    }
    
    public SensorRPM(int address) {
        this.address = address;
    }
    
    @Override
    protected void _init(int bus) throws IOException, UnsupportedBusNumberException {
        ads = new ADS1115FrequencyCounter(new I2CInterface(bus, address), 0);
    }

    @Override
    public String getSensorName() {
        return "RPM";
    }

    @Override
    protected void _read() throws Exception {
        frequency = ads.getFrequency();
    }
    
    public double getRPM() {
        return frequency * 60.0 / 9.0;
    }
    
    public void startReading() {
        ads.init(11.0, 0);
    }
}
