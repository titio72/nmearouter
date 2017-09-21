package com.aboni.nmea.router.batch;

import java.io.FileWriter;
import java.io.IOException;

import com.aboni.geo.NavSimulator;
import com.aboni.geo.PositionHistory;
import com.aboni.geo.Track2GPX;
import com.aboni.geo.Utils;

import net.sf.marineapi.nmea.util.Position;

public class SimulatorGoTo {
	
	public static void main(String[] args) {
		Position marina = new Position(43.679416, 10.267679);
		Position capraia = new Position(43.051326, 9.839279);
		NavSimulator sim = new NavSimulator();
		try { sim.loadPolars("web/dufour35c.csv"); } catch (Exception e) { e.printStackTrace(); }
		sim.setFrom(marina);
		sim.setTo(capraia);
		sim.setWind(9.0,  205.0);
		
		System.out.println("BRG  " + sim.getBRG());
		System.out.println("Dist " + sim.getDistance());

		PositionHistory p = sim.doSimulate((NavSimulator s, long t) -> 	{
				System.out.format(
					"Head %.1f Wind %.1f %.1f %.1f Dist %.2f Speed %.2f %s%n", 
					s.getHeading(), 
					s.getWindDir(),
					s.getWindTrue(),
					s.getWindApp(),
					s.getDistance(), s.getSpeed(), s.getSide());
				rotateWind(s, t);
			}
		);

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
			e.printStackTrace();
		}
	}

	private static void rotateWind(NavSimulator s, long t) {
		double w = s.getWindDir();
		w = Utils.normalizeDegrees0_360( w + (((double)t/1000d/60d/60d) * 5.0));
		s.setWind(9.0, w);
	}


}
