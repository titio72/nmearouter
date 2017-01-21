package com.aboni.geo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;


public class DeviationManager {

    class Pair implements Comparable<Pair> {
        
        Pair() {}
        
        Pair(int r, double a) {
            reading = r;
            actual = a;
        }
        
        Pair(Pair p) {
            this(p.reading, p.actual);
        }
        
        int reading;
        double actual;
        
        @Override
        public boolean equals(Object o) {
            if (o instanceof Pair) {
                return (reading==((Pair)o).reading);
            }
            return false;
        }
        
        @Override
        public int compareTo(Pair o) {
            return (reading<o.reading) ? -1 : ( ((reading>o.reading)) ? 1 : 0 );  
        }
    }
    
    private ArrayList<Pair> deviationMap;
    
    public DeviationManager() {
        deviationMap = new ArrayList<Pair>();
    }
    
    /**
     * Load an existing deviation map.
     * Each line is a sample in the form compass,magnetic
     * @param stream An InputStream for the deviation map. 
     * @throws IOException In case reading fails.
     */
    public void load(InputStream stream) throws IOException {
    
    	synchronized (this) {
	        BufferedReader r = new BufferedReader(new InputStreamReader(stream));
	        String line;
			while ((line = r.readLine()) != null) {
	        	String[] sample = line.split(",");
	        	if (sample.length==2) {
	        		add(Integer.parseInt(sample[0]), Double.parseDouble(sample[1]));
	        	}
	        }
			r.close();
    	}
    }

    /**
     * Dumps the deviation map (in the form of {@link #load(InputStream)})to the given output stream.
     * @param stream The stream where to dump the deviation map to.
     * @throws IOException In case writing fails. 
     */
    public void dump(OutputStream stream) throws IOException {
    	synchronized (this) {
	    	Pair p = null;
	    	for (Iterator<Pair> iter = deviationMap.iterator(); iter.hasNext(); ) {
	    	    p = iter.next();
	    		stream.write((p.reading + "," + p.actual + "\r\n").getBytes());
	        }
    	}
    }

    /**
     * Clear the deviation table.
     */
    public void reset() {
    	synchronized (this) {
    		deviationMap.clear();
    	}
    }
    
    /**
     * Add a sample.
     * @param reading The compass reading in decimal degrees.
     * @param actual The magnetic reading (reading of a compensated compass).
     */
    public void add(double reading, double magnetic) {
        synchronized (this) {
	    	if (Math.abs(reading)>360.0 || Math.abs(magnetic)>360.0) {
	            throw new IllegalArgumentException();
	        } else {
	            
	            Pair sample = new Pair(((int)reading)%360, normalize(magnetic));
	                    
	            int p = Collections.binarySearch(deviationMap, sample);
	            if (p>=0) { 
	                deviationMap.get(p).actual = sample.actual;
	            } else {
	                p = -(p+1);
	                deviationMap.add(p, sample);
	            }
	        }
        }
    }

    /**
     * Get the magnetic north given the compass reading by applying the deviation map.
     * @param reading The compass reading in decimal degrees to be converted.
     * @return The magnetic north in decimal degrees [0..360].
     */
    public double getMagnetic(double reading) {
    	synchronized (this) {
	    	reading = normalize(reading);
	    	
	        double res = reading;
	        
	        Pair sample = new Pair();
	        sample.reading = (int)reading; 
	        sample.actual = 0;
	        
	        //variationMap.sort(null);
	        
	        int p = Collections.binarySearch(deviationMap, sample);
	        if (p>=0) {
	            res = (reading - (int)reading) + deviationMap.get(p).actual;
	        } else if (deviationMap.size()>1) {
	            p = -(p+1);
	            Pair p0 = new Pair(deviationMap.get((p-1) % deviationMap.size()));
	            Pair p1 = new Pair(deviationMap.get(p % deviationMap.size()));
	
	            double[] readings = spreadThem(p0.reading, normalize(reading), p1.reading);
	            double[] actual = spreadThem(p0.actual, p1.actual);
	            
	            double dSamples = readings[2] - readings[0];
	            double dReading = readings[1] - readings[0];
	            double dActual = actual[1] - actual[0];
	            res = actual[0] + dActual * dReading / dSamples;
	            if (res>360.0) res -= 360;
	        }
	        
	        return res;
    	}
    }
    
    private static double[] spreadThem(double low, double mid, double high) {
    	if (low>mid) low = low - 360.0;
    	if (high<mid) high = high + 360.0;
    	
    	return new double[] {low, mid, high};
    }
    
    private static double[] spreadThem(double low, double high) {
    	if (low>high) low = low - 360.0;
    	if (low<0.0) {
    		low += 360.0;
    		high += 360.0;
    	}
    	
    	return new double[] {low, high};
    }

    private static double normalize(double m) {
        if (m>360.0) {
            return m - (360*((int)(m/360)));
        }
        else if (m<0.0) {
            return m + (360*((int)(-m/360))) + 360;
        }
        return m;
    }
}
