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

import com.aboni.misc.Utils;
import com.aboni.nmea.router.TimestampProvider;
import com.aboni.sensors.Sensor;
import com.aboni.sensors.SensorException;
import com.aboni.sensors.SensorPressureTemp;
import com.aboni.sensors.SensorTemp;
import com.aboni.sensors.hw.CPUTemp;
import com.aboni.utils.HWSettings;
import com.aboni.utils.Log;
import com.aboni.utils.LogStringBuilder;
import net.sf.marineapi.nmea.parser.SentenceFactory;
import net.sf.marineapi.nmea.sentence.*;
import net.sf.marineapi.nmea.util.Measurement;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.util.*;

public class NMEASourceSensor extends NMEAAgentImpl {

    private static final long READING_AGE_TIMEOUT = 600;
    public static final String SENSOR_AGENT_CATEGORY = "SensorAgent";
    public static final String SENSOR_KEY_NAME = "sensor";
    public static final String MESSAGE_KEY_NAME = "message";
    private final TimestampProvider timestampProvider;
    private boolean started;
    private int readCounter;

    private SensorPressureTemp pressureTempSensor;
    private SensorTemp tempSensor;
    private final Map<String, Measurement> xDrMap;
    private final Log log;

    @Inject
    public NMEASourceSensor(@NotNull TimestampProvider tp, @NotNull Log log) {
        super(log, tp, true, false);
        this.log = log;
        this.timestampProvider = tp;
        xDrMap = new HashMap<>();
    }

    @Override
    public String getType() {
        return "OnBoard Sensor";
    }

    @Override
    public String toString() {
        return getType();
    }

    @Override
    public String getDescription() {
        return "Temp(" + (tempSensor == null ? "-" : "*") + ") " +
                "Atm2(" + (pressureTempSensor == null ? "-" : "*") + ")";
    }

    @Override
    protected boolean onActivate() {
        started = true;

        List<Sensor> sensors = new ArrayList<>();
        tempSensor = createTempSensor();
        pressureTempSensor = createTempPressure();
        sensors.add(tempSensor);
        sensors.add(pressureTempSensor);
        for (Sensor s: sensors) {
            try {
                if (s != null) s.init();
            } catch (SensorException e) {
                log.error(LogStringBuilder.start(SENSOR_AGENT_CATEGORY).wO("init").wV(SENSOR_KEY_NAME, s.getSensorName()).toString(), e);
            }
        }
        return true;
    }

    @Override
    public void onTimer() {
        super.onTimer();
        doLF();
    }

    @Override
    protected synchronized void onDeactivate() {
        started = false;
    }

    private synchronized void doLF() {
        if (started) {
            resetXDR();

            readCounter = (readCounter + 1) % 10; // wait 10 seconds between readings
            if (readCounter == 0) {
                readSensors();
                sendAtmo();
                sendTemperature();
                sendMTA(HWSettings.getProperty("mta.sensor", "AirTemp"));
                sendMMB();
                sendMHU();
            }
            sendCPUTemp();
        }
    }

    private void resetXDR() {
        xDrMap.clear();
    }

    private SensorTemp createTempSensor() {
        try {
            return new SensorTemp(log);
        } catch (Exception e) {
            log.error(LogStringBuilder.start(SENSOR_AGENT_CATEGORY).wO("create sensor").wV(SENSOR_KEY_NAME, "temperature").toString(), e);
            return null;
        }
    }

    private SensorPressureTemp createTempPressure() {
        try {
            return new SensorPressureTemp(log);
        } catch (Exception e) {
            log.error(LogStringBuilder.start(SENSOR_AGENT_CATEGORY).wO("create sensor").wV(SENSOR_KEY_NAME, "pressure").toString(), e);
            return null;
        }
    }

    private void readSensors() {
        try {
            tempSensor = (SensorTemp) readSensor(tempSensor);
            pressureTempSensor = (SensorPressureTemp) readSensor(pressureTempSensor);
        } catch (Exception e) {
            log.error(LogStringBuilder.start(SENSOR_AGENT_CATEGORY).wO("read").toString(), e);
        }
    }

    private Sensor readSensor(Sensor s) {
        if (s!=null) {
            try {
                s.read();
            } catch (SensorException e) {
                log.error(LogStringBuilder.start(SENSOR_AGENT_CATEGORY).wO("read").toString(), e);
                return null;
            }
        }
        return s;
    }

    private boolean checkReadingAge(Sensor sensor) {
        return sensor != null && (timestampProvider.getNow() - sensor.getLastReadingTimestamp()) < READING_AGE_TIMEOUT;
    }

