package com.aboni.nmea.router.impl;

import com.aboni.nmea.router.NMEACache;
import com.aboni.utils.DataEvent;
import com.aboni.utils.ServerLog;
import net.sf.marineapi.nmea.sentence.*;

public class NMEACacheImpl implements NMEACache {


    private final DataEvent<TimeSentence> lastTime;
    private final DataEvent<HeadingSentence> lastHeading;
    private final DataEvent<PositionSentence> lastPosition;
	private boolean synced;
    
    public NMEACacheImpl() {
        lastHeading = new DataEvent<>();
        lastTime = new DataEvent<>();
        lastPosition = new DataEvent<>();
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
	 * @see com.aboni.nmea.router.NMEACache#isHeadingOlderThan(long, long)
	 */
    @Override
	public boolean isHeadingOlderThan(long time, long threshold) {
        return (time - lastHeading.timestamp) > threshold; 
    }
    
}