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
import com.aboni.log.Log;
import com.aboni.log.LogStringBuilder;
import com.aboni.nmea.message.MsgAttitude;
import com.aboni.nmea.message.MsgHeading;
import com.aboni.nmea.message.impl.MsgAttitudeImpl;
import com.aboni.nmea.router.*;
import com.aboni.nmea.router.utils.HWSettings;
import com.aboni.sensors.*;
import com.aboni.utils.TimestampProvider;
import com.aboni.utils.Utils;
import net.sf.marineapi.nmea.parser.SentenceFactory;
import net.sf.marineapi.nmea.sentence.*;

import javax.inject.Inject;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Logger;

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
    private final NMEACache cache;
    private static final boolean SEND_HDM = false;
    private static final boolean SEND_HDT = false;

    @Inject
    public NMEASourceGyro(NMEACache cache, TimestampProvider tp, DeviationManager deviationManager, Log log,
                          RouterMessageFactory messageFactory) {
        super(log, tp, messageFactory, true, false);
        if (cache==null) throw new IllegalArgumentException("Cache cannot be null");
        if (deviationManager==null) throw new IllegalArgumentException("Deviation manager cannot be null");
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
            SensorCompass r = new SensorCompass(getLog(),
                    USE_CMPS11 ? new CMPS11CompassDataProvider() : new HMC5883MPU6050CompassDataProvider(getLog()),
                    deviationManager);
            r.init();
            return r;
        } catch (Exception e) {
            getLog().errorForceStacktrace(LogStringBuilder.start(GYRO_AGENT_CATEGORY).wO("init").toString(), e);
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
            getLog().errorForceStacktrace(LogStringBuilder.start(GYRO_AGENT_CATEGORY).wO("read").toString(), e);
        }
    }

    private Sensor readSensor(Sensor s) {
        if (s!=null) {
            try {
                s.read();
            } catch (SensorException e) {
                getLog().errorForceStacktrace(LogStringBuilder.start(GYRO_AGENT_CATEGORY).wO("read").toString(), e);
                return null;
            }
        }
        return s;
    }

    private boolean headingNotPresentOnStream() {
        return (
                /* another source may have provided a heading, but it's too old, presumably the source is down*/
                cache.isHeadingOlderThan(getTimestampProvider().getNow(), SEND_HD_IDLE_TIME) ||

                        /* there is a heading, but it's mine (so no other sources are providing a heading  */
                        getName().equals(cache.getLastHeading().getSource()));
    }

    private void sendHDx() {
        try {
            if (compassSensor!=null && headingNotPresentOnStream()) {

                double b = compassSensor.getHeading();

                if (SEND_HDM) {
                    HDMSentence hdm = (HDMSentence) SentenceFactory.getInstance().createParser(TalkerId.II, SentenceId.HDM);
                    hdm.setHeading(Utils.normalizeDegrees0To360(b));
                    postMessage(hdm);
                }

                if (cache.getLastPosition().getData() != null) {
                    NMEAMagnetic2TrueConverter m = new NMEAMagnetic2TrueConverter(
                            getTimestampProvider().getYear(), Logger.getLogger(Constants.LOG_CONTEXT), Constants.WMM);
                    m.setPosition(cache.getLastPosition().getData().getPosition());

                    if (SEND_HDT) {
                        HDTSentence hdt = m.getTrueSentence(TalkerId.II, b);
                        postMessage(hdt);
                    }

                    HDGSentence hdg = m.getSentence(TalkerId.II, b, 0.0);
                    postMessage(hdg);
                } else {
                    HDGSentence hdg = (HDGSentence) SentenceFactory.getInstance().createParser(TalkerId.II, SentenceId.HDG);
                    hdg.setHeading(Utils.normalizeDegrees0To360(b));
                    hdg.setDeviation(0.0);
                    // do not set variation
                    postMessage(hdg);

                }
            }
        } catch (Exception e) {
            getLog().errorForceStacktrace(LogStringBuilder.start(GYRO_AGENT_CATEGORY).wO("message").toString(), e);
        }

    }

    private void sendXDR() {
        if (compassSensor!=null) {
            try {
                double roll = compassSensor.getUnfilteredRoll();
                double pitch = compassSensor.getUnfilteredPitch();
                double hd = compassSensor.getHeading();
                MsgAttitude msgAttitude = new MsgAttitudeImpl(hd, pitch, roll);
                postMessage(msgAttitude);
            } catch (Exception e) {
                getLog().errorForceStacktrace(LogStringBuilder.start(GYRO_AGENT_CATEGORY).wO("message xdr").toString(), e);
            }
        }
    }

    @OnRouterMessage
    public void onMessage(RouterMessage s) {
        if (HWSettings.getPropertyAsInteger("compass.dump", 0) > 0 &&
                s.getPayload() instanceof MsgHeading &&
                compassSensor != null) {
            try {
                double headingBoat = ((MsgHeading) s.getPayload()).getHeading();
                double headingSens = compassSensor.getUnfilteredSensorHeading();
                dump(headingSens, headingBoat);
            } catch (Exception e) {
                getLog().errorForceStacktrace(LogStringBuilder.start(GYRO_AGENT_CATEGORY).wO("learn").toString(), e);
            }
        }
    }

    private void dump(double headingSens, double headingBoat) throws IOException {
        int hdg = (int)headingSens;
        try (FileOutputStream stream = new FileOutputStream(String.format("hdg%d.csv", hdg), true)) {
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
