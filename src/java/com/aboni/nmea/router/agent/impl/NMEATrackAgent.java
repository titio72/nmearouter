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
import com.aboni.misc.Utils;
import com.aboni.nmea.router.*;
import com.aboni.nmea.router.data.track.*;
import com.aboni.nmea.router.message.MsgPositionAndVector;
import com.aboni.sensors.EngineStatus;
import com.aboni.utils.Log;
import net.sf.marineapi.nmea.util.Position;
import org.json.JSONObject;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;

public class NMEATrackAgent extends NMEAAgentImpl {

    private final TrackManager tracker;
    private final TripManagerX tripManager;
    private final TimestampProvider timestampProvider;
    private final NMEACache cache;
    private final Log log;
    private final TrackPointBuilder pointBuilder;

    @Inject
    public NMEATrackAgent(@NotNull Log log, @NotNull TimestampProvider tp, @NotNull NMEACache cache,
                          @NotNull TrackManager trackManager, @NotNull TripManagerX tripManager,
                          @NotNull TrackPointBuilder pointBuilder) {
        super(log, tp, true, true);
        this.log = log;
        this.timestampProvider = tp;
        this.cache = cache;
        this.tracker = trackManager;
        this.tripManager = tripManager;
        this.pointBuilder = pointBuilder;
    }

    @Override
    protected boolean onActivate() {
        return true;
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
        if (isStarted() && msg.getMessage() instanceof MsgPositionAndVector) {
            onPosition((MsgPositionAndVector) msg.getMessage());
        }
    }

    private void onPosition(MsgPositionAndVector p) {
        try {
            Position pos = p.getPosition();
            double sog = p.getSOG();
            if (pos != null && !Double.isNaN(sog)) {
                GeoPositionT posT = new GeoPositionT(
                        (p.getTimestamp()==null)?timestampProvider.getNow():p.getTimestamp().toEpochMilli(),
                        pos);
                processPosition(posT, p.getSOG());
            }
        } catch (Exception e) {
            getLogBuilder().wO("process sentence").wV("sentence", p).error(log, e);
        }
    }

    private void processPosition(GeoPositionT posT, double sog) {
        long t0 = timestampProvider.getNow();

        TrackPoint point = tracker.processPosition(posT, sog);
        notifyAnchorStatus();
        if (point != null) {
            TrackPointBuilder builder = pointBuilder.getNew();
            point = builder.withPoint(point).withEngine(cache.getStatus(NMEARouterStatuses.ENGINE_STATUS, EngineStatus.UNKNOWN)).getPoint();
            try {
                tripManager.onTrackPoint(new TrackEvent(point));
            } catch (TripManagerException e) {
                getLogBuilder().wO("process point").wV("point", posT).error(log, e);
            }
            notifyTrackedPoint(point);
            synchronized (this) {
                writes++;
            }
        }

        long t = timestampProvider.getNow() - t0;
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
        notify(msg);
    }

    private long lastStats = 0;

    @Override
    public void onTimer() {
        super.onTimer();
        if (isStarted()) {
            long now = timestampProvider.getNow();
            if (now - lastStats > 30000) {
                lastStats = now;
                synchronized (this) {
                    getLogBuilder().wO("stats").wV("avgWriteTime", "%.2f", avgTime).wV("samples", samples).wV("written", writes).info(log);
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