    private void sendMMB() {
        try {
            Measurement m = xDrMap.getOrDefault("Barometer", null);
            if (m != null) {
                double pr = m.getValue();
                MMBSentence mmb = (MMBSentence) SentenceFactory.getInstance().createParser(TalkerId.II, "MMB");
                mmb.setBars(pr);
                postMessage(mmb);
            }
        } catch (Exception e) {
            log.error(LogStringBuilder.start(SENSOR_AGENT_CATEGORY).wO(MESSAGE_KEY_NAME).wV(SENSOR_KEY_NAME, "pressure").toString(), e);
        }
    }

    private void sendMHU() {
        try {
            Measurement m = xDrMap.getOrDefault("Humidity", null);
            if (m != null) {
                double hum = m.getValue();
                MHUSentence mhu = (MHUSentence) SentenceFactory.getInstance().createParser(TalkerId.II, "MHU");
                mhu.setRelativeHumidity(hum);
                postMessage(mhu);
            }
        } catch (Exception e) {
            log.error(LogStringBuilder.start(SENSOR_AGENT_CATEGORY).wO(MESSAGE_KEY_NAME).wV(SENSOR_KEY_NAME, "humidity").toString(), e);
        }
    }

    private void sendMTA(String xdrName) {
        try {
            Measurement m = xDrMap.getOrDefault(xdrName, null);
            if (m != null) {
                double t = m.getValue();
                MTASentence mta = (MTASentence) SentenceFactory.getInstance().createParser(TalkerId.II, "MTA");
                mta.setTemperature(t);
                postMessage(mta);
            }
        } catch (Exception e) {
            log.error(LogStringBuilder.start(SENSOR_AGENT_CATEGORY).wO(MESSAGE_KEY_NAME).wV(SENSOR_KEY_NAME, "air temperature").toString(), e);
        }
    }

    private void sendCPUTemp() {
        try {
            XDRSentence xdr = (XDRSentence)SentenceFactory.getInstance().createParser(TalkerId.II, SentenceId.XDR.toString());
            addXDR(xdr, new Measurement("C", Utils.round(CPUTemp.getInstance().getTemp(), 2), "C", "CPUTemp"));
            postMessage(xdr);
        } catch (Exception e) {
            log.error(LogStringBuilder.start(SENSOR_AGENT_CATEGORY).wO(MESSAGE_KEY_NAME).wV(SENSOR_KEY_NAME, "cpu temperature").toString(), e);
        }
    }

    private void addXDR(XDRSentence xdr, Measurement m) {
        xdr.addMeasurement(m);
        xDrMap.put(m.getName(), m);
    }

    private void sendAtmo() {
        SensorPressureTemp sensor = pressureTempSensor;
        if (checkReadingAge(sensor)) {
            try {
                XDRSentence xdr = (XDRSentence) SentenceFactory.getInstance().createParser(TalkerId.II, SentenceId.XDR.toString());
                double t = sensor.getTemperatureCelsius();
                double pr = sensor.getPressureMB();
                double h = sensor.getHumidity();
                addXDR(xdr, new Measurement("B", Math.round(pr * 10) / 10000d, "B", "Barometer"));
                addXDR(xdr, new Measurement("C", Utils.round(t, 1), "C", "AirTemp"));
                addXDR(xdr, new Measurement("P", Utils.round(h, 2), "H", "Humidity"));
                postMessage(xdr);
            } catch (Exception e) {
                log.error(LogStringBuilder.start(SENSOR_AGENT_CATEGORY).wO(MESSAGE_KEY_NAME).wV(SENSOR_KEY_NAME, "xdr").toString(), e);
            }
        }
    }

    private void sendTemperature() {
        if (tempSensor!=null) {
            try {
                boolean empty = true;
                XDRSentence xdr = (XDRSentence) SentenceFactory.getInstance().createParser(TalkerId.II, SentenceId.XDR.toString());
                Collection<SensorTemp.Reading> r = tempSensor.getReadings();
                for (SensorTemp.Reading tr : r) {
                    if ((timestampProvider.getNow() - tr.getTimestamp()) < 1000) {
                        String name = tr.getKey().substring(tr.getKey().length() - 4, tr.getKey().length() - 1);
                        String mappedName = HWSettings.getProperty("temp.map." + name);
                        if (mappedName == null) mappedName = name;
                        addXDR(xdr, new Measurement("C", Math.round(tr.getValue() * 10d) / 10d, "C", mappedName));
                        empty = false;
                    }
                }
                if (!empty) postMessage(xdr);
            } catch (Exception e) {
                log.error(LogStringBuilder.start(SENSOR_AGENT_CATEGORY).wO(MESSAGE_KEY_NAME).wV(SENSOR_KEY_NAME, "xdr temperature").toString(), e);
            }
        }
    }
}
