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

public class PositionFilter implements NMEAFilter {

    private static final int RESET_TIMEOUT = 5 * 60000;    // 5 minutes
    private static final int SPEED_GATE = 35;            // Kn - if faster reject
    private static final int MAGIC_DISTANCE = 15;        // Points farther from the median of samples will be discarded
    private static final double SMALL_MAGIC_DISTANCE = 0.3; // Points farther from the last valid point will be discarded
    private static final int SIZE = 30;                    // ~30s of samples

    private final List<Position> positions = new LinkedList<>();
    private final int queueSize;
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
    public PositionFilter() {
        this(SIZE);
    }

    public PositionFilter(int queueSize) {
        stats = new FilterStats();
        this.queueSize = queueSize;
    }

    public static double getMaxIncrementalDistance() {
        return SMALL_MAGIC_DISTANCE;
    }

    public static double getMaxDistanceFromMedian() {
        return MAGIC_DISTANCE;
    }

    public static double getMaxAllowedSpeed() {
        return SPEED_GATE;
    }

    public boolean isReady() {
        return positions.size() == queueSize;
    }

    private static boolean isValid(MsgPositionAndVector message) {
        // duplicate some of the tests in MsgPositionAndVector but we should not rely on the implementation of the message class.
        // The concept of "valid" may be different in this context.
        return message != null && message.isValid() && message.getPosition() != null && message.getTimestamp() != null
                && !Double.isNaN(message.getCOG()) && !Double.isNaN(message.getSOG());
    }

    /**
     * Accept or reject the a position message, basing the decision on speed, timing and distance.
     * History is also considered so to avoid "jumps".
     * Use this method for testing - when integrated the entry point is the NMEAFilter interface.
     *
     * @param positionAndVector The message containing position, time and vector.
     * @return True if the position is to be accepted or false otherwise.
     */
    public boolean acceptPoint(MsgPositionAndVector positionAndVector) {
        synchronized (stats) {
            resetOnTimeout(positionAndVector);
            if (isValid(positionAndVector)) {
                return checkPosition(positionAndVector);
            } else {
                stats.totInvalid++;
            }
            stats.q = positions.size();
            return false;
        }
    }

    private boolean checkPosition(MsgPositionAndVector positionAndVector) {
        addPos(positionAndVector.getPosition());
        long timestamp = positionAndVector.getTimestamp().toEpochMilli();
        if (timestamp > lastTime) {
            lastTime = timestamp;
            if (positionAndVector.getSOG() < SPEED_GATE) {
                if (isReady()) {
                    if (checkDistance(positionAndVector.getPosition())) {
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
        boolean valid = (lastValid == null) ?
                /* no last valid position - check against the median of the last N seconds */
                checkDistanceConstraints(getMedian(), p, MAGIC_DISTANCE) :
                /* last position is valid, check that the distance is not too great */
                checkDistanceConstraints(lastValid, p, SMALL_MAGIC_DISTANCE);
        lastValid = valid ? p : null;
        return valid;
    }

    private boolean checkDistanceConstraints(Position prev, Position current, double distance) {
        if (prev == null) return false;
        else return (prev.distanceTo(current) / 1852) < distance;
    }

    /**
     * When no valid positions come through in RESET_TIMEOUT wipe out the position vector used to calc the median and
     * blank out the reference to the last valid position.
     * This will trigger starting fresh.
     *
     * @param positionAndVector The current message.
     */
    private void resetOnTimeout(MsgPositionAndVector positionAndVector) {
        Instant t = positionAndVector.getTimestamp();
        if (t == null || Math.abs(t.toEpochMilli() - lastTime) > RESET_TIMEOUT) {
            positions.clear();
            lastValid = null;
        }
    }

    private void addPos(Position pos) {
        positions.add(pos);
        while (positions.size() > SIZE) {
            positions.remove(0);
        }
    }

    private double getMedian(boolean isLat) {
        List<Double> list = new ArrayList<>(SIZE);
        for (Position p : positions) list.add(isLat ? p.getLatitude() : p.getLongitude());
        Collections.sort(list);
        return list.get(SIZE/2);
    }

    private Position getMedian() {
        if (isReady()) {
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
