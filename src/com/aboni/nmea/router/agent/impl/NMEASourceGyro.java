package com.aboni.nmea.router.agent.impl;

import com.aboni.geo.NMEAMagnetic2TrueConverter;
import com.aboni.misc.Utils;
import com.aboni.nmea.router.NMEACache;
import com.aboni.nmea.router.agent.NMEAAgent;
import com.aboni.nmea.router.agent.QOS;
import com.aboni.sensors.*;
import com.aboni.utils.HWSettings;
import net.sf.marineapi.nmea.parser.SentenceFactory;
import net.sf.marineapi.nmea.sentence.*;
import net.sf.marineapi.nmea.util.Measurement;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class NMEASourceGyro extends NMEAAgentImpl {

    /**
     * After 1m no HDx sentence appear on the stream the sensor start providing its own.
     * This is in case the boat can provide heading values (AP, boat compass etc.).
     */
    private static final long SEND_HDx_IDLE_TIME = 15 * 1000; //ms

    private static final boolean USE_CMPS11 = true;
    
    private ASensorCompass compassSensor;
    
    private final static boolean sendHDM = false;
    private final static boolean sendHDT = false;
    
    private final NMEACache cache;
    
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
    	synchronized (this) {
    		if (!isStarted()) {
		        compassSensor = createCompass();
    		}
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
    
    private boolean headingNotPresentOnStream() {
    	return (
    			/* another source may have provided a heading but it's too old, presumably the source is down*/ 
    			cache.isHeadingOlderThan(System.currentTimeMillis(), SEND_HDx_IDLE_TIME) || 
	    		
	    		/* there is a heading but it's mine (so no other sources are providing a heading  */
	    		getName().equals(cache.getLastHeading().source));
    }
    
    private void sendHDx() {
    	try {
    	    if (compassSensor!=null && headingNotPresentOnStream()) {

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
                xdr.addMeasurement(new Measurement("A", round(hd), "D", "HEAD"));
                xdr.addMeasurement(new Measurement("A", round(roll), "D", "ROLL"));
                xdr.addMeasurement(new Measurement("A", round(pitch), "D", "PITCH"));
                notify(xdr);
	        } catch (Exception e) {
	            getLogger().Error("Cannot post XDR data", e);
	        }
        }
	}
	
	private double round(double d) {
		return Math.round(d * Math.pow(10, 0)) / Math.pow(10, 0);
	}
	
    @Override
    protected void doWithSentence(Sentence s, NMEAAgent source) {
        if (HWSettings.getPropertyAsInteger("compass.dump", 0)>0 && s instanceof HDMSentence && compassSensor!=null) {
        	try {
        		double headingBoat = ((HDMSentence)s).getHeading();
        		double headingSens = compassSensor.getUnfilteredSensorHeading();
        		dump(headingSens, headingBoat);
        	} catch (Exception e) {
        		getLogger().Error("Error dumping compass readings", e);
			}
        }
    }

	private void dump(double headingSens, double headingBoat) throws IOException {
		int hdg = (int)headingSens;
		try (FileOutputStream stream = new FileOutputStream(new File(String.format("hdg%d.csv", hdg)), true)) {
			stream.write(String.format("%d%n", (int)headingBoat).getBytes());
		}
	}
	
	static final int TIMER_FACTOR = 2;
	private int timerCount = 0;
	
	@Override
	public void onTimerHR() {
		timerCount = (timerCount + 1) % TIMER_FACTOR;
		if (timerCount==0) doLF();
		super.onTimer();
	}
}
