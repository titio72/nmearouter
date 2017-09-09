package com.aboni.geo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import com.aboni.misc.PolarTable;

import net.sf.geographiclib.Geodesic;
import net.sf.geographiclib.GeodesicData;
import net.sf.marineapi.nmea.util.Position;

public class NavSimulator {

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
	
	public double getWindApp() {
		ApparentWind a = new ApparentWind(speed, getWindTrue(), getWindSpeed());
		return Utils.normalizeDegrees180_180(a.getApparentWindDeg());
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
		calcBrg();
		calcHeadings();
		calcSpeed();
	}
	
	public static Position calcNewLL(Position p0, double heading, double dist) {
		
		GeodesicData d = Geodesic.WGS84.Direct(p0.getLatitude(), p0.getLongitude(), heading, dist * 1852);
		
		return new Position(d.lat2, d.lon2);
	}

	public static void main(String[] args) {
		Position marina = new Position(43.679416, 10.267679);
		Position capraia = new Position(43.051326, 9.839279);
		NavSimulator sim = new NavSimulator();
		try {
			sim.loadPolars("web/dufour35c.csv");
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		sim.setFrom(marina);
		sim.setTo(capraia);
		sim.setWind(9.0,  230.0);
		System.out.println("BRG  " + sim.getBRG());
		System.out.println("Dist " + sim.getDistance());

		PositionHistory p = new PositionHistory();
		long t0 = System.currentTimeMillis();
		
		long dTime = 1 * 60 * 1000; /* 5 minutes*/ 
		
		double distThreshold = (double)dTime/60d/60d/1000d * sim.getSpeed() * 1.5;
		
		while (sim.getDistance()>distThreshold) {
			sim.doCalc(sim.getTime() + dTime);
			
			rotateWind(sim, dTime);
			
			System.out.format(
					"Head %.1f Wind %.1f %.1f %.1f Dist %.2f Speed %.2f %s%n", 
					sim.getHeading(), 
					sim.getWindDir(),
					sim.getWindTrue(),
					sim.getWindApp(),
					sim.getDistance(), sim.getSpeed(), sim.getSide());
			p.addPosition(new GeoPositionT(t0+sim.getTime(), sim.getPos()));
		}
		System.out.println(
				(int)(sim.getTime() / 1000d / 60d / 60d) + "h " + ((int)(sim.getTime()/1000d/60d) % 60) + "m " +
						p.getTotalDistance() 
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

	private static void rotateWind(NavSimulator sim, long dTime) {
		/*double w = sim.getWindDir();
		w = Utils.normalizeDegrees0_360( w + (((double)dTime/1000d/60d/60d) * 5.0));
		sim.setWind(9.0, w);*/
	}

}


