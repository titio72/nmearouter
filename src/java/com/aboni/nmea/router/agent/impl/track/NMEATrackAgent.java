package com.aboni.nmea.router.agent.impl.track;


import com.aboni.geo.GeoPositionT;
import com.aboni.nmea.router.NMEACache;
import com.aboni.nmea.router.agent.NMEAAgent;
import com.aboni.nmea.router.agent.impl.NMEAAgentImpl;
import com.aboni.nmea.sentences.NMEAUtils;
import com.aboni.nmea.sentences.XMCParser;
import com.aboni.utils.ServerLog;
import net.sf.marineapi.nmea.sentence.RMCSentence;
import net.sf.marineapi.nmea.sentence.Sentence;
import net.sf.marineapi.nmea.sentence.TalkerId;
import net.sf.marineapi.nmea.util.Position;

public class NMEATrackAgent extends NMEAAgentImpl {

	private TrackWriter media;
	private String mediaFile;
	private final TrackManager tracker;
	
    public NMEATrackAgent(NMEACache cache, String name) {
        super(cache, name);
        
        setSourceTarget(true, true);

        tracker = new TrackManager();
        
        media = null;
    }

    /**
     * Set "" for DB.
     * @param file WHen not empty redirect tracking info to the specified file
     */
    public void setFile(String file) {
    	mediaFile = file;
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

    /**
     * Set the sampling time in ms.
     * @param period Period in milliseconds
     */
    public void setPeriod(long period) {
        tracker.setPeriod(period);
    }

    /**
     * Set the sampling time in ms.
     * @param period Period in milliseconds when at anchor
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
	                Position pos = NMEAUtils.getPosition((RMCSentence)s);
	                if (pos!=null) {
		                GeoPositionT pos_t = new GeoPositionT(
		                		NMEAUtils.getTimestampOptimistic(rmc).getTimeInMillis(), pos);
                        processPosition(pos_t, rmc.getSpeed());
	                }
	            }
			} catch (Exception e) {
				ServerLog.getLogger().Error("Error processing position {" + s + "}", e);
				e.printStackTrace();
			}
		}
	}
	
    private void processPosition(GeoPositionT pos_t, double sog) {
		long t0 = System.currentTimeMillis();
        TrackPoint point = tracker.processPosition(pos_t, sog);
        
        Position avgPos = tracker.getAverage();
        boolean anchor = tracker.isStationary();

        if (avgPos!=null) {
	        XMCParser s = new XMCParser(TalkerId.P);
	        s.setAveragePosition(avgPos);
	        s.setAnchor(anchor);
	        notify(s);
        }
        
    	if (point!=null && media!=null) {
            media.write(point.position, 
            		point.anchor, 
            		point.distance,  
            		point.averageSpeed, 
            		point.maxSpeed, 
            		point.period);
            synchronized (this) {
                writes++;
            }
        }

        long t = System.currentTimeMillis() - t0;
        synchronized (this) {
        	avgTime = ((avgTime * samples) + t) / (samples + 1);
            samples++;
        }
    }
    
    private long lastStats = 0;
    
    @Override
    public void onTimer() {
    	if (System.currentTimeMillis() - lastStats > 30000) {
    		lastStats = System.currentTimeMillis();
    		synchronized (this) {
    			getLogger().Info(String.format("AvgWriteTime {%.2f} Samples {%d} Writes {%d}", avgTime, samples, writes));
    			avgTime = 0;
    			samples = 0;
    			writes = 0;
    		}
    	}
    }
    
    private double avgTime = 0;
    private int samples = 0;
    private int writes = 0;
    
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