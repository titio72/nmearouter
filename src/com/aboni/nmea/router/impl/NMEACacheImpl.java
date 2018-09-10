package com.aboni.nmea.router.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.aboni.nmea.router.NMEACache;
import com.aboni.utils.DataEvent;
import com.aboni.utils.ServerLog;

import net.sf.marineapi.nmea.sentence.HDGSentence;
import net.sf.marineapi.nmea.sentence.HDMSentence;
import net.sf.marineapi.nmea.sentence.HDTSentence;
import net.sf.marineapi.nmea.sentence.HeadingSentence;
import net.sf.marineapi.nmea.sentence.PositionSentence;
import net.sf.marineapi.nmea.sentence.Sentence;
import net.sf.marineapi.nmea.sentence.TimeSentence;
import net.sf.marineapi.nmea.sentence.XDRSentence;
import net.sf.marineapi.nmea.util.Measurement;

public class NMEACacheImpl implements NMEACache {


    private DataEvent<TimeSentence> lastTime;
    private DataEvent<HeadingSentence> lastHeading;
    private DataEvent<PositionSentence> lastPosition;
    private Map<String, DataEvent<Measurement>> sensors;
	private boolean synced;
    
    public NMEACacheImpl() {
        lastHeading = new DataEvent<HeadingSentence>();
        lastTime = new DataEvent<TimeSentence>();
        lastPosition = new DataEvent<PositionSentence>();
        sensors = new HashMap<String, DataEvent<Measurement>>();
    }
    
    @Override
    public void onSentence(Sentence s, String src) {
    	try {
	        if (s instanceof HDGSentence ||
	        		s instanceof HDTSentence ||
	        		s instanceof HDMSentence) {
	            lastHeading.timestamp = System.currentTimeMillis();
	            lastHeading.source = src;
	            lastHeading.data = (HeadingSentence)s;
	        }
	        else if (s instanceof PositionSentence) {
	            if (s.isValid()) {
		            lastPosition.data = (PositionSentence)s;
		            lastPosition.source = src;
		            lastPosition.timestamp = System.currentTimeMillis();
	            }
	        }
	        else if (s instanceof XDRSentence) {
	        	for (Measurement m: ((XDRSentence)s).getMeasurements()) {
	        		DataEvent<Measurement> x = new DataEvent<Measurement>();
	        		x.data = m;
	        		x.source = src;
	        		x.timestamp = System.currentTimeMillis();
	        		synchronized (sensors) {
	            		sensors.put(m.getName(), x);
					}
	        	}
	        		
	        }
	        
	        if (s instanceof TimeSentence) {
	            lastTime.data = (TimeSentence)s;
	            lastTime.source = src;
	            lastTime.timestamp = System.currentTimeMillis();
	        }
    	} catch (Exception e) {
    		ServerLog.getLogger().Warning("Cannot cache message {" + s + "} error {" + e.getMessage() + "}");
    	}
    }
    
    @Override
    public boolean isTimeSynced() {
    	return synced;
    }
    
    @Override
    public void setTimeSynced() {
    	synced = true;
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

	@Override
	public DataEvent<TimeSentence> getLastUTCTime() {
		// TODO Auto-generated method stub
		return null;
	}
}