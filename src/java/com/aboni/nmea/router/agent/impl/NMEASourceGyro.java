package com.aboni.nmea.router.agent.impl;

import com.aboni.geo.NMEAMagnetic2TrueConverter;
import com.aboni.geo.impl.DeviationManagerImpl;
import com.aboni.misc.Utils;
import com.aboni.nmea.router.NMEACache;
import com.aboni.nmea.router.OnSentence;
import com.aboni.sensors.*;
import com.aboni.utils.HWSettings;
import net.sf.marineapi.nmea.parser.SentenceFactory;
import net.sf.marineapi.nmea.sentence.*;
import net.sf.marineapi.nmea.util.Measurement;

import javax.inject.Inject;
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

    private SensorCompass compassSensor;

    private static final boolean SEND_HDM = false;
    private static final boolean SEND_HDT = false;

    @Inject
    public NMEASourceGyro(NMEACache cache) {
        super(cache);
        setSourceTarget(true, true);
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
            SensorCompass r = new SensorCompass(
                    USE_CMPS11 ? new CMPS11CompassDataProvider() : new HMC5883MPU6050CompassDataProvider(),
                    new DeviationManagerImpl());
            r.init();
            return r;
        } catch (Exception e) {
            getLogger().error("Error creating compass sensor ", e);
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
    
    private boolean headingNotPresentOnStream() {
    	return (
                /* another source may have provided a heading but it's too old, presumably the source is down*/
                getCache().isHeadingOlderThan(getCache().getNow(), SEND_HD_IDLE_TIME) ||

                        /* there is a heading but it's mine (so no other sources are providing a heading  */
                        getName().equals(getCache().getLastHeading().getSource()));
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

                if (getCache().getLastPosition().getData() != null) {
                    NMEAMagnetic2TrueConverter m = new NMEAMagnetic2TrueConverter();
                    m.setPosition(getCache().getLastPosition().getData().getPosition());

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
			getLogger().error("Cannot post heading data", e);
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
                getLogger().error("Cannot post XDR data", e);
            }
        }
    }

    private double round(double d) {
        return Math.round(d * Math.pow(10, 0)) / Math.pow(10, 0);
    }

    @OnSentence
    public void onSentence(Sentence s, String source) {
        if (HWSettings.getPropertyAsInteger("compass.dump", 0) > 0 && s instanceof HDMSentence && compassSensor != null) {
            try {
                double headingBoat = ((HDMSentence) s).getHeading();
                double headingSens = compassSensor.getUnfilteredSensorHeading();
                dump(headingSens, headingBoat);
            } catch (Exception e) {
                getLogger().error("Error dumping compass readings", e);
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
