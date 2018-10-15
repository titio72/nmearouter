package com.aboni.nmea.router.agent.impl.track;

import java.util.Calendar;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import com.aboni.geo.GeoPositionT;
import com.aboni.nmea.sentences.NMEAUtils;
import net.sf.marineapi.nmea.sentence.RMCSentence;
import net.sf.marineapi.nmea.util.Position;

public class TrackManagerEx {
	
	private static final int MAGIC_DISTANCE = 5; //5NM
	private static final int SIZE = 60; //~1m of samples
	private Set<Double> lon_positions = new TreeSet<>();
	private Set<Double> lat_positions = new TreeSet<>();
	private long lastTime = 0; 

	private boolean ready() {
		return lat_positions.size()==SIZE;
	}
	
	public boolean acceptPoint(RMCSentence rmc) {
		if (rmc.isValid()) {
			Position pos = NMEAUtils.getPosition(rmc);
	        if (pos!=null) {
	            Calendar timestamp = NMEAUtils.getTimestampOptimistic(rmc);
	            if (timestamp!=null) {
	            	GeoPositionT pos_t = new GeoPositionT(timestamp.getTimeInMillis(), pos);
	                addPos(pos_t);
	            }
	        }
	        if (ready()) {
	        	Position p = getMedian();
	        	return (p.distanceTo(rmc.getPosition())<MAGIC_DISTANCE); 
	        }
        }
    	return false;
	}
		
	private void resetOnTimeout(long t) {
		if (Math.abs(t-lastTime)>(30*60*1000)) {
			lat_positions.clear();
			lon_positions.clear();
		}
	}
	
	private void addPos(GeoPositionT p) {
		resetOnTimeout(p.getTimestamp());
		lat_positions.add(p.getLatitude());
		lon_positions.add(p.getLongitude());
		while (lat_positions.size()>SIZE) {
			lat_positions.iterator().remove();
			lon_positions.iterator().remove();
		}
	}
	
	private static Double getMedian(Set<Double> set) {
		int m = (int) Math.ceil(set.size()/2);
		Iterator<Double> iter = set.iterator();
		Double res = null;
		for (int i = 0; i<m; i++) res = iter.next();
		return res;
	}
	
	private Position getMedian() {
		double median_lat = getMedian(lat_positions).doubleValue();
		double median_lon = getMedian(lon_positions).doubleValue();
		Position p = new Position(median_lat, median_lon);
		return p;
	}
}
