/*
(C) 2020, Andrea Boni
This file is part of NMEARouter.
NMEARouter is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.
NMEARouter is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.
You should have received a copy of the GNU General Public License
along with NMEARouter.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.aboni.nmea.router.filters.impl;

import com.aboni.nmea.router.filters.NMEAFilter;
import com.aboni.nmea.sentences.NMEAUtils;
import com.aboni.utils.ServerLog;
import net.sf.marineapi.nmea.sentence.RMCSentence;
import net.sf.marineapi.nmea.sentence.Sentence;
import net.sf.marineapi.nmea.util.Position;

import javax.inject.Inject;
import java.util.*;

public class NMEAPositionFilter implements NMEAFilter {

    private static final int RESET_TIMEOUT = 5 * 60000;    // 5 minutes
    private static final int SPEED_GATE = 35;            // Kn - if faster reject
    private static final int MAGIC_DISTANCE = 15;        // Points farther from the median of samples will be discarded
    private static final double SMALL_MAGIC_DISTANCE = 0.5; // Points farther from the last valid point will be discarded
    private static final int SIZE = 30;                    // ~30s of samples

    private final List<Position> positions = new LinkedList<>();
    private Position lastValid;
    private long lastTime = 0;

    private final FilterStats stats;

    private static class FilterStats {
        int totProcessed;
        int totSkippedReverseTime;
        int totSkippedExceedDistance;
        int totSkippedExceedSpeed;
        int totInvalid;
        int medianRecalculation;
        int q;

        void reset() {
            totInvalid = 0;
            totProcessed = 0;
            totSkippedReverseTime = 0;
            totSkippedExceedDistance = 0;
            totSkippedExceedSpeed = 0;
            medianRecalculation = 0;
        }

        @Override
        public String toString() {
            return String.format("Ok {%d} Q {%d} XInv {%d} XTime {%d} XDist {%d} XSpeed {%d} MCalc {%d}",
                    totProcessed, q, totInvalid, totSkippedReverseTime,
                    totSkippedExceedDistance, totSkippedExceedSpeed,
                    medianRecalculation);
        }
    }

    @Inject
    public NMEAPositionFilter() {
        stats = new FilterStats();
    }

    private boolean ready() {
        return positions.size() == SIZE;
    }

    private boolean acceptPoint(RMCSentence rmc) {
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
            stats.medianRecalculation++;
            return new Position(getMedian(true), getMedian(false));
        } else {
            return null;
        }
    }

    public void dumpStats() {
        synchronized (stats) {
            ServerLog.getLogger().info("RMCFilter " + stats);
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
