package com.aboni.nmea.router.agent.impl.track;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.aboni.nmea.sentences.NMEAUtils;
import com.aboni.utils.ServerLog;

import net.sf.marineapi.nmea.sentence.RMCSentence;
import net.sf.marineapi.nmea.util.Position;

public class PositionFilter {
	
	private static final int RESET_TIMEOUT = 5*60000; 	// 5 minutes
	private static final int SPEED_GATE = 25; 			// 25Kn - if faster reject
	private static final int MAGIC_DISTANCE = 15; 		// 15NM
	private static final int SMALL_MAGIC_DISTANCE = 2; 	// 2NM
	private static final int SIZE = 30; 				// ~30s of samples
	private List<Position> positions = new LinkedList<>();
	private Position lastValid;
	private long lastTime = 0; 
	private FilterStats stats = new FilterStats();

	private class FilterStats {
		int totProcessed;
		int totSkippedReverseTime;
		int totSkippedExceedDistance;
		int totSkippedExceedSpeed;
		int totInvalid;
		int medianRecalc;
		
		void reset() {
			totInvalid = 0;
			totProcessed = 0;
			totSkippedReverseTime = 0;
			totSkippedExceedDistance = 0;
			totSkippedExceedSpeed = 0;
			medianRecalc = 0;
		}
		
		@Override
		public String toString() {
			return String.format("T {%d} Invalid {%d} RevTime {%d} Xd {%d} Xs {%d} M {%d}", 
					totProcessed, totInvalid, totSkippedReverseTime, 
					totSkippedExceedDistance, totSkippedExceedSpeed,
					medianRecalc);
		}
	}
	
	
	private boolean ready() {
		return positions.size()==SIZE;
	}
	
	public boolean acceptPoint(RMCSentence rmc) {
		synchronized (stats) {
			if (rmc.isValid()) {
				Position pos = NMEAUtils.getPosition(rmc);
		        if (pos!=null) {
		            Calendar timestamp = NMEAUtils.getTimestampOptimistic(rmc);
		            if (timestamp!=null && timestamp.getTimeInMillis()>lastTime) {
		            	if (rmc.getSpeed()<SPEED_GATE) {
			        		resetOnTimeout(timestamp.getTimeInMillis());
			        		lastTime = timestamp.getTimeInMillis();
			                addPos(rmc.getPosition());
			    	        if (ready()) {
			    	        	if (checkDistance(rmc.getPosition())) {
			    	        		stats.totProcessed++;
			    	        		return true; 
			    	        	} else {
			    	        		stats.totSkippedExceedDistance++;
			    	        	}
			    	        }
		            	} else {
		            		stats.totSkippedExceedSpeed++;
		            	}
		            } else {
		            	stats.totSkippedReverseTime++;
		            }
		        } else {
		        	stats.totInvalid++;
		        }
	        } else {
	        	stats.totInvalid++;
	        }
	    	return false;
		}
	}

	private boolean checkDistance(Position p) {
		boolean valid = false;
		if (lastValid==null) {
			Position pMedian = getMedian();
			if (pMedian!=null) {
				if (pMedian.distanceTo(p)<MAGIC_DISTANCE) {
					lastValid = p;
					valid = true;
				}
			}
		} else {
			if (lastValid.distanceTo(p)<SMALL_MAGIC_DISTANCE) {
				lastValid = p;
				valid = true;
			} else {
				lastValid = null;
			}
		}
		return valid;
	}
	
	
	private void resetOnTimeout(long t) {
		if (Math.abs(t-lastTime)>RESET_TIMEOUT) {
			positions.clear();
			lastValid = null;
		}
	}
	
	private void addPos(Position pos) {
		positions.add(pos);
		while (positions.size()>SIZE) {
			positions.remove(0);
		}		
	}
	
	private double getMedian(boolean isLat) {
		List<Double> lat = new ArrayList<>(SIZE);
		for (Position p: positions) lat.add(isLat?p.getLatitude():p.getLongitude());
		Collections.sort(lat);
		double medianL = lat.get(SIZE/2);
		return medianL;
	}
	
	private Position getMedian() {
		if (ready()) {
			stats.medianRecalc++;
	 		double median_lat = getMedian(true);
			double median_lon = getMedian(false);
			Position p = new Position(median_lat, median_lon);
			return p;
		} else {
			return null;
		}
	}
	
	public void dumpStats() {
		synchronized (stats) {
			ServerLog.getLogger().Info("RMCFilter" + stats);
			System.out.println(stats);
			stats.reset();
		}
	}
}
