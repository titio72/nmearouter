package com.aboni.nmea.router.agent.impl;

import com.aboni.nmea.router.NMEACache;
import com.aboni.nmea.router.agent.QOS;
import com.aboni.sensors.*;
import com.aboni.sensors.hw.CPUTemp;
import com.aboni.utils.HWSettings;
import com.pi4j.io.i2c.I2CFactory;
import net.sf.marineapi.nmea.parser.SentenceFactory;
import net.sf.marineapi.nmea.sentence.*;
import net.sf.marineapi.nmea.util.Measurement;

import java.io.IOException;
import java.util.*;

public class NMEASourceSensor extends NMEAAgentImpl {

    private static final String ERROR_POST_XDR_DATA = "Cannot post XDR data";
    private boolean started;
    private int readCounter;

    private SensorVoltage voltageSensor;
    private SensorPressureTemp pressureTempSensor0;
    private SensorPressureTemp pressureTempSensor1;
    private SensorPressureTemp[] pressureTempSensors;
    private SensorTemp tempSensor;
    private final Map<String, Measurement> xDrMap;


    public NMEASourceSensor(NMEACache cache, String name, QOS q) {
        super(cache, name, q);
        setSourceTarget(true, false);
        xDrMap = new HashMap<>();
    }

    @Override
    public String getType() {
        return "Onboard Sensor";
    }
    
    @Override
    public String getDescription() {
    	return 
    			"Temp(" + (tempSensor==null?"-":"*") + ") " + 
    			"Volt(" + (voltageSensor==null?"-":"*") + ") " + 
    			"Atm1(" + (pressureTempSensor0==null?"-":"*") + ") " + 
    			"Atm2(" + (pressureTempSensor1==null?"-":"*") + ")";
    }
    
    @Override
    protected boolean onActivate() {
        started = true;

        List<Sensor> sensors = new ArrayList<>();
        tempSensor = createTempSensor();
        pressureTempSensor0 = createTempPressure(0);
        pressureTempSensor1 = createTempPressure(1);
        sensors.add(tempSensor);
        sensors.add(pressureTempSensor0);
        sensors.add(pressureTempSensor1);
        pressureTempSensors = new SensorPressureTemp[] {pressureTempSensor0, pressureTempSensor1};
        voltageSensor = createVoltage();
        sensors.add(voltageSensor);
        for (Sensor s: sensors) {
            try {
                if (s!=null) s.init();
            } catch (IOException | I2CFactory.UnsupportedBusNumberException e) {
                getLogger().error("Error initializing sensor {" + s.getSensorName() + "}", e);
            }
        }
        return true;
    }

    @Override
    public void onTimer() {
    	doLF();
    	super.onTimer();
    }
    
    @Override
    protected synchronized void onDeactivate() {
        started = false;
    }

    private synchronized void doLF() {
        if (started) {
            readSensors();
            resetXDR();
            sendPressure();
            sendTemperature();
            sendVoltage();
            sendMTA(HWSettings.getProperty("mta.sensor", "AirTemp_0"));
            sendMMB(HWSettings.getPropertyAsInteger("mmb.sensor", 0));
            sendMHU(HWSettings.getPropertyAsInteger("mhu.sensor", 0));
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
            getLogger().error("Error creating temp sensor ", e);
            return null;
        }
    }

    private SensorVoltage createVoltage() {
        try {
            return new SensorVoltage();
        } catch (Exception e) {
            getLogger().error("Error creating voltage sensor ", e);
            return null;
        }
    }

	private SensorPressureTemp createTempPressure(int i) {
		try {
            return new SensorPressureTemp(
                    (i==0)?SensorPressureTemp.Sensor.BMP180:SensorPressureTemp.Sensor.BME280
            );
		} catch (Exception e) {
			getLogger().error("Error creating temp/press sensor", e);
			return null;
		}
	}
    
