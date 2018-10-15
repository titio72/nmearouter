package com.aboni.nmea.router.agent.impl.track;

import com.aboni.geo.Course;
import com.aboni.geo.GeoPositionT;

public class TrackManager {
	
    private double maxSpeed;
    private long staticPeriod;
    private long period;
	private GeoPositionT lastPoint;
	private GeoPositionT lastTrackedPoint;
	private StationaryManager stationaryStatus;

	private static int SECOND = 1000;
	private static int MINUTE = 60 * SECOND;
	
	public static final long STATIC_DEFAULT_PERIOD = 10 * MINUTE;
	public static final long DEFAULT_PERIOD = 30 * SECOND;
	
	private static final long STATIC_THRESHOLD_TIME = 15 * MINUTE; // if static for more than x minutes set anchor mode

	private static final double MOVE_THRESHOLD_SPEED_KN = 3.0; // if reported is greater than X then it's moving 
	private static final double MOVE_THRESHOLD_POS_METERS =  35.0; // if move by X meters since last reported point then it's moving

    public TrackManager() {
        period = DEFAULT_PERIOD;
        staticPeriod = STATIC_DEFAULT_PERIOD;
        maxSpeed = 0.0;
        stationaryStatus = new StationaryManager();
    }

    /**
     * Sampling period when cruising
     * @return milliseconds
     */
    public long getPeriod() {
    	return period;
    }
    
    /**
     * Sampling period when at anchor
     * @return milliseconds
     */
    public long getSTationaryPeriod() {
    	return staticPeriod;
    }
    
    public boolean isStationary() {
    	return stationaryStatus.stationary;
    }
    
    public long getStationaryTime() {
    	return stationaryStatus.stationarySince;
    }
    
    private boolean isFirstReport() {
    	return (getLastTrackedPosition()==null);
    }
    
    private boolean shallReport(GeoPositionT p) {
        boolean anchor = stationaryStatus.isAnchor(p.getTimestamp());
        long dt = p.getTimestamp() - getLastTrackedTime();
        long checkperiod = (anchor?getStaticPeriod():getPeriod());
       	return dt >= checkperiod;
    }
    
    public TrackPoint processPosition(GeoPositionT p, double sog) throws Exception {
        
        maxSpeed = Math.max(maxSpeed, sog);
    	TrackPoint res = null;

    	if (isFirstReport()) {
	        stationaryStatus.init(true);
	        if (lastPoint!=null) {
	        	stationaryStatus.updateStationaryStatus(p.getTimestamp(), isStationary(lastPoint, p));
                setLastTrackedPosition(p);
                maxSpeed = sog;
            	res = fillPoint(lastPoint, p);
	        }
    	} else {
	        long dt = p.getTimestamp() - getLastTrackedTime();
	        if (dt >= getPeriod()) {
	        	stationaryStatus.updateStationaryStatus(p.getTimestamp(), isStationary(getLastTrackedPosition(), p));
	            if (shallReport(p)) {
	            	res = fillPoint(getLastTrackedPosition(), p);
                    setLastTrackedPosition(p);
	                maxSpeed = 0.0; // reset maxSpeed for the new sampling period
	        	}
	        }
    	}
    	lastPoint = p;
    	
        return res;
    }

    private TrackPoint fillPoint(GeoPositionT prevPos, GeoPositionT p) {
        boolean anchor = stationaryStatus.isAnchor(p.getTimestamp());
        Course c = new Course(prevPos, p);
        double speed = c.getSpeed(); 
        speed = Double.isNaN(speed)?0.0:speed;
        double dist = c.getDistance(); 
        dist = Double.isNaN(speed)?0.0:dist;
        int period = (int) (c.getInterval()/1000);
        setLastTrackedPosition(p);
        return new TrackPoint(p, anchor, dist,  speed, maxSpeed, period);
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
 
	private boolean isStationary(GeoPositionT p1, GeoPositionT p2) {
	    if (p1==null || p2==null) {
	    	return false;
	    } else {
	    	double dist = p1.distanceTo(p2); // distance in meters
	    	long idtime = Math.abs(p2.getTimestamp() - p1.getTimestamp()); // d-time in mseconds
	    	// calc the speed but only if the two points are at least 500ms apart 
	    	double speed = 
	    			idtime>500 
	    			? 
	    			((dist / (double)idtime) * 1000.0) 
	    			: 
	    			0.0; // meter/second
	    	speed *= 1.94384; // speed in knots
	    	return speed <= MOVE_THRESHOLD_SPEED_KN && dist < MOVE_THRESHOLD_POS_METERS;
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
