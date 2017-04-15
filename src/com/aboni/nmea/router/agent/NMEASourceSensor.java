package com.aboni.nmea.router.agent;

import com.aboni.sensors.SensorPressureTemp;
import com.aboni.sensors.SensorTemp;
import com.aboni.sensors.Sensor;
import com.aboni.sensors.SensorCompass;
import com.aboni.sensors.SensorNotInititalizedException;
import com.aboni.sensors.SensorVoltage;
import com.aboni.sensors.hw.CPUTemp;
import com.aboni.utils.HWSettings;

import java.util.Collection;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

import com.aboni.geo.NMEAMagnetic2TrueConverter;
import com.aboni.geo.Utils;
import com.aboni.nmea.router.NMEACacheProvider;
import com.aboni.nmea.router.impl.NMEAAgentImpl;
import com.aboni.nmea.sentences.XXXPSentence;

import net.sf.marineapi.nmea.parser.SentenceFactory;
import net.sf.marineapi.nmea.sentence.HDGSentence;
import net.sf.marineapi.nmea.sentence.HDMSentence;
import net.sf.marineapi.nmea.sentence.HDTSentence;
import net.sf.marineapi.nmea.sentence.MHUSentence;
import net.sf.marineapi.nmea.sentence.MMBSentence;
import net.sf.marineapi.nmea.sentence.MTASentence;
import net.sf.marineapi.nmea.sentence.Sentence;
import net.sf.marineapi.nmea.sentence.SentenceId;
import net.sf.marineapi.nmea.sentence.TalkerId;
import net.sf.marineapi.nmea.sentence.XDRSentence;
import net.sf.marineapi.nmea.util.Measurement;

public class NMEASourceSensor extends NMEAAgentImpl {

    private static final int PERIOD = 500; //ms
    
    /**
     * After 1m no HDx sentence appear on the stream the sensor start providing its own.
     * This is in case the boat can provide heading values (AP, boat compass etc.).
     */
    private static final long SEND_HDx_IDLE_TIME = 15*1000; //ms
	
    private boolean started;
    
    private Timer timer;
    
    private SensorVoltage voltageSensor;
    private SensorCompass compassSensor;
    private SensorPressureTemp pressureTempSensor0;
    private SensorPressureTemp pressureTempSensor1;
    private SensorPressureTemp[] pressureTempSensors;
    private SensorTemp tempSensor;
    
    private boolean sendHDM = false;
    private boolean sendHDT = false;
    
    public NMEASourceSensor(String name, QOS q) {
        super(name, q);
        setSourceTarget(true, false);
    }
    
    @Override
    public String getDescription() {
    	return 
    			"Temp(" + (tempSensor==null?"-":"*") + ") " + 
    			"Volt(" + (voltageSensor==null?"-":"*") + ") " + 
    			"Gyro(" + (compassSensor==null?"-":"*") + ") " + 
    			"Atm1(" + (pressureTempSensor0==null?"-":"*") + ") " + 
    			"Atm2(" + (pressureTempSensor1==null?"-":"*") + ")";
    }
    
    @Override
    protected boolean onActivate() {
        started = true;
    
        tempSensor = createTempSensor();
        compassSensor = createCompass();
        pressureTempSensor0 = createTempPressure(0);
        pressureTempSensor1 = createTempPressure(1);
        pressureTempSensors = new SensorPressureTemp[] {pressureTempSensor0, pressureTempSensor1};
        voltageSensor = createVoltage();
        
        TimerTask t = new TimerTask() {
            
            @Override
            public void run() {
                doLF();
            }
        };
        timer = new Timer(getName(), true);
        timer.scheduleAtFixedRate(t, 1000 /* wait 1s before starting reading*/, PERIOD);
        return true;
    }
    
    @Override
    protected synchronized void onDeactivate() {
        started = false;
        timer.cancel();
        timer = null;
    }

