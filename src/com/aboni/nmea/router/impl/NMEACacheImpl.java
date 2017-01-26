package com.aboni.nmea.router.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.aboni.nmea.router.NMEACache;
import com.aboni.nmea.router.Startable;
import com.aboni.nmea.router.agent.NMEAAgent;
import com.aboni.nmea.router.agent.NMEASentenceListener;
import com.aboni.utils.DataEvent;

import net.sf.marineapi.nmea.sentence.HDGSentence;
import net.sf.marineapi.nmea.sentence.HDMSentence;
import net.sf.marineapi.nmea.sentence.HDTSentence;
import net.sf.marineapi.nmea.sentence.HeadingSentence;
import net.sf.marineapi.nmea.sentence.PositionSentence;
import net.sf.marineapi.nmea.sentence.Sentence;
import net.sf.marineapi.nmea.sentence.XDRSentence;
import net.sf.marineapi.nmea.util.Measurement;

public class NMEACacheImpl implements Startable, NMEASentenceListener, NMEACache {


    private DataEvent<HeadingSentence> lastHeading;
    private DataEvent<PositionSentence> lastPosition;
    private Map<String, DataEvent<Measurement>> sensors;
    
    private boolean started;
    
    public NMEACacheImpl() {
        lastHeading = new DataEvent<HeadingSentence>();
        lastPosition = new DataEvent<PositionSentence>();
        sensors = new HashMap<String, DataEvent<Measurement>>();
        started = false;
    }
    
    @Override
    public void start() {
        if (!started) {
            started = true;
        }
    }
    
    @Override
    public void stop() {
        if (started) {
            started = false;
        }
    }
    
    /* (non-Javadoc)
	 * @see com.aboni.nmea.router.NMEACache#isStarted()
	 */
    @Override
    public boolean isStarted() {
        return started;
    }
    
    @Override
    public void onSentence(Sentence s, NMEAAgent src) {
    	if (isStarted()) {
	        if (s instanceof HDGSentence ||
	        		s instanceof HDTSentence ||
	        		s instanceof HDMSentence) {
	            lastHeading.timestamp = System.currentTimeMillis();
	            lastHeading.source = src.getName();
	            lastHeading.data = (HeadingSentence)s;
	        }
	        else if (s instanceof PositionSentence) {
	            lastPosition.data = (PositionSentence)s;
	            lastPosition.source = src.getName();
	            lastPosition.timestamp = System.currentTimeMillis();
	        }
	        else if (s instanceof XDRSentence) {
	        	for (Measurement m: ((XDRSentence)s).getMeasurements()) {
	        		DataEvent<Measurement> x = new DataEvent<Measurement>();
	        		x.data = m;
	        		x.source = src.getName();
	        		x.timestamp = System.currentTimeMillis();
	        		synchronized (sensors) {
	            		sensors.put(m.getName(), x);
					}
	        	}
	        		
	        }
    	}
    }

    /* (non-Javadoc)
	 * @see com.aboni.nmea.router.NMEACache#getLastHeading()
	 */
    @Override
	public DataEvent<HeadingSentence> getLastHeading() {
        return lastHeading;
    }

    /* (non-Javadoc)
	 * @see com.aboni.nmea.router.NMEACache#getLastPosition()
	 */
    @Override
	public DataEvent<PositionSentence> getLastPosition() {
        return lastPosition;
    }
    
    /* (non-Javadoc)
	 * @see com.aboni.nmea.router.NMEACache#getSensorData(java.lang.String)
	 */
    @Override
	public DataEvent<Measurement> getSensorData(String sensorName) {
    	synchronized (sensors) {
        	return sensors.getOrDefault(sensorName, null);
		}
    }
    
    /* (non-Javadoc)
	 * @see com.aboni.nmea.router.NMEACache#getSensors()
	 */
    @Override
	public Collection<String> getSensors() {
    	synchronized (sensors) {
			return sensors.keySet();
		}
    }
    
    /* (non-Javadoc)
	 * @see com.aboni.nmea.router.NMEACache#isHeadingOlderThan(long, long)
	 */
    @Override
	public boolean isHeadingOlderThan(long time, long threshold) {
        return (time - lastHeading.timestamp) > threshold; 
    }
    
    /* (non-Javadoc)
	 * @see com.aboni.nmea.router.NMEACache#isPositionOlderThan(long, long)
	 */
    @Override
	public boolean isPositionOlderThan(long time, long threshold) {
        return (time - lastPosition.timestamp) > threshold; 
    }
}