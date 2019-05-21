package com.aboni.nmea.router.impl;

import com.aboni.nmea.router.NMEACache;
import com.aboni.nmea.sentences.NMEATimestampExtractor;
import com.aboni.utils.DataEvent;
import com.aboni.utils.ServerLog;
import net.sf.marineapi.nmea.sentence.*;

import java.util.Calendar;

public class NMEACacheImpl implements NMEACache {


    private DataEvent<HeadingSentence> lastHeading;
    private DataEvent<PositionSentence> lastPosition;
	private boolean synced;
	private static final long SYNC_THRESHOLD = 10000L; // 10 seconds
    private long timeSkew;

    public NMEACacheImpl() {
        lastHeading = new DataEvent<>(null, 0, "");
        lastPosition = new DataEvent<>(null, 0, "");
    }
    
    @Override
    public void onSentence(Sentence s, String src) {
    	try {
	        if (s instanceof HDGSentence ||
	        		s instanceof HDTSentence ||
	        		s instanceof HDMSentence) {
	        	lastHeading = new DataEvent<>((HeadingSentence)s, System.currentTimeMillis(),src );
	        } else if (s instanceof PositionSentence && s.isValid()) {
				lastPosition = new DataEvent<>((PositionSentence)s, System.currentTimeMillis(), src);
                checkTimeSkew(s);
	        }
    	} catch (Exception e) {
    		ServerLog.getLogger().warning("Cannot cache message {" + s + "} error {" + e.getMessage() + "}");
    	}
    }

    private void checkTimeSkew(Sentence s) {
        try {
            Calendar c = NMEATimestampExtractor.getTimestamp(s);
            if (c!=null) {
                timeSkew = c.getTimeInMillis() - System.currentTimeMillis();
                synced = Math.abs(timeSkew) < SYNC_THRESHOLD;
            }
        } catch (NMEATimestampExtractor.GPSTimeException ignored) {
            // do nothing
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
        return (time - lastHeading.getTimestamp()) > threshold;
    }

    @Override
    public long getTimeSkew() {
        return timeSkew;
    }
}