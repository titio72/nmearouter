package com.aboni.toolkit;

import com.aboni.geo.NavSimulator;
import com.aboni.geo.PositionHistory;
import com.aboni.geo.Track2GPX;
import com.aboni.misc.Utils;
import net.sf.marineapi.nmea.util.Position;

import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SimulatorGoTo {

	private static final Position CAPRAIA = new Position(43.051326, 9.839279);
	private static final Position MARINA = new Position(43.679416, 10.267679);
	private static final String POLARS = "web/dufour35c.csv";
	private static final String OUTPUT = "out.gpx";

	public static void main(String[] args) {
		Position from = MARINA;
		Position to = CAPRAIA;
		NavSimulator sim = new NavSimulator();
		try {
			sim.loadPolars(POLARS);
		} catch (Exception e) {
			Logger.getGlobal().log(Level.SEVERE, "Error reading polars", e);
		}
		sim.setFrom(from);
		sim.setTo(to);
		sim.setWind(9.0,  205.0);

		Logger.getGlobal().info("BRG  " + sim.getBRG());
		Logger.getGlobal().info("Dist " + sim.getDistance());

		PositionHistory p = sim.doSimulate((NavSimulator s, long t) -> 	{
				Logger.getGlobal().info(() -> String.format(
					"Head %.1f Wind %.1f %.1f %.1f Dist %.2f Speed %.2f %s%n", 
					s.getHeading(), 
					s.getWindDir(),
					s.getWindTrue(),
					s.getWindApp(),
					s.getDistance(), s.getSpeed(), s.getSide()));
				rotateWind(s, t);
			}
		);

		Logger.getGlobal().info(
				(int)(sim.getTime() / 1000d / 60d / 60d) + "h " + ((int)(sim.getTime()/1000d/60d) % 60) + "m " +
						p.getTotalDistance() 
				);
		
		Track2GPX x = new Track2GPX();
		x.setTrack(p);
		try (FileWriter w = new FileWriter(OUTPUT)) {
			x.dump(w);
		} catch (IOException e) {
			Logger.getGlobal().log(Level.SEVERE, "Error dumping GPX", e);
		}
	}

	private static void rotateWind(NavSimulator s, long t) {
		double w = s.getWindDir();
		w = Utils.normalizeDegrees0_360( w + (((double)t/1000d/60d/60d) * 5.0));
		s.setWind(9.0, w);
	}


}
