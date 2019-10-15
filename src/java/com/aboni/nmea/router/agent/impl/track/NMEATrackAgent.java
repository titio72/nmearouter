package com.aboni.nmea.router.agent.impl.track;

import com.aboni.geo.GeoPositionT;
import com.aboni.misc.Utils;
import com.aboni.nmea.router.NMEACache;
import com.aboni.nmea.router.agent.impl.NMEAAgentImpl;
import com.aboni.nmea.sentences.NMEAUtils;
import com.aboni.utils.Pair;
import com.aboni.utils.ServerLog;
import net.sf.marineapi.nmea.sentence.RMCSentence;
import net.sf.marineapi.nmea.sentence.Sentence;
import net.sf.marineapi.nmea.util.Position;
import org.json.JSONObject;

public class NMEATrackAgent extends NMEAAgentImpl {

	private TrackWriter media;
	private String mediaFile;
	private final TrackManager tracker;
    private Integer tripId;
    private TripManager tripManager;

    public NMEATrackAgent(NMEACache cache, String name) {
        super(cache, name);
        setSourceTarget(true, true);
        tracker = new TrackManager();
        media = null;
        tripId = null;
        tripManager = new DBTripManager();
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
	protected void doWithSentence(Sentence s, String src) {
		if (isStarted()) {
			try {
	            if (s instanceof RMCSentence) {
	                RMCSentence rmc = (RMCSentence)s;
	                Position pos = NMEAUtils.getPosition((RMCSentence)s);
	                if (pos!=null) {
		                GeoPositionT posT = new GeoPositionT(
		                		NMEAUtils.getTimestampOptimistic(rmc).getTimeInMillis(), pos);
                        processPosition(posT, rmc.getSpeed());
	                }
	            }
			} catch (Exception e) {
				ServerLog.getLogger().error("Error processing position {" + s + "}", e);
			}
		}
	}
	
    private void processPosition(GeoPositionT posT, double sog) {
		long t0 = System.currentTimeMillis();

		checkTrip(posT.getTimestamp());
        TrackPoint point = tracker.processPosition(posT, sog, tripId);
        notifyAnchorStatus();
    	if (point!=null && media!=null) {
            media.write(point);
            notifyTrackedPoint(point);
            synchronized (this) {writes++;}
        }

        long t = System.currentTimeMillis() - t0;
        synchronized (this) {
        	avgTime = ((avgTime * samples) + t) / (samples + 1);
            samples++;
        }
    }

    private void notifyAnchorStatus() {
        Position avgPos = tracker.getAverage();
        if (avgPos!=null) {
            JSONObject msg = new JSONObject();
            msg.put("topic", "anchor");
            msg.put("stationary", tracker.isStationary());
            msg.put("latDec", avgPos.getLatitude());
            msg.put("lonDec", avgPos.getLongitude());
            msg.put("lat", Utils.formatLatitude(avgPos.getLatitude()));
            msg.put("lon", Utils.formatLongitude(avgPos.getLongitude()));
            notify(msg);
        }
    }

    private void notifyTrackedPoint(TrackPoint point) {
        JSONObject msg = new JSONObject();
        msg.put("topic", "track");
        msg.put("stationary", point.isAnchor());
        msg.put("distance", point.getDistance());
        msg.put("maxSpeed", point.getMaxSpeed());
        msg.put("speed", point.getAverageSpeed());
        msg.put("period", point.getPeriod());
        msg.put("lon", point.getPosition().getLongitude());
        msg.put("lat", point.getPosition().getLatitude());
        if (tripId != null) msg.put("lat", point.getTrip());
        notify(msg);
    }

    private long lastStats = 0;

    private long lastTripCheckTs = 0;

    private void checkTrip(long now) {
        if (tripId == null) {
            if ((now - lastTripCheckTs) > 60000L) {
                Pair<Integer, Long> tripInfo = tripManager.getCurrentTrip(now);
                if (tripInfo!=null) {
                    long lastTripTs = tripInfo.second;
                    int lastTrip = tripInfo.first;
                    if ((now - lastTripTs) < (3 * 60 * 1000) /* 3 hours */) {
                        tripId = lastTrip;
                        tripManager.setTrip(lastTripTs - 1, now + 1, tripId);
                    }
                }
            }
            lastTripCheckTs = now;
        }
    }

    @Override
    public void onTimer() {
        if (System.currentTimeMillis() - lastStats > 30000) {
    		lastStats = System.currentTimeMillis();
    		synchronized (this) {
    			getLogger().info(String.format("AvgWriteTime {%.2f} Samples {%d} Writes {%d}", avgTime, samples, writes));
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
