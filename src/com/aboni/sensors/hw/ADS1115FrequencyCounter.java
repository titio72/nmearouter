package com.aboni.sensors.hw;

import com.aboni.sensors.I2CInterface;

public class ADS1115FrequencyCounter {

    private int conf;
    private I2CInterface device;
    private FrequencyMeter freq;
    private static double MULTIPLIER = 5.0;
    private Thread workingThread;
    private int delay;

    public ADS1115FrequencyCounter(I2CInterface d, int c) {
        if (c<0 || c>3) throw new UnsupportedOperationException("ADS1115 channel out of range [0..3]" + c);
        else {
        device = d;
            conf = ADS1115.ADS1x15_REG_CONFIG_CQUE_NONE | // Disable the comparator (default val)
                    ADS1115.ADS1x15_REG_CONFIG_CLAT_NONLAT | // Non-latching (default val)
                    ADS1115.ADS1x15_REG_CONFIG_CPOL_ACTVLOW | // Alert/Rdy active low (default val)
                    ADS1115.ADS1x15_REG_CONFIG_CMODE_TRAD | // Traditional comparator (default val)
                    ADS1115.ADS1x15_REG_CONFIG_DR_3300SPS | // 1600 samples per second (default)
                    ADS1115.ADS1x15_REG_CONFIG_MODE_CONTIN | // continuous mode (default)
                    ADS1115.ProgrammableGainAmplifierValue.PGA_6_144V.getConfigValue() |
                    ADS1115.ADS1x15_REG_CONFIG_MUX_SINGLE[c];
        }
    }

    /**
     * Starts the meter. The threshold is in Volts and it used to "square" the analog input, 
     * the delay is in ms and is used to wait a small amount of time between two samples.
     * Max sampling is >1khz with a 0 delay.  
     * @param threshold
     * @param delay
     */
    public void init(double threshold, int delay) {
        try {
            this.delay = delay;
            int iThreshold = 12000;
            
            System.out.println("Suggested threshold " + 
            		(int) (ADS1115.ADS1115_RANGE_MAX_VALUE
                    * ((threshold / MULTIPLIER) / ADS1115.ProgrammableGainAmplifierValue.PGA_6_144V.getVoltage())));
            freq = new FrequencyMeter();
            freq.setSensitivity(500);
            freq.setThreshold(iThreshold);
            ADS1115.writeRegister(device, ADS1115.ADS1x15_REG_POINTER_CONFIG, conf);
            Thread.sleep(16);
            start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void start() {
        if (workingThread==null) {
            workingThread = new Thread(new Runnable() {
                public void run() {
                    while (true) {
                        read();
                        if (delay>0) {
                            try { Thread.sleep(delay); } catch (Exception e) {}
                        }
                    }
                }
            });
            workingThread.setDaemon(true);
            workingThread.start();
        }
    }
    
    private void read() {
        synchronized (device) {
            try {
                int value = ADS1115.readRegister(device, ADS1115.ADS1x15_REG_POINTER_CONVERT);
                //System.out.println(""+value);
                freq.sample(System.currentTimeMillis(), value);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public double getFrequency() {
        synchronized (device) {
            return freq.calcFreq(System.currentTimeMillis());
        }
    }

}
