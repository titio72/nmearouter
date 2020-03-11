package com.aboni.nmea.router.agent.impl;

import com.aboni.geo.GeoPositionT;
import com.aboni.misc.Utils;
import com.aboni.nmea.router.NMEACache;
import com.aboni.nmea.router.NMEARouterStatuses;
import com.aboni.nmea.router.agent.QOS;
import com.aboni.nmea.router.track.*;
import com.aboni.nmea.sentences.NMEAUtils;
import com.aboni.sensors.EngineStatus;
import com.aboni.utils.Pair;
import com.aboni.utils.ServerLog;
import com.aboni.utils.ThingsFactory;
import net.sf.marineapi.nmea.sentence.RMCSentence;
import net.sf.marineapi.nmea.sentence.Sentence;
import net.sf.marineapi.nmea.util.Position;
import org.json.JSONObject;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.util.Date;

public class NMEATrackAgent extends NMEAAgentImpl {

    private static final long CHECK_TRIP_TIMEOUT = 60000L;
    private static final int CONTINUE_TRIP_THRESHOLD = 3 * 60 * 60 * 1000;  /* 3 hours */
    private final TrackWriter media;
    private final TrackManager tracker;
    private final TripManager tripManager;
    private Integer tripId;

    @Inject
    public NMEATrackAgent(@NotNull NMEACache cache, @NotNull TrackManager trackManager, @NotNull TripManager tripManager,
                          @NotNull TrackWriter mediaWriter) {
        super(cache);
        setSourceTarget(true, true);
        this.tracker = trackManager;
        this.tripManager = tripManager;
        this.media = mediaWriter;
        this.tripId = null;
    }

    @Override
    protected boolean onActivate() {
        return media.init();
    }
    
    @Override
    protected void onDeactivate() {
        // nothing to do
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
        long t0 = getCache().getNow();

        checkTrip(posT.getTimestamp());
        TrackPoint point = tracker.processPosition(posT, sog, tripId);
        notifyAnchorStatus();
        if (point != null && media != null) {
            TrackPointBuilder builder = ThingsFactory.getInstance(TrackPointBuilder.class);
            point = builder.withPoint(point).withEngine(getCache().getStatus(NMEARouterStatuses.ENGINE_STATUS, EngineStatus.UNKNOWN));
            media.write(point);
            notifyTrackedPoint(point);
            synchronized (this) {
                writes++;
            }
        }

        long t = getCache().getNow() - t0;
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
            if (tripId!=null) {
                msg.put("trip", tripId);
            }
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
        if (tripId != null) {
            msg.put("trip", point.getTrip());
        }
        notify(msg);
    }

    private long lastStats = 0;

    private long lastTripCheckTs = 0;

    private void checkTrip(long now) {
        if (tripId == null && (now - lastTripCheckTs) > CHECK_TRIP_TIMEOUT) {
            getLogger().info("Checking trip");
            try {
                Pair<Integer, Long> tripInfo = tripManager.getCurrentTrip(now);
                if (tripInfo != null) {
                    long lastTripTs = tripInfo.second;
                    int lastTrip = tripInfo.first;
                    getLogger().info(String.format("Last trip {%d} {%s}", lastTrip, new Date(lastTripTs).toString()));
                    if ((now - lastTripTs) < CONTINUE_TRIP_THRESHOLD) {
                        getLogger().info("Setting current trip {" + lastTrip + "}");
                        tripId = lastTrip;
                        tripManager.setTrip(lastTripTs - 1, now + 1, tripId);
                    }
                }
            } catch (TripManagerException e) {
                getLogger().error("Error detecting current trip", e);
            }
            lastTripCheckTs = now;
        }
    }

    @Override
    public void onTimer() {
        long now = getCache().getNow();
        if (now - lastStats > 30000) {
            lastStats = now;
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
    protected void onSetup(String name, QOS qos) {
        // do nothing
    }

    @Override
    public String toString() {
        return "{Tracker}";
    }

    @Override
    public String getDescription() {
        GeoPositionT pos = tracker.getLastTrackedPosition();
        return "Tracking position " + ((pos == null) ? "" : ("<br>" + pos));
    }
}
