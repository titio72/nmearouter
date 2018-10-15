package com.aboni.nmea.router.agent.impl.track;

import java.util.Calendar;

import com.aboni.geo.GeoPositionT;
import com.aboni.nmea.router.NMEACache;
import com.aboni.nmea.router.agent.NMEAAgent;
import com.aboni.nmea.router.agent.impl.NMEAAgentImpl;
import com.aboni.nmea.sentences.NMEAUtils;
import com.aboni.utils.ServerLog;

import net.sf.marineapi.nmea.sentence.RMCSentence;
import net.sf.marineapi.nmea.sentence.Sentence;
import net.sf.marineapi.nmea.util.Position;

public class NMEATrackAgent extends NMEAAgentImpl {

	private static final double SPEED_THRESHOLD = 40; //kn - anything faster than 40 knots is a mistake
	private TrackWriter media;
	private String mediaFile;
	private TrackManager tracker;
	
    public NMEATrackAgent(NMEACache cache, String name) {
        super(cache, name);
        
        setSourceTarget(false, true);

        tracker = new TrackManager();
        
        media = null;
    }

    public void setMedia(TrackWriter m) {
    	media = m;
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
    
	@Override
	protected void doWithSentence(Sentence s, NMEAAgent src) {
		if (isStarted()) {
			try {
	            if (s instanceof RMCSentence) {
	                RMCSentence rmc = (RMCSentence)s;
	                Position pos = NMEAUtils.getPosition(rmc);
	                if (pos!=null) {
	                    Calendar timestamp = NMEAUtils.getTimestampOptimistic(rmc);
	                    if (timestamp!=null) {
                        	GeoPositionT pos_t = new GeoPositionT(timestamp.getTimeInMillis(), pos);
                        	double speed = rmc.getSpeed();
                        	if (speed < SPEED_THRESHOLD) {
                                processPosition(pos_t, speed);
                        	} else {
                                ServerLog.getLogger().Info("Skipping {" + s + "} reason {speed>threshold}");
                        	}
	                    }
	                }
	            }
			} catch (Exception e) {
				ServerLog.getLogger().Error("Error processing position {" + s + "}", e);
			}
		}
	}
	
    private void processPosition(GeoPositionT pos_t, double sog) throws Exception {
        TrackPoint point = tracker.processPosition(pos_t, sog);
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
    public String toString() {
        return "{Tracker}";
    }
    
    @Override
    public String getDescription() {
    	GeoPositionT pos = tracker.getLastTrackedPosition();
    	return "Tracking position " + ((pos==null)?"":("<br>" + pos));
    }
    

}
