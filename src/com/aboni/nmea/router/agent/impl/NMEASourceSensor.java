package com.aboni.nmea.router.agent.impl;

import com.aboni.sensors.SensorPressureTemp;
import com.aboni.sensors.SensorTemp;
import com.aboni.sensors.Sensor;
import com.aboni.sensors.SensorNotInititalizedException;
import com.aboni.sensors.SensorVoltage;
import com.aboni.sensors.hw.CPUTemp;
import com.aboni.utils.HWSettings;

import java.util.Collection;

import com.aboni.nmea.router.NMEACache;

import com.aboni.nmea.router.agent.NMEAAgent;
import com.aboni.nmea.router.agent.QOS;

import net.sf.marineapi.nmea.parser.SentenceFactory;
import net.sf.marineapi.nmea.sentence.MHUSentence;
import net.sf.marineapi.nmea.sentence.MMBSentence;
import net.sf.marineapi.nmea.sentence.MTASentence;
import net.sf.marineapi.nmea.sentence.Sentence;
import net.sf.marineapi.nmea.sentence.SentenceId;
import net.sf.marineapi.nmea.sentence.TalkerId;
import net.sf.marineapi.nmea.sentence.XDRSentence;
import net.sf.marineapi.nmea.util.Measurement;

public class NMEASourceSensor extends NMEAAgentImpl {

    private boolean started;
    
    private SensorVoltage voltageSensor;
    private SensorPressureTemp pressureTempSensor0;
    private SensorPressureTemp pressureTempSensor1;
    private SensorPressureTemp[] pressureTempSensors;
    private SensorTemp tempSensor;
    
