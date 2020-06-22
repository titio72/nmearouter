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

import com.aboni.misc.SpeedMovingAverage;
import com.aboni.nmea.router.NMEACache;
import com.aboni.nmea.sentences.NMEASentenceFilter;
import net.sf.marineapi.nmea.sentence.PositionSentence;
import net.sf.marineapi.nmea.sentence.RMCSentence;
import net.sf.marineapi.nmea.sentence.Sentence;
import net.sf.marineapi.nmea.sentence.VHWSentence;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;

public class NMEASpeedFilter implements NMEASentenceFilter {

    private final NMEACache cache;
    private final SpeedMovingAverage speedMovingAverage;
    private static final boolean USE_GPS = true;
    private static final boolean USE_AVERAGE = false;

    // check if the speed is within gps*factor
    private static final double SPEED_TOLERANCE_FACTOR = 2.0;

    // beyond this speed the boat can't possibly go
    private static final double SPEED_UNREASONABLE_THRESHOLD = 30.0;

    // below this speed do not use GPS factor
    private static final double SPEED_CHECK_GPS_THRESHOLD = 2.5;

    @Inject
    public NMEASpeedFilter(@NotNull NMEACache cache) {
        this.cache = cache;
        speedMovingAverage = new SpeedMovingAverage(10000 /* 10 seconds */);
    }

    private boolean checkGPS(double speed) {
        if (USE_GPS && speed > SPEED_CHECK_GPS_THRESHOLD) {
            PositionSentence posSentence = cache.getLastPosition().getData();
            if (posSentence instanceof RMCSentence) {
                RMCSentence rmc = (RMCSentence) posSentence;
                return (speed <= (rmc.getSpeed() * SPEED_TOLERANCE_FACTOR));
            } else {
                return true;
            }
        } else {
            return true;
        }
    }

    private boolean checkThresholds(double speed) {
        return speed < SPEED_UNREASONABLE_THRESHOLD;
    }

    private boolean checkMovingAverage(double speed) {
        if (USE_AVERAGE) {
            double movingAvg = speedMovingAverage.getAvg();
            if (!Double.isNaN(movingAvg)) {
                return (speed <= (movingAvg * SPEED_TOLERANCE_FACTOR));
            } else {
                return true;
            }
        } else {
            return true;
        }
    }

    @Override
    public boolean match(Sentence s, String src) {
        if (s instanceof VHWSentence) {
            VHWSentence vhw = (VHWSentence) s;
            double speed = vhw.getSpeedKnots();
            speedMovingAverage.setSample(System.currentTimeMillis(), speed);
            return checkThresholds(speed)
                    && checkGPS(speed)
                    && checkMovingAverage(speed);
        } else {
            return true;
        }
    }
}
