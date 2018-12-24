package com.aboni.sensors.hw;

public class FrequencyMeter {

    private int threshold;
	private int status; // -1 0 +1
	private int wx;
	private long period;
	private double freq;

	private static final int BUF_SIZE = 16384;
	
    /**
     * Samples are collected and counted over a moving window of "sensitivity" milliseconds.
     * The longer, the stabler the measure but less reactive.
     */
	private long sensitivity;   
    
    private final long[] times;
    
	public FrequencyMeter() {
        times = new long[BUF_SIZE];
		initDefault();
	}

	private void initDefault() {
		threshold = 16383;
		status = 0;
		sensitivity = 1000;
	}
	
	public void sample(long timestamp, int analogvalue) {
	    synchronized (this) {
	    	//System.out.println(""+analogvalue);
    		int newStatus;
    		if (analogvalue >= threshold) newStatus = 1; 
    		else if (analogvalue <= -threshold) newStatus = -1; 
    		else newStatus = 0;
    	
            if (newStatus==1 && status!=1) {
                wx = mod(wx + 1, BUF_SIZE);
                times[wx] = System.currentTimeMillis();
            }	
            
    		status = newStatus;
	    }
	}

	/**
	 * calculate the frequency on a time span (timestamp-sensitivity, timestamp).
	 * @param timestamp The starting time in ms
	 * @return
	 */
    public double calcFreq(long timestamp) {
        synchronized (this) {
            
            int rx = wx;
            
            int counter = 0;
            long ts = times[rx];
            while ((timestamp-ts) < sensitivity && ts>0 && counter<(BUF_SIZE/2)) {
                counter++;
                rx = mod(rx - 1, BUF_SIZE);
                ts = times[rx];
            }
            if (counter!=0) {
                freq = (counter - 1) * (1000.0 / (double)(timestamp - ts));
                period = (int)((1.0 / freq) * 1000.0);
            } else {
                freq = 0;
                period = 0;
            }
            return freq;
        }
        
    }
	
	public long getPeriod() {
        synchronized (this) {
            return period;
        }
	}
	
	public double getFrequency() {
        synchronized (this) {
            return freq;
        }
	}

    public void setThreshold(int d) {
        synchronized (this) {
            threshold = d;
        }
    }

    public void setSensitivity(long i) {
        synchronized (this) {
            sensitivity = i;
        }        
    }
    
    private static int mod(int v, int m) {
        v = v % m;
        return (v<0)?(v+m):v;
    }
}
