package com.aboni.nmea.router.agent.impl.track;

import java.util.Calendar;

import com.aboni.geo.Course;
import com.aboni.geo.GeoPositionT;
import com.aboni.nmea.router.NMEACache;
import com.aboni.nmea.router.agent.NMEAAgent;
import com.aboni.nmea.router.agent.impl.NMEAAgentImpl;
import com.aboni.nmea.sentences.NMEAUtils;
import com.aboni.utils.ServerLog;

import net.sf.marineapi.nmea.sentence.RMCSentence;
import net.sf.marineapi.nmea.sentence.Sentence;
import net.sf.marineapi.nmea.sentence.SentenceId;
import net.sf.marineapi.nmea.util.Date;
import net.sf.marineapi.nmea.util.Position;
import net.sf.marineapi.nmea.util.Time;

public class NMEATrackAgent extends NMEAAgentImpl {

	private static final long CALC_SPEED_THRESHOLD = 5*60*1000; // 5 minutes
	private static final double SPEED_THRESHOLD = 40; //kn - anything faster than 40 knots is a mistake
	private TrackWriter media;
	private String mediaFile;
	private String listenSentence;

	private TrackManager tracker;
	
    public NMEATrackAgent(NMEACache cache, String name) {
        this(cache, name, SentenceId.RMC.toString());
    }

    public NMEATrackAgent(NMEACache cache, String name, String sentence) {
        super(cache, name);
        
        setSourceTarget(false, true);

        tracker = new TrackManager();
        
        // alternatives supported are GGA, RMC
        listenSentence = sentence;
        
        media = null;
    }

    public void setMedia(TrackWriter m) {
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
	    		media = new DBTrackWriter();
	    	} else {
	    		media = new FileTrackWriter(mediaFile);
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
    
    private GeoPositionT last;
	
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
	                        	Calendar timestamp = NMEAUtils.getTimestamp(time, date);
	                        	GeoPositionT pos_t = new GeoPositionT(timestamp.getTimeInMillis(), pos);
	                        	double speed = calcSpeed(s, pos_t);
	                        	if (speed < SPEED_THRESHOLD) {
		                        	last = pos_t;
		                            processPosition(pos_t, speed);
	                        	}
	                        }
	                    }
	                }
	            }
			} catch (Exception e) {
				ServerLog.getLogger().Error("Error processing position {" + s + "}", e);
			}
		}
	}

	private double calcSpeed(Sentence s, GeoPositionT pos_t) {
    	double speed = 0.0;
    	if (s instanceof RMCSentence) {
    		speed = ((RMCSentence)s).getSpeed();
    	}
    	else {
    		if (last!=null && (pos_t.getTimestamp()-last.getTimestamp())<CALC_SPEED_THRESHOLD) {
    			Course c = new Course(pos_t, last);
    			speed = c.getSpeed();
    		}
    	}
    	return speed;
	}
	
    private void processPosition(GeoPositionT pos_t, double sog) throws Exception {
        TrackManager.TrackPoint point = tracker.processPosition(pos_t, sog);
    	if (point!=null && media!=null) {
            media.write(point.position, 
            		point.anchor, 
            		point.distance,  
            		point.averageSpeed, 
            		point.maxSpeed, 
            		point.period);
        }
    }
    
    @Override
    public String getType() {
    	return "Tracker";
    }
    
    @Override
    public String getDescription() {
    	GeoPositionT pos = tracker.getLastTrackedPosition();
    	return "Tracking position from " + listenSentence + ((pos==null)?"":("<br>" + pos));
    }
    

}