    public NMEASourceSensor(NMEACache cache, String name, QOS q) {
        super(cache, name, q);
        setSourceTarget(true, false);
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
    
        tempSensor = createTempSensor();
        pressureTempSensor0 = createTempPressure(0);
        pressureTempSensor1 = createTempPressure(1);
        pressureTempSensors = new SensorPressureTemp[] {pressureTempSensor0, pressureTempSensor1};
        voltageSensor = createVoltage();
        
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
        	int mtaSensor = HWSettings.getPropertyAsInteger("mta.sensor", 0);
        	int mmbSensor = HWSettings.getPropertyAsInteger("mmb.sensor", 0);
        	int mhuSensor = HWSettings.getPropertyAsInteger("mhu.sensor", 0);
            sendMTA(mtaSensor);
            sendMMB(mmbSensor);
            sendMHU(mhuSensor);
            sendXDR();
            sendCPUTemp();
        }            
    }

	private SensorTemp createTempSensor() {
    	try {
    		SensorTemp t = new SensorTemp();
    		t.init();
    		return t;
    	} catch (Exception e) {
            getLogger().Error("Error creating temp sensor ", e);
            return null;
    	}
    }

    private SensorVoltage createVoltage() {
        try {
            SensorVoltage r = new SensorVoltage();
            r.init();
            return r;
        } catch (Exception e) {
            getLogger().Error("Error creating voltage sensor ", e);
            return null;
        }
    }

	private SensorPressureTemp createTempPressure(int i) {
		try {
			SensorPressureTemp p = new SensorPressureTemp(
					(i==0)?SensorPressureTemp.Sensor.BMP180:SensorPressureTemp.Sensor.BME280
			);
			p.init();
			return p;
		} catch (Exception e) {
			getLogger().Error("Error creating temp/press sensor", e);
			return null;
		}
	}
    
	private void readSensors() {
        try {
        	tempSensor = (SensorTemp)readSensor(tempSensor);
            voltageSensor = (SensorVoltage)readSensor(voltageSensor);
            pressureTempSensor0 = (SensorPressureTemp)readSensor(pressureTempSensor0);
            pressureTempSensor1 = (SensorPressureTemp)readSensor(pressureTempSensor1);
        } catch (Exception e) {
            getLogger().Error("Error reading sensor data", e);
        }
    }
    
    private Sensor readSensor(Sensor s) {
        if (s!=null) {
            try {
                s.read();
            } catch (SensorNotInititalizedException e) {
                getLogger().Error("Trying to read from a not initialized sensor {" + s.getSensorName() + "} - disabling it ");
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
			getLogger().Error("Cannot post pressure data", e);
		}
    }

	private void sendMHU(int sensor) {
		try {
		    if (pressureTempSensors[sensor]!=null) {
    	        MHUSentence mhu = (MHUSentence) SentenceFactory.getInstance().createParser(TalkerId.II, "MHU");
    	        mhu.setRelativeHumidity(pressureTempSensors[sensor].getHumidity());
    	        notify(mhu);
		    }
		} catch (Exception e) {
			getLogger().Error("Cannot post pressure data", e);
		}
    }

	private void sendMTA(int sensor) {
		try {
		    if (pressureTempSensors[sensor]!=null) {
    			double t = pressureTempSensors[sensor].getTemperatureCelsius();
    	        MTASentence mta = (MTASentence) SentenceFactory.getInstance().createParser(TalkerId.II, "MTA");
    	        mta.setTemperature(t);
    	        notify(mta);
		    }
		} catch (Exception e) {
			getLogger().Error("Cannot post temperature data", e);
		}
	}
	
	private void sendCPUTemp() {
		try {
			XDRSentence xdr = (XDRSentence)SentenceFactory.getInstance().createParser(TalkerId.II, SentenceId.XDR.toString());
			xdr.addMeasurement(new Measurement("C", round(CPUTemp.getInstance().getTemp(), 2), "C", "CPUTemp"));
			notify(xdr);
		} catch (Exception e) {
			getLogger().Error("Cannot post XDR data", e);
		}
	}

	private void sendXDR() {
        for (int i = 0; i<pressureTempSensors.length; i++) {
	        if (pressureTempSensors[i]!=null) {
	            try {
	                XDRSentence xdr = (XDRSentence)SentenceFactory.getInstance().createParser(TalkerId.II, SentenceId.XDR.toString());
	                double t = pressureTempSensors[i].getTemperatureCelsius();
	                double pr = pressureTempSensors[i].getPressureMB();
	                double h = pressureTempSensors[i].getHumidity();
	                xdr.addMeasurement(new Measurement("B", Math.round(pr)/1000d, "B", "Barometer_" + i));
	                xdr.addMeasurement(new Measurement("C", round(t, 1), "C", "AirTemp_" + i));
	                xdr.addMeasurement(new Measurement("P", round(h, 2), "H", "Humidity_" + i));
	                notify(xdr);
	            } catch (Exception e) {
	                getLogger().Error("Cannot post XDR data", e);
	            }
	        }
	    }

        if (tempSensor!=null) {
            try {
                boolean empty = true;
                XDRSentence xdr = (XDRSentence)SentenceFactory.getInstance().createParser(TalkerId.II, SentenceId.XDR.toString());
            	Collection<SensorTemp.Reading> r = tempSensor.getReadings();
                for (SensorTemp.Reading tr : r) {
                    if ((System.currentTimeMillis() - tr.ts) < 1000) {
                        String name = tr.k.substring(tr.k.length() - 4, tr.k.length() - 1);
                        String mappedName = HWSettings.getProperty("temp.map." + name);
                        if (mappedName == null) mappedName = name;
                        xdr.addMeasurement(
                                new Measurement("C", Math.round(tr.v * 10d) / 10d, "C", mappedName));
                        empty = false;
                    }
                }
                if (!empty) notify(xdr);
            } catch (Exception e) {
                getLogger().Error("Cannot post XDR data", e);
            }
        }

        if (voltageSensor!=null) {
            try {
                XDRSentence xdr = (XDRSentence)SentenceFactory.getInstance().createParser(TalkerId.II, SentenceId.XDR.toString());
				xdr.addMeasurement(new Measurement("V", round(voltageSensor.getVoltage0(), 3), "V", "V0"));
				xdr.addMeasurement(new Measurement("V", round(voltageSensor.getVoltage1(), 3), "V", "V1"));
				xdr.addMeasurement(new Measurement("V", round(voltageSensor.getVoltage2(), 3), "V", "V2"));
				xdr.addMeasurement(new Measurement("V", round(voltageSensor.getVoltage3(), 3), "V", "V3"));
		        notify(xdr);
            } catch (Exception e) {
                getLogger().Error("Cannot post XDR data", e);
            }
        }
	}
	
	private double round(double d, int precision) {
        return Math.round(d * Math.pow(10, precision)) / Math.pow(10, precision);
	}

    @Override
    protected void doWithSentence(Sentence s, NMEAAgent source) {
    }
}
