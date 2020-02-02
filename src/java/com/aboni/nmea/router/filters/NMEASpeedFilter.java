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

    // check if the speed is within gps*factor
    private static final double GPS_TOLERANCE_FACTOR = 2.0;

    // beyond this speed the boat can't possibly go
    private static final double SPEED_UNREASONABLE_THRESHOLD = 30.0;

    // below this speed do not GPS factor
    private static final double SPEED_CHECK_GPS_THRESHOLD = 1.0;

    public NMEASpeedFilter(NMEACache cache) {
        this.cache = cache;
        speedMovingAverage = new SpeedMovingAverage(10000 /* 10 seconds */);
    }

    private boolean checkGPS(double speed) {
        if (speed > SPEED_CHECK_GPS_THRESHOLD) {
            PositionSentence posSentence = cache.getLastPosition().getData();
            if (posSentence instanceof RMCSentence) {
                RMCSentence rmc = (RMCSentence) posSentence;
                return (speed <= (rmc.getSpeed() * GPS_TOLERANCE_FACTOR));
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
        double movingAvg = speedMovingAverage.getAvg();
        if (!Double.isNaN(movingAvg)) {
            return (speed <= (movingAvg * GPS_TOLERANCE_FACTOR));
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
            return (checkThresholds(speed) && checkGPS(speed) && checkMovingAverage(speed));
        } else {
            return true;
        }
    }
}
