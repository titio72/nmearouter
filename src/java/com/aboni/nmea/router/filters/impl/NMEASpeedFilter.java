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

import com.aboni.data.SpeedMovingAverage;
import com.aboni.nmea.message.Message;
import com.aboni.nmea.message.MsgSOGAdCOG;
import com.aboni.nmea.message.MsgSpeed;
import com.aboni.nmea.router.NMEACache;
import com.aboni.nmea.router.RouterMessage;
import com.aboni.nmea.router.filters.NMEAFilter;
import org.json.JSONObject;

import javax.inject.Inject;

public class NMEASpeedFilter implements NMEAFilter {


    public static final String FILTER_TYPE = "speed";
    private final NMEACache cache;
    private final SpeedMovingAverage speedMovingAverage;
    private static final boolean USE_GPS = false;
    private static final boolean USE_AVERAGE = false;

    // check if the speed is within gps*factor
    private static final double SPEED_TOLERANCE_FACTOR = 2.0;

    // beyond this speed the boat can't possibly go
    private static final double SPEED_UNREASONABLE_THRESHOLD = 30.0;

    // below this speed do not use GPS factor
    private static final double SPEED_CHECK_GPS_THRESHOLD = 2.5;

    @Inject
    public NMEASpeedFilter(NMEACache cache) {
        this.cache = cache;
        if (cache==null) throw new IllegalArgumentException("Cache cannot be null");
        speedMovingAverage = new SpeedMovingAverage(10000 /* 10 seconds */);
    }

    private boolean checkGPS(double speed) {
        if (USE_GPS && speed > SPEED_CHECK_GPS_THRESHOLD) {
            MsgSOGAdCOG vector = cache.getLastVector().getData();
            if (vector!=null && !Double.isNaN(vector.getSOG())) {
                return (speed <= (vector.getSOG() * SPEED_TOLERANCE_FACTOR));
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
    public boolean match(RouterMessage m) {
        Message s = m.getPayload();
        if (s instanceof MsgSpeed) {
            MsgSpeed msgSpeed = (MsgSpeed) s;
            double speed = msgSpeed.getSpeedWaterRef();
            if (Double.isNaN(speed)) {
                return false;
            } else {
                speedMovingAverage.setSample(System.currentTimeMillis(), speed);
                return checkThresholds(speed)
                        && checkGPS(speed)
                        && checkMovingAverage(speed);
            }
        } else {
            return true;
        }
    }

    @Override
    public JSONObject toJSON() {
        return JSONFilterUtils.createFilter(this, null);
    }

    public static NMEASpeedFilter parseFilter(JSONObject obj, NMEACache cache) {
        // throw an exception is the JSON filter is not right
        JSONFilterUtils.getFilter(obj, FILTER_TYPE);
        return new NMEASpeedFilter(cache);
    }
}