    private void doLF() {
        if (started) {
        	readSensors();
        	int mtaSensor = HWSettings.getPropertyAsInteger("mta.sensor", 0);
        	int mmbSensor = HWSettings.getPropertyAsInteger("mmb.sensor", 0);
        	int mhuSensor = HWSettings.getPropertyAsInteger("mhu.sensor", 0);
            sendXXX();
            sendMTA(mtaSensor);
            sendMMB(mmbSensor);
            sendMHU(mhuSensor);
            sendHDx();
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
    
	private SensorCompass createCompass() {
		try {
			SensorCompass r = new SensorCompass();
			r.init();
			return r;
		} catch (Exception e) {
			getLogger().Error("Error creating compass sensor ", e);
			return null;
		}
	}

    private void readSensors() {
        try {
        	tempSensor = (SensorTemp)readSensor(tempSensor);
            voltageSensor = (SensorVoltage)readSensor(voltageSensor);
            pressureTempSensor0 = (SensorPressureTemp)readSensor(pressureTempSensor0);
            pressureTempSensor1 = (SensorPressureTemp)readSensor(pressureTempSensor1);
            if (compassSensor!=null) {
                compassSensor.loadConfiguration();
                compassSensor = (SensorCompass)readSensor(compassSensor);
            }
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
    
    
    private void sendHDx() {
    	try {
    	    if (compassSensor!=null && 
    	    		(NMEACacheProvider.getCache().isHeadingOlderThan(System.currentTimeMillis(), SEND_HDx_IDLE_TIME)
    	    		|| getName().equals(NMEACacheProvider.getCache().getLastHeading().source))) {
	            double b = compassSensor.getHeading();
	            
	            if (sendHDM) {
    	            HDMSentence hdm = (HDMSentence) SentenceFactory.getInstance().createParser(TalkerId.II, SentenceId.HDM);
    	            hdm.setHeading(Utils.normalizeDegrees0_360(b));
    	            notify(hdm);
	            }
	            
	            if (NMEACacheProvider.getCache().getLastPosition().data != null) {
	                NMEAMagnetic2TrueConverter m = new NMEAMagnetic2TrueConverter();
	                m.setPosition(NMEACacheProvider.getCache().getLastPosition().data.getPosition());
	                
	                if (sendHDT) {
    	                HDTSentence hdt = m.getTrueSentence(TalkerId.II, b);
    	                notify(hdt);
	                }
	                
	                HDGSentence hdg = m.getSentence(TalkerId.II, b, 0.0);
	                notify(hdg);
	            } else {
	                HDGSentence hdg = (HDGSentence) SentenceFactory.getInstance().createParser(TalkerId.II, SentenceId.HDG);
                    hdg.setHeading(Utils.normalizeDegrees0_360(b));
                    hdg.setDeviation(0.0);
                    // do not set variation
	                notify(hdg);
	                
	            }
	        }	
		} catch (Exception e) {
			getLogger().Error("Cannot post heading data", e);
		}

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
    	        mhu.setAbsoluteHumidity(pressureTempSensors[sensor].getHumidity());
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
			xdr.addMeasurement(new Measurement("C", Math.round(CPUTemp.getInstance().getTemp()*100d)/100d, "C", "CPUTemp"));
			notify(xdr);
		} catch (Exception e) {
			getLogger().Error("Cannot post XDR data", e);
		}
	}

	private void sendXDR() {
	    XDRSentence xdr = (XDRSentence)SentenceFactory.getInstance().createParser(TalkerId.II, SentenceId.XDR.toString());
	    if (compassSensor!=null) {
	        try {
                double[] rot = compassSensor.getRotationDegrees();
                xdr.addMeasurement(new Measurement("A", Math.round(rot[0]), "D", "ROLL"));
                xdr.addMeasurement(new Measurement("A", Math.round(rot[1]), "D", "PITCH"));
	        } catch (Exception e) {
	            getLogger().Error("Cannot post XDR data", e);
	        }
        }
	    for (int i = 0; i<pressureTempSensors.length; i++) {
	        if (pressureTempSensors[i]!=null) {
	            try {
	                double t = pressureTempSensors[i].getTemperatureCelsius();
	                double pr = pressureTempSensors[i].getPressureMB();
	                double h = pressureTempSensors[i].getHumidity();
	                xdr.addMeasurement(new Measurement("B", Math.round(pr)/1000d, "B", "Barometer_" + i));
	                xdr.addMeasurement(new Measurement("C", Math.round(t*10d)/10d, "C", "AirTemp_" + i));
	                xdr.addMeasurement(new Measurement("P", Math.round(h*100d)/100d, "H", "Humidity_" + i));
	            } catch (Exception e) {
	                getLogger().Error("Cannot post XDR data", e);
	            }
	        }
	    }
        if (tempSensor!=null) {
            try {
            	Collection<SensorTemp.Reading> r = tempSensor.getReadings();
            	for (Iterator<SensorTemp.Reading> i = r.iterator(); i.hasNext(); ) {
    				SensorTemp.Reading tr = i.next();
    				if ((System.currentTimeMillis() - tr.ts) < 1000) {
    					String name = tr.k.substring(tr.k.length()-4, tr.k.length()-1);
    					String mappedName = HWSettings.getProperty("temp.map." + name);
    					if (mappedName==null) mappedName = name;
    					xdr.addMeasurement(
            				new Measurement("C", Math.round(tr.v*10d)/10d, "C", mappedName));
    				}
            	}
            } catch (Exception e) {
                getLogger().Error("Cannot post XDR data", e);
            }
        }
        if (voltageSensor!=null) {
            try {
				xdr.addMeasurement(new Measurement("V", Math.round(voltageSensor.getVoltage0()*1000d)/1000d, "V", "V0"));
				xdr.addMeasurement(new Measurement("V", Math.round(voltageSensor.getVoltage1()*1000d)/1000d, "V", "V1"));
				xdr.addMeasurement(new Measurement("V", Math.round(voltageSensor.getVoltage2()*1000d)/1000d, "V", "V2"));
				xdr.addMeasurement(new Measurement("V", Math.round(voltageSensor.getVoltage3()*1000d)/1000d, "V", "V3"));
            } catch (Exception e) {
                getLogger().Error("Cannot post XDR data", e);
            }
        }
        notify(xdr);
	}
	
	private void sendXXX() {
        try {
            XXXPSentence sentence = (XXXPSentence) SentenceFactory.getInstance().createParser(TalkerId.P, "XXP");
            if (compassSensor!=null) {
            	double[] rot = compassSensor.getRotationDegrees();
                double[] xyz = compassSensor.getMagReading();
                double b = compassSensor.getHeading();
                sentence.setHeading(b);
                sentence.setMagX(xyz[0]);
                sentence.setMagY(xyz[1]);
                sentence.setMagZ(xyz[2]);
                sentence.setRotationX(rot[0]);
                sentence.setRotationY(rot[1]);
                sentence.setRotationZ(rot[2]);
            }
            if (pressureTempSensor0!=null) {
            	double t = pressureTempSensor0.getTemperatureCelsius();
    	        double pr = pressureTempSensor0.getPressureMB();
    	        sentence.setPressure(pr);
    	        sentence.setTemperature(t);
            }
            if (voltageSensor!=null) {
                sentence.setVoltage(voltageSensor.getVoltage0());
                sentence.setVoltage1(voltageSensor.getVoltage1());
            }
	        notify(sentence);
		} catch (Exception e) {
			getLogger().Error("Cannot post XXX data", e);
		}
	}

    @Override
    protected void doWithSentence(Sentence s, NMEAAgent source) {
        // do nothing - pure source
    }
}
