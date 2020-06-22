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

package com.aboni.nmea.router.processors;

import com.aboni.geo.NMEAMagnetic2TrueConverter;
import com.aboni.geo.TrueWind;
import com.aboni.misc.Utils;
import com.aboni.nmea.router.NMEACache;
import com.aboni.utils.Pair;
import com.aboni.utils.ServerLog;
import net.sf.marineapi.nmea.parser.SentenceFactory;
import net.sf.marineapi.nmea.sentence.*;
import net.sf.marineapi.nmea.util.DataStatus;
import net.sf.marineapi.nmea.util.Units;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;

/**
 * Calc true wind
 *
 * @author aboni
 */
public class NMEAMWVTrue implements NMEAPostProcess {

    @Inject
    public NMEAMWVTrue(@NotNull NMEACache cache, boolean useSOG) {
        this.useRMC = useSOG;
        this.cache = cache;
    }

    private static final long AGE_THRESHOLD = 5000;

    private final NMEACache cache;

    private double lastTrueHeading;
    private double lastMagHeading;
    private long lastHeadingTime;
    private double lastSpeed;
    private long lastSpeedTime;

    private final boolean useRMC;

    private double lastSentTWindSpeed = Double.NaN;

    @Override
    public Pair<Boolean, Sentence[]> process(Sentence sentence, String src) {
        try {
            long time = cache.getNow();
            if (sentence instanceof MWVSentence) {
                return processWind((MWVSentence) sentence, time);
            } else if (!useRMC && sentence instanceof VHWSentence) {
                lastSpeed = ((VHWSentence) sentence).getSpeedKnots();
                lastSpeedTime = time;
            } else if (useRMC && sentence instanceof RMCSentence) {
                lastSpeed = ((RMCSentence) sentence).getSpeed();
                lastSpeedTime = time;
            } else if (sentence instanceof HDMSentence) {
                HDMSentence hdm = (HDMSentence) sentence;
                lastMagHeading = hdm.getHeading();
                lastTrueHeading = new NMEAMagnetic2TrueConverter().getTrue(lastMagHeading);
                lastHeadingTime = time;
            }
            return null;
        } catch (Exception e) {
            ServerLog.getLogger().warning("Cannot enrich wind message {" + sentence + "} error {" + e.getLocalizedMessage() + "}");
        }
        return new Pair<>(Boolean.TRUE, null);
    }

    private Pair<Boolean, Sentence[]> processWind(MWVSentence sentence, long time) {
        if (sentence.isTrue()) {
            // skip it (filter out true wind)
            return new Pair<>(Boolean.FALSE, new Sentence[]{});
        } else if ((time - lastSpeedTime) < AGE_THRESHOLD) {
            // calculate true wind
            TrueWind t = new TrueWind(lastSpeed, sentence.getAngle(), sentence.getSpeed());
            MWVSentence mwvTrue = (MWVSentence) SentenceFactory.getInstance().createParser(TalkerId.II, SentenceId.MWV);
            mwvTrue.setAngle(Utils.normalizeDegrees0To360(t.getTrueWindDeg()));
            mwvTrue.setTrue(true);

            double speed = t.getTrueWindSpeed();
            if (!Double.isNaN(lastSentTWindSpeed)) {
                speed = com.aboni.misc.LPFFilter.getLPFReading(0.75, lastSentTWindSpeed, speed);
            }
            lastSentTWindSpeed = speed;
            mwvTrue.setSpeed(speed);
            mwvTrue.setSpeedUnit(Units.KNOT);
            mwvTrue.setStatus(DataStatus.ACTIVE);

            if ((time - lastHeadingTime) < AGE_THRESHOLD) {
                MWDSentence mwd = (MWDSentence) SentenceFactory.getInstance().createParser(TalkerId.II, SentenceId.MWD);
                mwd.setMagneticWindDirection(Utils.normalizeDegrees0To360(lastMagHeading + mwvTrue.getAngle()));
                mwd.setTrueWindDirection(Utils.normalizeDegrees0To360(lastTrueHeading + mwvTrue.getAngle()));
                mwd.setWindSpeed(Math.round(speed * 51.4444) / 100.0);
                mwd.setWindSpeedKnots(speed);
                return new Pair<>(Boolean.TRUE, new Sentence[]{mwvTrue, mwd});
            } else {
                return new Pair<>(Boolean.TRUE, new Sentence[]{mwvTrue});
            }
        }
        return null;
    }

    @Override
    public void onTimer() {
        // nothing to do here
    }

}
