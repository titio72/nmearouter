package com.aboni.nmea.router.agent.impl.track;

import java.util.LinkedList;
import java.util.List;

import com.aboni.geo.GeoPositionT;

public class Boh {

	private static final double SPEED_THRESHOLD = 0.67;
	private long slowSince; 
	private List<GeoPositionT> positions = new LinkedList<>();
	
	public void setPos(GeoPositionT p, double speed) {
		if (speed<SPEED_THRESHOLD) {
			long now = p.getTimestamp();
			if (slowSince==0) slowSince = now;
			positions.add(p);
			GeoPositionT p0 = positions.get(0);
			while (p0!=null && p0.getTimestamp()< (now - (1000*60*10) /*10 minutes*/)) {
				positions.remove(0);
				p0 = positions.get(0);
			}
			
		} else {
			slowSince = 0;
			positions.clear();
		}
	}
	
	
}
