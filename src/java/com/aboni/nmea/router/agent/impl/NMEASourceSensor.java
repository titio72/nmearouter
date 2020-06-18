package com.aboni.nmea.router.agent.impl;

import com.aboni.misc.Utils;
import com.aboni.nmea.router.NMEACache;
import com.aboni.sensors.Sensor;
import com.aboni.sensors.SensorException;
import com.aboni.sensors.SensorPressureTemp;
import com.aboni.sensors.SensorTemp;
import com.aboni.sensors.hw.CPUTemp;
import com.aboni.utils.HWSettings;
import net.sf.marineapi.nmea.parser.SentenceFactory;
import net.sf.marineapi.nmea.sentence.*;
import net.sf.marineapi.nmea.util.Measurement;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.util.*;

public class NMEASourceSensor extends NMEAAgentImpl {

    private static final String ERROR_POST_XDR_DATA = "Cannot post XDR data";
    private static final long READING_AGE_TIMEOUT = 600;
    private boolean started;
    private int readCounter;

    private SensorPressureTemp pressureTempSensor;
    private SensorTemp tempSensor;
    private final Map<String, Measurement> xDrMap;

    @Inject
    public NMEASourceSensor(@NotNull NMEACache cache) {
        super(cache);
        setSourceTarget(true, false);
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
                getLogger().error("Error initializing sensor {" + s.getSensorName() + "}", e);
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
            return new SensorTemp();
        } catch (Exception e) {
            getLogger().errorForceStacktrace("Error creating temp sensor ", e);
            return null;
        }
    }

    private SensorPressureTemp createTempPressure() {
        try {
            return new SensorPressureTemp(SensorPressureTemp.Sensor.BME280);
        } catch (Exception e) {
            getLogger().errorForceStacktrace("Error creating temp/press sensor", e);
            return null;
        }
    }

    private void readSensors() {
        try {
            tempSensor = (SensorTemp) readSensor(tempSensor);
            pressureTempSensor = (SensorPressureTemp) readSensor(pressureTempSensor);
        } catch (Exception e) {
            getLogger().error("Error reading sensor data", e);
        }
    }
    
    private Sensor readSensor(Sensor s) {
        if (s!=null) {
            try {
                s.read();
            } catch (SensorException e) {
                getLogger().error("Trying to read from a not initialized sensor {" + s.getSensorName() + "} - disabling it ");
                return null;
            }
        }
        return s;
    }

    private boolean checkReadingAge(Sensor sensor) {
        return sensor != null && (getCache().getNow() - sensor.getLastReadingTimestamp()) < READING_AGE_TIMEOUT;
    }

    private void sendMMB() {
        try {
            Measurement m = xDrMap.getOrDefault("Barometer", null);
            if (m != null) {
                double pr = m.getValue();
                MMBSentence mmb = (MMBSentence) SentenceFactory.getInstance().createParser(TalkerId.II, "MMB");
                mmb.setBars(pr);
                notify(mmb);
            }
        } catch (Exception e) {
            getLogger().error("Cannot post pressure data", e);
        }
    }

    private void sendMHU() {
        try {
            Measurement m = xDrMap.getOrDefault("Humidity", null);
            if (m != null) {
                double hum = m.getValue();
                MHUSentence mhu = (MHUSentence) SentenceFactory.getInstance().createParser(TalkerId.II, "MHU");
                mhu.setRelativeHumidity(hum);
                notify(mhu);
            }
        } catch (Exception e) {
            getLogger().error("Cannot post pressure data", e);
        }
    }

    private void sendMTA(String xdrName) {
        try {
            Measurement m = xDrMap.getOrDefault(xdrName, null);
            if (m != null) {
                double t = m.getValue();
                MTASentence mta = (MTASentence) SentenceFactory.getInstance().createParser(TalkerId.II, "MTA");
                mta.setTemperature(t);
                notify(mta);
            }
        } catch (Exception e) {
            getLogger().error("Cannot post temperature data", e);
		}
	}
	
	private void sendCPUTemp() {
		try {
			XDRSentence xdr = (XDRSentence)SentenceFactory.getInstance().createParser(TalkerId.II, SentenceId.XDR.toString());
            addXDR(xdr, new Measurement("C", Utils.round(CPUTemp.getInstance().getTemp(), 2), "C", "CPUTemp"));
            notify(xdr);
		} catch (Exception e) {
            getLogger().error(ERROR_POST_XDR_DATA, e);
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
                notify(xdr);
            } catch (Exception e) {
                getLogger().error(ERROR_POST_XDR_DATA, e);
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
                    if ((getCache().getNow() - tr.getTimestamp()) < 1000) {
                        String name = tr.getKey().substring(tr.getKey().length() - 4, tr.getKey().length() - 1);
                        String mappedName = HWSettings.getProperty("temp.map." + name);
                        if (mappedName == null) mappedName = name;
                        addXDR(xdr, new Measurement("C", Math.round(tr.getValue() * 10d) / 10d, "C", mappedName));
                        empty = false;
                    }
                }
                if (!empty) notify(xdr);
            } catch (Exception e) {
                getLogger().error(ERROR_POST_XDR_DATA, e);
            }
        }
    }
}
