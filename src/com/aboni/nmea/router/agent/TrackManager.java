package com.aboni.nmea.router.agent;

import java.util.Calendar;

import com.aboni.geo.Course;
import com.aboni.geo.GeoPositionT;

public class TrackManager {
	
    private double maxSpeed;
    private long staticPeriod;
    private long period;
	private GeoPositionT lastPoint;
	private GeoPositionT lastTrackedPoint;
	private StationaryManager stationaryStatus;

	private static final long STATIC_DEFAULT_PERIOD = 30 * 60000; // 30 minutes;
	private static final long DEFAULT_PERIOD = 60000; // 1 minute
	private static final double STATIC_THRESHOLD = 15.0; // meters in the period
	private static final double STATIC_THRESHOLD_TIME = 15 * 60000; // if static for more than x minutes set anchor mode

	public static class TrackPoint {
		GeoPositionT position;
		boolean anchor;
		double distance;
		double averageSpeed;
		double maxSpeed;
		int period;
		
		public TrackPoint() {
		}

		public TrackPoint(GeoPositionT p, boolean anchor, double dist, double speed, double maxSpeed, int period) {
			this.position = p;
			this.anchor = anchor;
			this.distance = dist;
			this.averageSpeed = speed;
			this.maxSpeed = maxSpeed;
			this.period = period;

		}
	}
	
    public TrackManager() {
        period = DEFAULT_PERIOD;
        staticPeriod = STATIC_DEFAULT_PERIOD;
        maxSpeed = 0.0;
        stationaryStatus = new StationaryManager();
    }

    public boolean isStationary() {
    	return stationaryStatus.stationary;
    }
    
    public long getStationaryTime() {
    	return stationaryStatus.stationarySince;
    }
    
    public boolean isAnchored(Calendar t) {
    	return stationaryStatus.isAnchor(t.getTimeInMillis());
    }
    
    public boolean isAnchored(long t) {
    	return stationaryStatus.isAnchor(t);
    }
    
    private boolean isFirstReport() {
    	return (getLastTrackedPosition()==null);
    }
    
    private boolean shallReport(GeoPositionT p) {
        boolean anchor = stationaryStatus.isAnchor(p.getTimestamp());
        long dt = p.getTimestamp() - getLastTrackedTime();
        long checkperiod = anchor?getStaticPeriod():getPeriod();
       	return dt >= checkperiod;
    }
    
    public TrackPoint processPosition(GeoPositionT p, double sog) throws Exception {
        maxSpeed = Math.max(maxSpeed, sog);
    	TrackPoint res = null;

    	if (isFirstReport()) {
	        stationaryStatus.init(true);
	        if (lastPoint!=null) {
	        	stationaryStatus.updateStationaryStatus(p.getTimestamp(), isMotionless(lastPoint, p));
            	double speed;
            	double dist;
            	int period;
	            boolean anchor = stationaryStatus.isAnchor(p.getTimestamp());
                Course c = new Course(lastPoint, p);
            	speed = c.getSpeed(); speed = Double.isNaN(speed)?0.0:speed;
                dist = c.getDistance(); dist = Double.isNaN(speed)?0.0:dist;
                period = (int) (c.getInterval()/1000);
                setLastTrackedPosition(p);
                maxSpeed = sog;
            	res = new TrackPoint(p, anchor, dist,  speed, maxSpeed, period);
	        }
    	} else {
	        long dt = p.getTimestamp() - getLastTrackedTime();
	        if (dt >= getPeriod()) {
	        	stationaryStatus.updateStationaryStatus(p.getTimestamp(), isMotionless(getLastTrackedPosition(), p));
	            if (shallReport(p)) {
	            	double speed;
	            	double dist;
	            	int period;
		            boolean anchor = stationaryStatus.isAnchor(p.getTimestamp());
	                Course c = new Course(getLastTrackedPosition(), p);
	            	speed = c.getSpeed(); speed = Double.isNaN(speed)?0.0:speed;
	                dist = c.getDistance(); dist = Double.isNaN(speed)?0.0:dist;
	                period = (int) (c.getInterval()/1000);
	                setLastTrackedPosition(p);
	            	res = new TrackPoint(p, anchor, dist,  speed, maxSpeed, period);
	                maxSpeed = 0.0;
	        	}
	        }
    	}
    	lastPoint = p;
    	
        return res;
    }

    public long getPeriod() {
        return period;
    }

    /**
     * Set the sampling time in ms.
     * @param period
     */
    public void setPeriod(long period) {
        this.period = period;
    }

    public long getStaticPeriod() {
        return staticPeriod;
    }

    /**
     * Set the sampling time in ms.
     * @param period
     */
    public void setStaticPeriod(long period) {
        this.staticPeriod = period;
    }

    private long getLastTrackedTime() {
        return (lastTrackedPoint!=null)?lastTrackedPoint.getTimestamp():0; 
    }

    private void setLastTrackedPosition(GeoPositionT lastKnownPosition) {
        lastTrackedPoint = lastKnownPosition;
    }

    public GeoPositionT getLastTrackedPosition() {
        return lastTrackedPoint;
    }
 
	private boolean isMotionless(GeoPositionT p1, GeoPositionT p2) {
	    if (p1==null || p2==null) return false;
	    else {
	    	double dist = p1.distanceTo(p2); // distance in meters
	    	double period = Math.abs(p1.getTimestamp() - p2.getTimestamp());
	    	double normalizedDist = (dist * ((double)getPeriod())) / period; 
	    	return normalizedDist <= STATIC_THRESHOLD;
	    }
	}
	
    private class StationaryManager {

    	boolean init = false;
        boolean stationary = true;
    	long stationarySince = 0;
    	
    	void init(boolean stationary) {
    		if (!init) {
    			this.stationary = stationary;
    			init = true;
    		}
    	}
    	
    	boolean isAnchor(long t) {
    		return stationary && ((t - stationarySince)>STATIC_THRESHOLD_TIME);
    	}
    	
    	void updateStationaryStatus(long t, boolean stationary) {
    		if (this.stationary!=stationary) {
    			this.stationarySince = stationary?t:0;
    			this.stationary = stationary;
    		}
    	}
    }

}
