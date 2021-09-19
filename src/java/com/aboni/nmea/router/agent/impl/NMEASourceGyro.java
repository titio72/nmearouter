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

package com.aboni.nmea.router.agent.impl;

import com.aboni.geo.DeviationManager;
import com.aboni.geo.NMEAMagnetic2TrueConverter;
import com.aboni.misc.Utils;
import com.aboni.nmea.router.NMEACache;
import com.aboni.nmea.router.OnRouterMessage;
import com.aboni.nmea.router.RouterMessage;
import com.aboni.nmea.router.TimestampProvider;
import com.aboni.nmea.router.message.MsgAttitude;
import com.aboni.nmea.router.message.MsgHeading;
import com.aboni.nmea.router.message.impl.MsgAttitudeImpl;
import com.aboni.sensors.*;
import com.aboni.utils.HWSettings;
import com.aboni.utils.LogAdmin;
import com.aboni.utils.LogStringBuilder;
import net.sf.marineapi.nmea.parser.SentenceFactory;
import net.sf.marineapi.nmea.sentence.*;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class NMEASourceGyro extends NMEAAgentImpl {

    /**
     * After 1m no HDx sentence appear on the stream the sensor start providing its own.
     * This is in case the boat can provide heading values (AP, boat compass etc.).
     */
    private static final long SEND_HD_IDLE_TIME = 15L * 1000L; //ms

    private static final boolean USE_CMPS11 = true;
    public static final String GYRO_AGENT_CATEGORY = "GyroAgent";

    private SensorCompass compassSensor;

    private final DeviationManager deviationManager;
    private final TimestampProvider timestampProvider;
    private final NMEACache cache;
    private final LogAdmin log;
    private static final boolean SEND_HDM = false;
    private static final boolean SEND_HDT = false;

    @Inject
    public NMEASourceGyro(@NotNull NMEACache cache, @NotNull TimestampProvider tp, @NotNull DeviationManager deviationManager, @NotNull LogAdmin log) {
        super(log, tp, true, false);
        this.log = log;
        this.timestampProvider = tp;
        this.cache = cache;
        this.deviationManager = deviationManager;
    }

    @Override
    public String getType() {
        return "OnBoard Gyro";
    }

    @Override
    public String toString() {
        return getType();
    }

    @Override
    public String getDescription() {
        return "Gyro(" + (compassSensor == null ? "-" : "*") + ")";
    }

    @Override
    protected boolean onActivate() {
        synchronized (this) {
            compassSensor = createCompass();
            return true;
        }
    }

    private void doLF() {
        synchronized (this) {
            if (isStarted()) {
                readSensors();
                sendHDx();
                sendXDR();
            }
        }
    }

    private SensorCompass createCompass() {
        try {
            SensorCompass r = new SensorCompass(log,
                    USE_CMPS11 ? new CMPS11CompassDataProvider() : new HMC5883MPU6050CompassDataProvider(log),
                    deviationManager);
            r.init();
            return r;
        } catch (Exception e) {
            log.errorForceStacktrace(LogStringBuilder.start(GYRO_AGENT_CATEGORY).wO("init").toString(), e);
            return null;
        }
    }

    private void readSensors() {
        try {
            if (compassSensor!=null) {
                compassSensor.loadConfiguration();
                compassSensor = (SensorCompass) readSensor(compassSensor);
            }
        } catch (Exception e) {
            log.errorForceStacktrace(LogStringBuilder.start(GYRO_AGENT_CATEGORY).wO("read").toString(), e);
        }
    }

    private Sensor readSensor(Sensor s) {
        if (s!=null) {
            try {
                s.read();
            } catch (SensorException e) {
                log.errorForceStacktrace(LogStringBuilder.start(GYRO_AGENT_CATEGORY).wO("read").toString(), e);
                return null;
            }
        }
        return s;
    }

    private boolean headingNotPresentOnStream() {
        return (
                /* another source may have provided a heading but it's too old, presumably the source is down*/
                cache.isHeadingOlderThan(timestampProvider.getNow(), SEND_HD_IDLE_TIME) ||

                        /* there is a heading but it's mine (so no other sources are providing a heading  */
                        getName().equals(cache.getLastHeading().getSource()));
    }

    private void sendHDx() {
        try {
            if (compassSensor!=null && headingNotPresentOnStream()) {

                double b = compassSensor.getHeading();

                if (SEND_HDM) {
                    HDMSentence hdm = (HDMSentence) SentenceFactory.getInstance().createParser(TalkerId.II, SentenceId.HDM);
                    hdm.setHeading(Utils.normalizeDegrees0To360(b));
                    notify(hdm);
                }

                if (cache.getLastPosition().getData() != null) {
                    NMEAMagnetic2TrueConverter m = new NMEAMagnetic2TrueConverter(timestampProvider.getYear());
                    m.setPosition(cache.getLastPosition().getData().getPosition());

                    if (SEND_HDT) {
                        HDTSentence hdt = m.getTrueSentence(TalkerId.II, b);
                        notify(hdt);
                    }

                    HDGSentence hdg = m.getSentence(TalkerId.II, b, 0.0);
                    notify(hdg);
                } else {
                    HDGSentence hdg = (HDGSentence) SentenceFactory.getInstance().createParser(TalkerId.II, SentenceId.HDG);
                    hdg.setHeading(Utils.normalizeDegrees0To360(b));
                    hdg.setDeviation(0.0);
                    // do not set variation
                    notify(hdg);

                }
            }
        } catch (Exception e) {
            log.errorForceStacktrace(LogStringBuilder.start(GYRO_AGENT_CATEGORY).wO("message").toString(), e);
        }

    }

    private void sendXDR() {
        if (compassSensor!=null) {
            try {
                double roll = compassSensor.getUnfilteredRoll();
                double pitch = compassSensor.getUnfilteredPitch();
                double hd = compassSensor.getHeading();
                MsgAttitude msgAttitude = new MsgAttitudeImpl(hd, pitch, roll);
                notify(msgAttitude);
            } catch (Exception e) {
                log.errorForceStacktrace(LogStringBuilder.start(GYRO_AGENT_CATEGORY).wO("message xdr").toString(), e);
            }
        }
    }

    @OnRouterMessage
    public void onMessage(RouterMessage s) {
        if (HWSettings.getPropertyAsInteger("compass.dump", 0) > 0 &&
                s.getMessage() instanceof MsgHeading &&
                compassSensor != null) {
            try {
                double headingBoat = ((MsgHeading) s.getMessage()).getHeading();
                double headingSens = compassSensor.getUnfilteredSensorHeading();
                dump(headingSens, headingBoat);
            } catch (Exception e) {
                log.errorForceStacktrace(LogStringBuilder.start(GYRO_AGENT_CATEGORY).wO("learn").toString(), e);
            }
        }
    }

    private void dump(double headingSens, double headingBoat) throws IOException {
        int hdg = (int)headingSens;
        try (FileOutputStream stream = new FileOutputStream(new File(String.format("hdg%d.csv", hdg)), true)) {
            stream.write(String.format("%d%n", (int)headingBoat).getBytes());
        }
    }

    private static final int TIMER_FACTOR = 2;
    private int timerCount = 0;

    @Override
    public void onTimerHR() {
        timerCount = (timerCount + 1) % TIMER_FACTOR;
        if (timerCount==0) doLF();
        super.onTimer();
    }
}
