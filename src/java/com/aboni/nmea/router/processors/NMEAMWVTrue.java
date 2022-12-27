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

import com.aboni.geo.TrueWind;
import com.aboni.nmea.router.TimestampProvider;
import com.aboni.nmea.router.message.Message;
import com.aboni.nmea.router.message.MsgSOGAdCOG;
import com.aboni.nmea.router.message.MsgSpeed;
import com.aboni.nmea.router.message.MsgWindData;
import com.aboni.nmea.router.message.impl.MsgWindDataImpl;
import com.aboni.utils.LPFFilter;
import com.aboni.utils.Pair;
import com.aboni.utils.Utils;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;

/**
 * Calc true wind
 *
 * @author aboni
 */
public class NMEAMWVTrue implements NMEAPostProcess {

    @Inject
    public NMEAMWVTrue(@NotNull TimestampProvider timestampProvider, boolean useSOG) {
        this.useSOG = useSOG;
        this.timestampProvider = timestampProvider;
    }

    private static final long AGE_THRESHOLD = 5000;

    private final TimestampProvider timestampProvider;

    private double lastSpeed;
    private long lastSpeedTime;

    private final boolean useSOG;

    private double lastSentTWindSpeed = Double.NaN;

    @Override
    public Pair<Boolean, Message[]> process(Message message, String src) throws NMEARouterProcessorException {
        try {
            long time = timestampProvider.getNow();
            if (message instanceof MsgWindData) {
                return processWind((MsgWindData) message, time);
            } else if (!useSOG && message instanceof MsgSpeed) {
                lastSpeed = ((MsgSpeed) message).getSpeedWaterRef();
                lastSpeedTime = time;
            } else if (useSOG && message instanceof MsgSOGAdCOG) {
                lastSpeed = ((MsgSOGAdCOG) message).getSOG();
                lastSpeedTime = time;
            }
        } catch (Exception e) {
            throw new NMEARouterProcessorException("Error processing sentence \"" + message + "\"", e);
        }
        return new Pair<>(Boolean.TRUE, null);
    }

    private Pair<Boolean, Message[]> processWind(MsgWindData windMessage, long time) {
        if (windMessage.isTrue()) {
            // skip it (filter out true wind)
            return new Pair<>(Boolean.FALSE, new Message[]{});
        } else if ((time - lastSpeedTime) < AGE_THRESHOLD) {
            // calculate true wind
            TrueWind t = new TrueWind(lastSpeed, windMessage.getAngle(), windMessage.getSpeed());

            double angle = Utils.normalizeDegrees0To360(t.getTrueWindDeg());
            double speed = t.getTrueWindSpeed();
            if (!Double.isNaN(lastSentTWindSpeed)) {
                speed = LPFFilter.getLPFReading(0.75, lastSentTWindSpeed, speed);
            }
            lastSentTWindSpeed = speed;
            MsgWindData trueWindMsg = new MsgWindDataImpl(speed, angle, false);
            return new Pair<>(Boolean.TRUE, new Message[]{trueWindMsg});
        }
        return new Pair<>(Boolean.TRUE, new Message[]{});
    }

    @Override
    public void onTimer() {
        // nothing to do here
    }
}
