/*
(C) 2020, Andrea Boni
This file is part of NMEARouter.
NMEARouter is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.
NMEARouter is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.
You should have received a copy of the GNU General Public License
along with NMEARouter.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.aboni.nmea.router.agent.impl;

import com.aboni.geo.GeoPositionT;
import com.aboni.nmea.router.NMEACache;
import com.aboni.nmea.router.NMEARouterStatuses;
import com.aboni.nmea.router.OnRouterMessage;
import com.aboni.nmea.router.RouterMessage;
import com.aboni.nmea.message.MsgPositionAndVector;
import com.aboni.nmea.router.data.track.*;
import com.aboni.sensors.EngineStatus;
import com.aboni.log.Log;
import com.aboni.utils.TimestampProvider;
import com.aboni.utils.Utils;
import net.sf.marineapi.nmea.util.Position;
import org.json.JSONObject;

import javax.inject.Inject;

public class NMEATrackAgent extends NMEAAgentImpl {

    private final TrackManager tracker;
    private final TripManagerX tripManager;
    private final NMEACache cache;
    private final TrackPointBuilder pointBuilder;

    @Inject
    public NMEATrackAgent(Log log, TimestampProvider tp, NMEACache cache,
                          TrackManager trackManager, TripManagerX tripManager,
                          TrackPointBuilder pointBuilder) {
        super(log, tp, true, true);
        if (cache==null) throw new IllegalArgumentException("Cache cannot be null");
        if (trackManager==null) throw new IllegalArgumentException("TrackManager cannot be null");
        if (tripManager==null) throw new IllegalArgumentException("TripManager cannot be null");
        if (pointBuilder==null) throw new IllegalArgumentException("PointBuilder cannot be null");
        this.cache = cache;
        this.tracker = trackManager;
        this.tripManager = tripManager;
        this.pointBuilder = pointBuilder;
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
     *
     * @param period Period in milliseconds when at anchor
     */
    public void setStaticPeriod(long period) {
        tracker.setStaticPeriod(period);
    }

    @OnRouterMessage
    public void onRouterMessage(RouterMessage msg) {
        if (isStarted() && msg.getPayload() instanceof MsgPositionAndVector) {
            onPosition((MsgPositionAndVector) msg.getPayload());
        }
    }

    private void onPosition(MsgPositionAndVector p) {
        try {
            Position pos = p.getPosition();
            double sog = p.getSOG();
            if (pos != null && !Double.isNaN(sog)) {
                GeoPositionT posT = new GeoPositionT(
                        (p.getTimestamp()==null)?getTimestampProvider().getNow():p.getTimestamp().toEpochMilli(),
                        pos);
                processPosition(posT, p.getSOG());
            }
        } catch (Exception e) {
            getLog().error(() -> getLogBuilder().wO("process sentence").wV("sentence", p).toString(), e);
        }
    }

    private void processPosition(GeoPositionT posT, double sog) {
        long t0 = getTimestampProvider().getNow();

        TrackPoint point = tracker.processPosition(posT, sog);
        notifyAnchorStatus();
        if (point != null) {
            TrackPointBuilder builder = pointBuilder.getNew();
            point = builder.withPoint(point).withEngine(cache.getStatus(NMEARouterStatuses.ENGINE_STATUS, EngineStatus.UNKNOWN)).getPoint();
            try {
                tripManager.onTrackPoint(new TrackEvent(point));
            } catch (TripManagerException e) {
                getLog().error(() -> getLogBuilder().wO("process point").wV("point", posT).toString(), e);
            }
            notifyTrackedPoint(point);
            synchronized (this) {
                writes++;
            }
        }

        long t = getTimestampProvider().getNow() - t0;
        synchronized (this) {
            avgTime = ((avgTime * samples) + t) / (samples + 1);
            samples++;
        }
    }

    private void notifyAnchorStatus() {
        Position avgPos = tracker.getAverage();
        cache.setStatus(NMEARouterStatuses.ANCHOR_STATUS, tracker.isStationary());
        if (avgPos!=null) {
            JSONObject msg = new JSONObject();
            msg.put("topic", "anchor");
            msg.put("stationary", tracker.isStationary());
            msg.put("latDec", avgPos.getLatitude());
            msg.put("lonDec", avgPos.getLongitude());
            msg.put("lat", Utils.formatLatitude(avgPos.getLatitude()));
            msg.put("lon", Utils.formatLongitude(avgPos.getLongitude()));
            postMessage(msg);
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
        postMessage(msg);
    }

    private long lastStats = 0;

    @Override
    public void onTimer() {
        super.onTimer();
        if (isStarted()) {
            long now = getTimestampProvider().getNow();
            if (now - lastStats > 30000) {
                lastStats = now;
                synchronized (this) {
                    getLog().info(() -> getLogBuilder().wO("stats").wV("avgWriteTime", "%.2f", avgTime).wV("samples", samples).wV("written", writes).toString());
                    avgTime = 0;
                    samples = 0;
                    writes = 0;
                }
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
        return "Tracker";
    }

    @Override
    public String getDescription() {
        GeoPositionT pos = tracker.getLastTrackedPosition();
        return "Tracking position " + ((pos == null) ? "" : ("<br>" + pos));
    }
}
