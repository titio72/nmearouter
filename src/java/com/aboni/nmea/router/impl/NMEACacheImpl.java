package com.aboni.nmea.router.impl;

import com.aboni.nmea.router.NMEACache;
import com.aboni.utils.DataEvent;
import com.aboni.utils.ServerLog;
import net.sf.marineapi.nmea.sentence.*;

import java.util.HashMap;
import java.util.Map;

public class NMEACacheImpl implements NMEACache {


    private DataEvent<HeadingSentence> lastHeading;
    private DataEvent<PositionSentence> lastPosition;
    private final Map<String, Object> statuses;

    public NMEACacheImpl() {
        lastHeading = new DataEvent<>(null, 0, "");
        lastPosition = new DataEvent<>(null, 0, "");
        statuses = new HashMap<>();
    }

    @Override
    public void onSentence(Sentence s, String src) {
    	try {
	        if (s instanceof HDGSentence ||
	        		s instanceof HDTSentence ||
	        		s instanceof HDMSentence) {
                lastHeading = new DataEvent<>((HeadingSentence) s, getNow(), src);
            } else if (s instanceof PositionSentence && s.isValid()) {
                lastPosition = new DataEvent<>((PositionSentence) s, getNow(), src);
            }
    	} catch (Exception e) {
    		ServerLog.getLogger().warning("Cannot cache message {" + s + "} error {" + e.getMessage() + "}");
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
	 * @see com.aboni.nmea.router.NMEACache#isHeadingOlderThan(long, long)
     */
    @Override
    public boolean isHeadingOlderThan(long time, long threshold) {
        return (time - lastHeading.getTimestamp()) > threshold;
    }

    @Override
    public <T> void setStatus(String statusKey, T status) {
        synchronized (statuses) {
            statuses.put(statusKey, status);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getStatus(String statusKey, T defaultValue) {
        synchronized (statuses) {
            return (T) statuses.getOrDefault(statusKey, defaultValue);
        }
    }

    @Override
    public long getNow() {
        return System.currentTimeMillis();
    }
}