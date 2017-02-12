package com.aboni.geo;

import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;

import net.sf.geographiclib.Geodesic;
import net.sf.geographiclib.GeodesicData;
import net.sf.marineapi.nmea.util.Position;

public class NavSimulator {

	public double heading = -150;
	public double speed = 5;
	public double lat = 43.67830115349512;
	public double lon = 10.266444683074951;
	public double sog;
	public double cog;
	
	private long t = 0;
	
	private Position calcNewLL(Position p0, double heading, double dist) {
		
		GeodesicData d = Geodesic.WGS84.Direct(p0.getLatitude(), p0.getLongitude(), heading, dist * 1852);
		
		return new Position(d.lat2, d.lon2);
	}
	
	public void calc(long t1) {

		if (t==t1) return;
		
		double distance = speed *  ((double)(t1 - t)/(60.0*60.0*1000.0));
		Position p0 = new Position(lat, lon);
		Position p1 = calcNewLL(p0, heading, distance);

		
		heading = heading + (Math.sin(t/10) * 5.0);
		speed = speed + (Math.cos(t) * 0.5);
		
		Course c = new Course(p0, p1);
		cog = c.getCOG();
		sog = c.getDistance() / ((double)(t1-t) / 1000.0 / 60.0 / 60.0);
		
		lat = p1.getLatitude();
		lon = p1.getLongitude();
		
		t = t1;

	}

	public static void main(String[] args) {
		NavSimulator n = new NavSimulator();
		double lat = n.lat;
		double lon = n.lon;
		
		int interval = 500;

		PositionHistory p = new PositionHistory();
		
		for (long t = 0; t<=(1*60*60*1000); t+=interval) {
			n.calc(t);
			System.out.println(DateFormat.getTimeInstance(DateFormat.SHORT).format(new Date(n.t)));
			System.out.println(n.lat);
			System.out.println(n.lon);
			System.out.println(n.sog);
			System.out.println(n.cog);
			System.out.println("-------------------------------");
			p.addPosition(new GeoPositionT(t * 1000, n.lat, n.lon));
			
		}
		
		Course c = new Course(new Position(lat, lon), new Position(n.lat, n.lon));
		System.out.println(c.getDistance());
		
		TrackDumper gpx = new Track2GPX();
		gpx.setTrack(p);
		try {
			gpx.dump(new FileWriter("x.gpx"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}


