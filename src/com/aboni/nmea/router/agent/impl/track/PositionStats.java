package com.aboni.nmea.router.agent.impl.track;

import java.util.LinkedList;
import java.util.List;

import com.aboni.geo.GeoPositionT;

import net.sf.marineapi.nmea.util.Position;

public class PositionStats {
	
	private final List<GeoPositionT> positions = new LinkedList<>();
	
	private int samples;
	private double avgLat;
	private double avgLon;
	
	private static final long period = 5 * 60000; //5 minutes
	
	public PositionStats() {}
	
	public void addPosition(GeoPositionT pos) {
		positions.add(pos);
		avgLat = (avgLat * samples + pos.getLatitude()) / (samples + 1);
		avgLon = (avgLon * samples + pos.getLongitude()) / (samples + 1);
		samples = positions.size();
		
		long t = pos.getTimestamp(); 
		while (!positions.isEmpty() && (t - positions.get(0).getTimestamp()) > period) {
			GeoPositionT p = positions.get(0);
			avgLat = (avgLat * samples - p.getLatitude()) / (samples - 1);
			avgLon = (avgLon * samples - p.getLongitude()) / (samples - 1);
			positions.remove(0);
			samples = positions.size();
		}
	}

	public Position getAveragePosition() {
		return new Position(avgLat, avgLon);
	}
	
}
