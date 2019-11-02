package com.aboni.nmea.router.agent.impl.track;

import com.aboni.geo.GeoPositionT;
import net.sf.marineapi.nmea.util.Position;

import java.util.LinkedList;
import java.util.List;

public class PositionStats {

    private final List<GeoPositionT> positions;
	
	private int samples;
	private double avgLat;
	private double avgLon;
	
	private static final long PERIOD = 5L * 60000L; //5 minutes
	
	public PositionStats() {
        positions = new LinkedList<>();
	}
	
	public void addPosition(GeoPositionT pos) {
        synchronized (this) {
            positions.add(pos);
            avgLat = (avgLat * samples + pos.getLatitude()) / (samples + 1);
            avgLon = (avgLon * samples + pos.getLongitude()) / (samples + 1);
            samples = positions.size();

            long t = pos.getTimestamp();
            while (!positions.isEmpty() && (t - positions.get(0).getTimestamp()) > PERIOD) {
                GeoPositionT p = positions.get(0);
                avgLat = (avgLat * samples - p.getLatitude()) / (samples - 1);
                avgLon = (avgLon * samples - p.getLongitude()) / (samples - 1);
                positions.remove(0);
                samples = positions.size();
            }
		}
	}

	public Position getAveragePosition() {
        synchronized (this) {
            return new Position(avgLat, avgLon);
        }
	}
	
}
