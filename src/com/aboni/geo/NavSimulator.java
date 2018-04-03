package com.aboni.geo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Random;

import com.aboni.misc.PolarTable;
import com.aboni.misc.Utils;

import net.sf.marineapi.nmea.util.Position;

public class NavSimulator {
	
	public interface DoWithSim {
		void doIt(NavSimulator sim, long t);
	}

	private long time;
	
	private Position pos;
	private Position posTo;
	
	private int side = 0; // 0.UNKN -1.PORT 1.STARBOARD
	
	private double windSpeed;
	private double windDir;
	
	private double heading;
	private double headingStarboard;
	private double headingPort;
	
	private double brg;
	private double dist;
	
	private double speed = 5.0;
	
	private static long lastTack;
	
	private PolarTable polar;
	
	private double polarAdj = 0.9;
	
	private static double REACH = 55.0;
	
	public NavSimulator() {
	}
	
	public void loadPolars(String file) throws FileNotFoundException, IOException {
		polar = new PolarTable();
		polar.load(new FileReader(new File(file)));
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
	
	public void setHeading(double head) {
		heading = head;
	}
	
	public String getSide() {
		return side==PORT?"P":"S";
	}
	
	private void calcBrg() {
		Course c = new Course(pos, posTo);
		dist = c.getDistance();
		brg = c.getCOG();
	}
	
	public double getWindDir() {
		return windDir;
	}

	public double getWindTrue() {
		return Utils.normalizeDegrees180_180(windDir - heading);
	}

	public double getWindTrueSpeed() {
		return getWindSpeed();
	}
	
	public double getWindApp() {
		ApparentWind a = new ApparentWind(speed, getWindTrue(), getWindSpeed());
		return Utils.normalizeDegrees180_180(a.getApparentWindDeg());
	}
	
	public double getWindAppSpeed() {
		ApparentWind a = new ApparentWind(speed, getWindTrue(), getWindSpeed());
		return a.getApparentWindSpeed();
	}
	
	private static final int PORT = 1;
	private static final int STARBOARD = -1;
	
	private void calcHeadings() {
		double trueWind = Utils.normalizeDegrees180_180(brg - windDir);
		if (Math.abs(trueWind)<REACH) {
			
			headingStarboard = REACH + windDir;
			headingPort = -REACH + windDir;
			double d1 = Math.abs(Utils.normalizeDegrees180_180(headingStarboard - brg)); 
			double d2 = Math.abs(Utils.normalizeDegrees180_180(headingPort - brg));
			
			double newH = d1<d2?headingStarboard:headingPort;
			double newTrue = Utils.normalizeDegrees180_180(newH - windDir);
			int newSide =  newTrue>0?STARBOARD:PORT;
			
			if (side==0) {
				heading = newSide==PORT?headingPort:headingStarboard;
				side = newSide;
			} if (newSide==side) {
				heading = newSide==PORT?headingPort:headingStarboard;
			} else {
				if ((time - lastTack) > 60*60*1000) {
					heading = newSide==PORT?headingPort:headingStarboard;
					side = newSide;
					lastTack = time;
				} else {
					heading = side==PORT?headingPort:headingStarboard;
				}
			} 
		} else {
			headingStarboard = brg;
			headingPort = brg;
			heading = brg;
			side = Utils.normalizeDegrees180_180(heading - windDir)>0?STARBOARD:PORT;
		}
	}
	
	private void calcSpeed() {
		if (polar!=null) {
			speed = polarAdj * polar.getSpeed((int)Math.abs(getWindTrue()), (float)getWindSpeed());
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
		if (getTo()!=null) {
			calcBrg();
			calcHeadings();
		}
		calcSpeed();
	}
	
	public static Position calcNewLL(Position p0, double heading, double dist) {
		Course c = new Course(p0, heading, dist);
		return c.getP1();
	}

	private static Random r = new Random();

	public static Position calcNewLL(Position p0, double heading, double dist, double noiseRadiusMeters) {
		Course c = new Course(p0, heading, dist);
		Position p1 = c.getP1();
		double dNoise = (r.nextDouble() * noiseRadiusMeters / 1852.0);
		double hNoise = (r.nextDouble() * 360.0);
		c = new Course(p1, hNoise, dNoise);
		return c.getP1();
	}

	public PositionHistory doSimulate(DoWithSim oncalc) {
		PositionHistory p = new PositionHistory();
		long t0 = System.currentTimeMillis();
		long dTime = 1 * 60 * 1000; /* 5 minutes*/ 
		double distThreshold = (double)dTime/60d/60d/1000d * getSpeed() * 1.5;
		while (getDistance()>distThreshold) {
			doCalc(getTime() + dTime);
			if (oncalc!=null) oncalc.doIt(this, getTime());
			p.addPosition(new GeoPositionT(t0 + getTime(), getPos()));
		}
		return p;
	}
}


