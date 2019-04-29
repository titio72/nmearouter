package com.aboni.nmea.router.filters;

import com.aboni.nmea.sentences.NMEASentenceFilter;
import com.aboni.nmea.sentences.NMEAUtils;
import com.aboni.utils.ServerLog;
import net.sf.marineapi.nmea.sentence.RMCSentence;
import net.sf.marineapi.nmea.sentence.Sentence;
import net.sf.marineapi.nmea.util.Position;

import java.util.*;

public class NMEAPositionFilter implements NMEASentenceFilter {
	
	private static final int 	RESET_TIMEOUT = 5*60000; 	// 5 minutes
	private static final int 	SPEED_GATE = 35; 			// Kn - if faster reject
	private static final int 	MAGIC_DISTANCE = 15; 		// Points farther from the median of samples will be discarded
	private static final double SMALL_MAGIC_DISTANCE = 0.5; // Points farther from the last valid point will be discarded
	private static final int 	SIZE = 30; 					// ~30s of samples
	
	private final List<Position> positions = new LinkedList<>();
	private Position lastValid;
	private long lastTime = 0; 
	
	private final FilterStats stats;

	private class FilterStats {
		int totProcessed;
		int totSkippedReverseTime;
		int totSkippedExceedDistance;
		int totSkippedExceedSpeed;
		int totInvalid;
		int medianRecalc;
		int q;
		
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
			return String.format("Ok {%d} Q {%d} XInv {%d} Xtime {%d} Xdist {%d} Xspeed {%d} MCalc {%d}", 
					totProcessed, q, totInvalid, totSkippedReverseTime, 
					totSkippedExceedDistance, totSkippedExceedSpeed,
					medianRecalc);
		}
	}
	
	public NMEAPositionFilter() {
		stats = new FilterStats();
	}
	
	private boolean ready() {
		return positions.size()==SIZE;
	}
	
	public boolean acceptPoint(RMCSentence rmc) {
		synchronized (stats) {
			if (rmc.isValid()) {
				Position pos = NMEAUtils.getPosition(rmc);
		        if (pos!=null) {
					if (checkPosition(rmc)) return true;
				} else {
		        	stats.totInvalid++;
		        }
	        } else {
	        	stats.totInvalid++;
	        }
			stats.q = positions.size();
	    	return false;
		}
	}

	private boolean checkPosition(RMCSentence rmc) {
		Calendar timestamp = NMEAUtils.getTimestampOptimistic(rmc);
		if (timestamp!=null && timestamp.getTimeInMillis()>lastTime) {
			if (rmc.getSpeed()<SPEED_GATE) {
				resetOnTimeout(timestamp.getTimeInMillis());
				lastTime = timestamp.getTimeInMillis();
				addPos(rmc.getPosition());
				if (ready()) {
					if (checkDistance(rmc.getPosition())) {
						stats.totProcessed++;
						stats.q = positions.size();
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
		return false;
	}

	private boolean checkDistance(Position p) {
		boolean valid = false;
		if (lastValid==null) {
			Position pMedian = getMedian();
			if (pMedian!=null) {
				double d = pMedian.distanceTo(p) / 1852; 
				if (d<MAGIC_DISTANCE) {
					lastValid = p;
					valid = true;
				}
			}
		} else {
			double d = lastValid.distanceTo(p) / 1852; 
			if (d<SMALL_MAGIC_DISTANCE) {
				lastValid = p;
				valid = true;
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
		return lat.get(SIZE/2);
	}
	
	private Position getMedian() {
		if (ready()) {
			stats.medianRecalc++;
	 		return new Position(getMedian(true), getMedian(false));
		} else {
			return null;
		}
	}
	
	public void dumpStats() {
		synchronized (stats) {
			ServerLog.getLogger().Info("RMCFilter " + stats);
			stats.reset();
		}
	}

	@Override
	public boolean match(Sentence s, String src) {
		if (s instanceof RMCSentence) {
			return acceptPoint((RMCSentence)s);
		} else {
			return true;
		}
	}
}
