package com.aboni.nmea.router.agent.impl.track;

import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.aboni.geo.GeoPositionT;

public class TrackManagerEx {

	private static final int SIZE = 20;
	private List<GeoPositionT> positions = new LinkedList<>();
	
	public void addPoint(GeoPositionT p) {
		positions.add(p);
		while (positions.size()>SIZE) positions.remove(0);
	}
}
