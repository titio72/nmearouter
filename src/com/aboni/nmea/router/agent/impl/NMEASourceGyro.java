package com.aboni.nmea.router.agent.impl;

import com.aboni.sensors.ASensorCompass;
import com.aboni.sensors.Sensor;
import com.aboni.sensors.SensorCMPS11;
import com.aboni.sensors.SensorCompass;
import com.aboni.sensors.SensorNotInititalizedException;
import com.aboni.utils.HWSettings;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import com.aboni.geo.NMEAMagnetic2TrueConverter;
import com.aboni.misc.Utils;
import com.aboni.nmea.router.NMEACache;

import com.aboni.nmea.router.agent.NMEAAgent;
import com.aboni.nmea.router.agent.QOS;

import net.sf.marineapi.nmea.parser.SentenceFactory;
import net.sf.marineapi.nmea.sentence.HDGSentence;
import net.sf.marineapi.nmea.sentence.HDMSentence;
import net.sf.marineapi.nmea.sentence.HDTSentence;
import net.sf.marineapi.nmea.sentence.Sentence;
import net.sf.marineapi.nmea.sentence.SentenceId;
import net.sf.marineapi.nmea.sentence.TalkerId;
import net.sf.marineapi.nmea.sentence.XDRSentence;
import net.sf.marineapi.nmea.util.Measurement;

public class NMEASourceGyro extends NMEAAgentImpl {

    private static final int PERIOD = 500; //ms
    
    /**
     * After 1m no HDx sentence appear on the stream the sensor start providing its own.
     * This is in case the boat can provide heading values (AP, boat compass etc.).
     */
    private static final long SEND_HDx_IDLE_TIME = 15 * 1000; //ms

    private static final boolean USE_CMPS11 = true;
    
    private boolean started;
    
    private Timer timer;
    
    private ASensorCompass compassSensor;
    
    private boolean sendHDM = false;
    private boolean sendHDT = false;
    
    private NMEACache cache;
    
    public NMEASourceGyro(NMEACache cache, String name, QOS q) {
        super(cache, name, q);
        this.cache = cache;
        setSourceTarget(true, true);
    }
    
    @Override
    public String getType() {
    	return "Onboard Sensor";
    }
    
    @Override
    public String getDescription() {
    	return "Gyro(" + (compassSensor==null?"-":"*") + ")";
    }
    
    @Override
    protected boolean onActivate() {
        started = true;
    
        compassSensor = createCompass();
        
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

    private synchronized void doLF() {
        if (started) {
        	readSensors();
            sendHDx();
            sendXDR();
        }            
    }
    
	private ASensorCompass createCompass() {
		try {
			ASensorCompass r = USE_CMPS11? new SensorCMPS11() : new SensorCompass();
			r.init();
			return r;
		} catch (Exception e) {
			getLogger().Error("Error creating compass sensor ", e);
			return null;
		}
	}

    private void readSensors() {
        try {
            if (compassSensor!=null) {
                compassSensor.loadConfiguration();
                compassSensor = (ASensorCompass)readSensor(compassSensor);
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
    	    		(cache.isHeadingOlderThan(System.currentTimeMillis(), SEND_HDx_IDLE_TIME)
    	    		|| getName().equals(cache.getLastHeading().source))) {
	            double b = compassSensor.getHeading();
	            
	            if (sendHDM) {
    	            HDMSentence hdm = (HDMSentence) SentenceFactory.getInstance().createParser(TalkerId.II, SentenceId.HDM);
    	            hdm.setHeading(Utils.normalizeDegrees0_360(b));
    	            notify(hdm);
	            }
	            
	            if (cache.getLastPosition().data != null) {
	                NMEAMagnetic2TrueConverter m = new NMEAMagnetic2TrueConverter();
	                m.setPosition(cache.getLastPosition().data.getPosition());
	                
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


	private void sendXDR() {
	    if (compassSensor!=null) {
	        try {
	            XDRSentence xdr = (XDRSentence)SentenceFactory.getInstance().createParser(TalkerId.II, SentenceId.XDR.toString());
	        	double roll = compassSensor.getUnfilteredRoll();
	        	double pitch = compassSensor.getUnfilteredPitch();
	        	double hd = compassSensor.getHeading();
                xdr.addMeasurement(new Measurement("A", round(hd, 0), "D", "HEAD"));
                xdr.addMeasurement(new Measurement("A", round(roll, 0), "D", "ROLL"));
                xdr.addMeasurement(new Measurement("A", round(pitch, 0), "D", "PITCH"));
                notify(xdr);
	        } catch (Exception e) {
	            getLogger().Error("Cannot post XDR data", e);
	        }
        }
	}
	
	private double round(double d, int precision) {
	    double r = Math.round(d * Math.pow(10, precision)) / Math.pow(10, precision);
	    return r;
	}
	
    @Override
    protected void doWithSentence(Sentence s, NMEAAgent source) {
        if (HWSettings.getPropertyAsInteger("compass.dump", 0)>0 && s instanceof HDMSentence && compassSensor!=null) {
        	try {
        		double headingBoat = ((HDMSentence)s).getHeading();
        		double headingSens = compassSensor.getHeading();
        		dump(headingSens, headingBoat);
        	} catch (Exception e) {}
        }
    }

	private void dump(double headingSens, double headingBoat) {
		int hdg = (int)headingSens;
		try {
			FileOutputStream stream = new FileOutputStream(new File(String.format("hdg%d.csv", hdg)), true);
			stream.write(String.format("%d%n", (int)headingBoat).getBytes());
			stream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
}