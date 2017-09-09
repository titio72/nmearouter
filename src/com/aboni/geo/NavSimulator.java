package com.aboni.geo;

import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;

import com.aboni.nmea.router.conf.GPXPlayerAgent;

import net.sf.geographiclib.Geodesic;
import net.sf.geographiclib.GeodesicData;
import net.sf.marineapi.nmea.util.Position;

public class NavSimulator {

	private long time;
	private long prevTime;
	
	private Position pos;
	private Position posTo;
	
	private int side = 0; // 0.UNKN -1.PORT 1.STARBOARD
	
	private double windSpeed;
	private double windDir;
	
	private double heading;
	private double heading1;
	private double heading2;
	
	private double brg;
	private double dist;
	
	private double speed = 5.0;
	
	private static long lastTack;
	
	public NavSimulator() {
	}
	
	public void setFrom(Position pos) {
		this.pos = pos;
		if (pos!=null && posTo!=null) calcBrg(); 
	}
	
	public Position getPos() {
		return pos;
	}
	
	public void setTo(Position to) {
		posTo = to;
		if (pos!=null && posTo!=null) calcBrg(); 
	}

	public Position getTo() {
		return posTo;
	}
	
	public long getTime() {
		return time;
	}
	
	public void setWind(double speedKn, double fromDirectionDeg) {
		windDir = fromDirectionDeg;
		windSpeed = speedKn;
	}

	public double getWindSpeed() {
		return windSpeed;
	}
	
	public double getHeading() {
		return heading;
	}
	
	private void calcBrg() {
		Course c = new Course(pos, posTo);
		dist = c.getDistance();
		brg = c.getCOG();
	}
	
	private void calcHeadings() {
		double trueWind = Utils.normalizeDegrees180_180(windDir - brg);
		if (Math.abs(trueWind)<45.0) {
			
			heading1 = 45 + windDir;
			heading2 = -45 + windDir;
			double d1 = Math.abs(Utils.normalizeDegrees180_180(heading1 - brg)); 
			double d2 = Math.abs(Utils.normalizeDegrees180_180(heading2 - brg));
			
			double newHeading = d1<d2?heading1:heading2;
			int newSide =  d1<d2?-1:1;
			if (side==0) {
				heading = newHeading;
				side = newSide;
			} if (newSide==side) {
				heading = newHeading;
			} else {
				if ((time - lastTack) > 60*60*1000) {
					heading = newHeading;
					side = newSide;
					lastTack = time;
				}
			}
		} else {
			heading1 = brg;
			heading2 = brg;
			heading = brg;
		}
	}
		
	public double getSpeed() {
		return speed;
	}
	
	public double getDistance() {
		return dist;
	}
	
	public double getBRG() {
		return brg;
	}
	
	public void doCalc(long time) {
		Position newPos = calcNewLL(pos, heading, speed * (double)(time - this.time)/1000d/60d/60d);
		this.time = time;
		pos = newPos;
		calcBrg();
		calcHeadings();
	}
	
	public static Position calcNewLL(Position p0, double heading, double dist) {
		
		GeodesicData d = Geodesic.WGS84.Direct(p0.getLatitude(), p0.getLongitude(), heading, dist * 1852);
		
		return new Position(d.lat2, d.lon2);
	}

	public static void main(String[] args) {
		Position marina = new Position(43.679416, 10.267679);
		Position capraia = new Position(43.051326, 9.839279);
		NavSimulator sim = new NavSimulator();
		sim.setFrom(marina);
		sim.setTo(capraia);
		sim.setWind(11.0,  230.0);
		System.out.println("BRG  " + sim.getBRG());
		System.out.println("Dist " + sim.getDistance());

		double d = sim.getDistance();
		
		PositionHistory p = new PositionHistory();
		long t0 = System.currentTimeMillis();
		
		while (d>0.1) {
			sim.doCalc(sim.getTime() + 5 * 60 * 1000 /* 1 minutes*/);
			d = sim.getDistance();
			
			sim.setWind(11.0, sim.getWindDir() - ((double)sim.getTime()/1000d/60d/60d) * 10.0);
			
			System.out.println("Head " + (int)sim.getHeading() + " dist " + d);
			p.addPosition(new GeoPositionT(t0+sim.getTime(), sim.getPos()));
		}
		System.out.println(
				(int)(sim.getTime() / 1000d / 60d / 60d) + "h " + ((int)(sim.getTime()/1000d/60d) % 60) + "m" 
				);
		
		Track2GPX x = new Track2GPX();
		x.setTrack(p);
		try {
			FileWriter w = new FileWriter("out.gpx");
			x.dump(w);
			w.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private double getWindDir() {
		return windDir;
	}
	
	
	
	/*
	public double heading = -150;
	public double speed = 5;
	public double lat = 43.67830115349512;
	public double lon = 10.266444683074951;
	public double sog;
	public double cog;
	
	private long t = 0;

	
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
	*/
}