	private void readSensors() {
        try {
            readCounter = (readCounter + 1) % 10;

            voltageSensor = (SensorVoltage) readSensor(voltageSensor);
            if (readCounter == 0) {
                tempSensor = (SensorTemp) readSensor(tempSensor);
            }
            if (readCounter == 5) {
                pressureTempSensor0 = (SensorPressureTemp) readSensor(pressureTempSensor0);
                pressureTempSensor1 = (SensorPressureTemp) readSensor(pressureTempSensor1);
            }
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

	private void sendMMB(int sensor) {
		try {
		    if (pressureTempSensors[sensor]!=null) {
    	        double pr = pressureTempSensors[sensor].getPressureMB();
    	        MMBSentence mmb = (MMBSentence) SentenceFactory.getInstance().createParser(TalkerId.II, "MMB");
    	        mmb.setBars(pr / 1000.0);
    	        notify(mmb);
		    }
		} catch (Exception e) {
			getLogger().error("Cannot post pressure data", e);
		}
    }

	private void sendMHU(int sensor) {
		try {
            if (pressureTempSensors[sensor] != null) {
                MHUSentence mhu = (MHUSentence) SentenceFactory.getInstance().createParser(TalkerId.II, "MHU");
                mhu.setRelativeHumidity(pressureTempSensors[sensor].getHumidity());
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
            addXDR(xdr, new Measurement("C", round(CPUTemp.getInstance().getTemp(), 2), "C", "CPUTemp"));
            notify(xdr);
		} catch (Exception e) {
            getLogger().error(ERROR_POST_XDR_DATA, e);
        }
    }

    private void addXDR(XDRSentence xdr, Measurement m) {
        xdr.addMeasurement(m);
        xDrMap.put(m.getName(), m);
    }

    private void sendPressure() {
        for (int i = 0; i < pressureTempSensors.length; i++) {
            long now = System.currentTimeMillis();
            if (pressureTempSensors[i] != null && (now - pressureTempSensors[i].getLastReadingTimestamp()) < 1000) {
                try {
                    XDRSentence xdr = (XDRSentence) SentenceFactory.getInstance().createParser(TalkerId.II, SentenceId.XDR.toString());
                    double t = pressureTempSensors[i].getTemperatureCelsius();
                    double pr = pressureTempSensors[i].getPressureMB();
                    double h = pressureTempSensors[i].getHumidity();
                    addXDR(xdr, new Measurement("B", Math.round(pr) / 1000d, "B", "Barometer_" + i));
                    addXDR(xdr, new Measurement("C", round(t, 1), "C", "AirTemp_" + i));
                    addXDR(xdr, new Measurement("P", round(h, 2), "H", "Humidity_" + i));
                    notify(xdr);
                } catch (Exception e) {
	                getLogger().error(ERROR_POST_XDR_DATA, e);
	            }
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
                    if ((System.currentTimeMillis() - tr.getTimestamp()) < 1000) {
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

    private void sendVoltage() {
        if (voltageSensor!=null) {
            try {
                XDRSentence xdr = (XDRSentence) SentenceFactory.getInstance().createParser(TalkerId.II, SentenceId.XDR.toString());
                addXDR(xdr, new Measurement("V", round(voltageSensor.getVoltage0(), 3), "V", "V0"));
                addXDR(xdr, new Measurement("V", round(voltageSensor.getVoltage1(), 3), "V", "V1"));
                addXDR(xdr, new Measurement("V", round(voltageSensor.getVoltage2(), 3), "V", "V2"));
                addXDR(xdr, new Measurement("V", round(voltageSensor.getVoltage3(), 3), "V", "V3"));
                notify(xdr);
            } catch (Exception e) {
                getLogger().error(ERROR_POST_XDR_DATA, e);
            }
        }
    }

    private double round(double d, int precision) {
        return Math.round(d * Math.pow(10, precision)) / Math.pow(10, precision);
	}

}
