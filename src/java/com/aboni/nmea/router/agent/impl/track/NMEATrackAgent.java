package com.aboni.nmea.router.agent.impl.track;


import com.aboni.geo.GeoPositionT;
import com.aboni.misc.Utils;
import com.aboni.nmea.router.NMEACache;
import com.aboni.nmea.router.agent.impl.NMEAAgentImpl;
import com.aboni.nmea.sentences.NMEAUtils;
import com.aboni.utils.ServerLog;
import com.aboni.utils.db.DBHelper;
import net.sf.marineapi.nmea.sentence.RMCSentence;
import net.sf.marineapi.nmea.sentence.Sentence;
import net.sf.marineapi.nmea.util.Position;
import org.json.JSONObject;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class NMEATrackAgent extends NMEAAgentImpl {

	private TrackWriter media;
	private String mediaFile;
	private final TrackManager tracker;
	private Integer currentTrip;
	
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

        TrackPoint point = tracker.processPosition(posT, sog);
        notifyAnchorStatus();
    	if (point!=null && media!=null) {
            media.write(point.position, 
            		point.anchor, 
            		point.distance,  
            		point.averageSpeed, 
            		point.maxSpeed, 
            		point.period);
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
        msg.put("stationary", point.anchor);
        msg.put("distance", point.distance);
        msg.put("maxSpeed", point.maxSpeed);
        msg.put("speed", point.averageSpeed);
        msg.put("period", point.period);
        msg.put("lon", point.position.getLongitude());
        msg.put("lat", point.position.getLatitude());
        notify(msg);
    }

    private long lastStats = 0;

    private long lastTripCheckTs = 0;

    private static final String SQL_GETLASTTRIP = "select max(tripId), max(track.ts) from track where tripId=(select max(track.tripId) from track)";

    private void checkTrip() {
        if (currentTrip == null) {
            long now = System.currentTimeMillis();
            if ((now - lastTripCheckTs) > 60000L) {
                try (DBHelper db = new DBHelper(true)) {
                    try (PreparedStatement st = db.getConnection().prepareStatement(SQL_GETLASTTRIP)) {
                        ResultSet r = st.executeQuery();
                        if (r.next()) {
                            Timestamp lastTripTs = r.getTimestamp(2);
                            int lastTrip = r.getInt(1);
                            if ((now - lastTripTs.getTime()) < (3 * 60 * 1000) /* 3 hours */) {
                                currentTrip = lastTrip;
                                updateLastSamples(db, lastTripTs);
                            }
                        }
                    } catch (SQLException e) {
                        getLogger().error("Error detecting current trip", e);
                    }
                } catch (ClassNotFoundException e) {
                    getLogger().error("Error detecting current trip", e);
                }
            }
            lastTripCheckTs = now;
        }
    }

    private void updateLastSamples(DBHelper db, Timestamp ts) throws SQLException {
        String sql = "UPDATE track SET tripId=? WHERE TS>? AND TS<=?";
        try (PreparedStatement st = db.getConnection().prepareStatement(sql)) {
            st.setInt(1, currentTrip);
            st.setTimestamp(2, ts);
            st.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
            int n = st.executeUpdate();
            getLogger().info("Detected current trip {" + currentTrip + "} Updated {" + n + "} track points");
        }
    }

    @Override
    public void onTimer() {
    	checkTrip();
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
