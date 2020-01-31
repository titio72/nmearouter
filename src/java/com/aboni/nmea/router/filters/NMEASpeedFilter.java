package com.aboni.nmea.router.filters;

import com.aboni.misc.SpeedMovingAverage;
import com.aboni.nmea.router.NMEACache;
import com.aboni.nmea.sentences.NMEASentenceFilter;
import net.sf.marineapi.nmea.sentence.PositionSentence;
import net.sf.marineapi.nmea.sentence.RMCSentence;
import net.sf.marineapi.nmea.sentence.Sentence;
import net.sf.marineapi.nmea.sentence.VHWSentence;

public class NMEASpeedFilter implements NMEASentenceFilter {

    private NMEACache cache;
    private SpeedMovingAverage speedMovingAverage;
    private boolean useGPS = true;
    private boolean useAverage = false;

    // check if the speed is within gps*factor
    private static final double SPEED_TOLERANCE_FACTOR = 2.0;

    // beyond this speed the boat can't possibly go
    private static final double SPEED_UNREASONABLE_THRESHOLD = 30.0;

    // below this speed do not use GPS factor
    private static final double SPEED_CHECK_GPS_THRESHOLD = 2.5;

    public NMEASpeedFilter(NMEACache cache) {
        this.cache = cache;
        speedMovingAverage = new SpeedMovingAverage(10000 /* 10 seconds */);
    }

    private boolean checkGPS(double speed) {
        if (useGPS && speed > SPEED_CHECK_GPS_THRESHOLD) {
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
        if (useAverage) {
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
            if (checkThresholds(speed)) {
                if (checkGPS(speed)) {
                    return (checkMovingAverage(speed));
                }
            }
            return false;
        } else {
            return true;
        }
    }
}
