package com.aboni.nmea.router.agent;

import java.util.Calendar;

import com.aboni.geo.GeoPositionT;
import com.aboni.nmea.router.NMEACache;
import com.aboni.nmea.router.NMEAStream;
import com.aboni.nmea.router.agent.impl.TrackMediaDB;
import com.aboni.nmea.router.agent.impl.TrackMediaFile;
import com.aboni.nmea.router.impl.NMEAAgentImpl;
import com.aboni.nmea.sentences.NMEAUtils;
import com.aboni.utils.ServerLog;

import net.sf.marineapi.nmea.sentence.RMCSentence;
import net.sf.marineapi.nmea.sentence.Sentence;
import net.sf.marineapi.nmea.sentence.SentenceId;
import net.sf.marineapi.nmea.util.Date;
import net.sf.marineapi.nmea.util.Position;
import net.sf.marineapi.nmea.util.Time;

public class NMEATrackAgent extends NMEAAgentImpl {

	private TrackMedia media;
	private String mediaFile;
	private String listenSentence;

	private TrackManager tracker;
	
    public NMEATrackAgent(NMEACache cache, NMEAStream stream, String name) {
        this(cache, stream, name, SentenceId.RMC.toString());
    }

    public NMEATrackAgent(NMEACache cache, NMEAStream stream, String name, String sentence) {
        super(cache, stream, name);
        
        setSourceTarget(false, true);

        tracker = new TrackManager();
        
        // alternatives supported are GGA, RMC
        listenSentence = sentence;
        
        media = null;
    }

    public void setMedia(TrackMedia m) {
    	media = m;
    }
    
    /**
     * Set the sentence type to listen to (RMC & GLL are supported).
     * @param sentence
     */
    public void setListenSentence(String sentence) {
        listenSentence = sentence;
    }
    
    /**
     * Set "" for DB.
     * @param file
     */
    public void setFile(String file) {
    	mediaFile = file;
    }
    
    
    public String getFile() { 
    	return mediaFile;
    }

    @Override
    protected boolean onActivate() {
    	if (media==null) {
	    	if ("".equals(mediaFile) || mediaFile==null) { 
	    		media = new TrackMediaDB();
	    	} else {
	    		media = new TrackMediaFile(mediaFile);
	    	}
    	}
        return media.init();
    }
    
    @Override
    protected void onDeactivate() {
        if (media!=null) {
            media.dispose();
        }
        media = null;
    }    
    
    public long getPeriod() {
        return tracker.getPeriod();
    }

    /**
     * Set the sampling time in ms.
     * @param period
     */
    public void setPeriod(long period) {
        tracker.setPeriod(period);
    }

    public long getStaticPeriod() {
        return tracker.getStaticPeriod();
    }

    /**
     * Set the sampling time in ms.
     * @param period
     */
    public void setStaticPeriod(long period) {
        tracker.setStaticPeriod(period);
    }
	
	@Override
	protected void doWithSentence(Sentence s, NMEAAgent src) {
		if (isStarted()) {
			try {
	            if (s.getSentenceId().equals(listenSentence)) {
	                Position pos = NMEAUtils.getPosition(s);
	                if (pos!=null) {
	                    Time time = NMEAUtils.getTime(s);
	                    if (time!=null) {
	                        Date date = NMEAUtils.getDate(s);
	                        if (date!=null) {
	                        	double speed = 0.0;
	                        	if (s instanceof RMCSentence) speed = ((RMCSentence)s).getSpeed();
	                            processPosition(pos, time, date, speed);
	                        }
	                    }
	                }
	            }
			} catch (Exception e) {
				ServerLog.getLogger().Error("Cannot write down position!", e);
			}
		}
	}

    private void processPosition(Position pos, Time time, Date date, double sog) throws Exception {
        Calendar timestamp = NMEAUtils.getTimestamp(time, date);
    	TrackManager.TrackPoint point = tracker.processPosition(
    			new GeoPositionT(timestamp.getTimeInMillis(), pos), sog);
    	if (point!=null && media!=null) {
            media.writePoint(point.position, 
            		point.anchor, 
            		point.distance,  
            		point.averageSpeed, 
            		point.maxSpeed, 
            		point.period);
        }
    }
    
    @Override
    public String getDescription() {
    	String res = "Tracking " + listenSentence;
    	return res;
    }
    

}
