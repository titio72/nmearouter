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

import com.aboni.nmea.router.RouterMessage;
import com.aboni.nmea.router.filters.NMEAFilter;
import com.aboni.nmea.router.message.Message;
import com.aboni.nmea.router.message.MsgPositionAndVector;
import com.aboni.utils.Log;
import com.aboni.utils.LogStringBuilder;
import net.sf.marineapi.nmea.util.Position;

import javax.inject.Inject;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

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

    private boolean acceptPoint(MsgPositionAndVector rmc) {
        synchronized (stats) {
            if (rmc.isValid()) {
                return checkPosition(rmc);
            } else {
                stats.totInvalid++;
            }
            stats.q = positions.size();
            return false;
        }
    }

    private boolean checkPosition(MsgPositionAndVector rmc) {
        Instant timestamp = rmc.getTimestamp();
        if (timestamp != null && timestamp.toEpochMilli() > lastTime) {
            if (rmc.getSOG() < SPEED_GATE) {
                resetOnTimeout(timestamp.toEpochMilli());
                lastTime = timestamp.toEpochMilli();
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

    public void dumpStats(Log log) {
        if (log != null) {
            synchronized (stats) {
                LogStringBuilder.start("PositionFilter").wO("stats").w(stats.toString()).info(log);
                stats.reset();
            }
        }
    }

    @Override
    public boolean match(RouterMessage m) {
        Message s = m.getMessage();
        if (s instanceof MsgPositionAndVector) {
            return acceptPoint((MsgPositionAndVector) s);
        } else {
            return true;
        }
    }
}
