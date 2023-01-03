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

package com.aboni.nmea.router.data.track.impl;

import com.aboni.geo.Course;
import com.aboni.geo.GeoPositionT;
import com.aboni.nmea.router.data.track.PositionStats;
import com.aboni.nmea.router.data.track.TrackManager;
import com.aboni.nmea.router.data.track.TrackPoint;
import com.aboni.nmea.router.data.track.TrackPointBuilder;
import com.aboni.nmea.router.utils.ThingsFactory;
import net.sf.marineapi.nmea.util.Position;

public class TrackManagerImpl implements TrackManager {

    private double maxSpeed;
    private long staticPeriod;
    private long period;
    private GeoPositionT lastPoint;
    private GeoPositionT lastTrackedPoint;
    private final StationaryManager stationaryStatus;
    private final PositionStats stats;

    private final boolean reportAll;

    private static final int SECOND = 1000;
    private static final int MINUTE = 60 * SECOND;

    public static final long STATIC_DEFAULT_PERIOD = 10L * MINUTE;
    public static final long DEFAULT_PERIOD = 30L * SECOND;

    private static final long STATIC_THRESHOLD_TIME = 15L * MINUTE; // if static for more than x minutes set anchor mode

    private static final double MOVE_THRESHOLD_SPEED_KN = 3.0; // if reported is greater than X then it's moving
    private static final double MOVE_THRESHOLD_POS_METERS = 35.0; // if move by X meters since last reported point then it's moving

    public TrackManagerImpl() {
        this(false);
    }

    public TrackManagerImpl(boolean reportAll) {
        period = DEFAULT_PERIOD;
        staticPeriod = STATIC_DEFAULT_PERIOD;
        maxSpeed = 0.0;
        stationaryStatus = new StationaryManager();
        stats = new PositionStats();
        this.reportAll = reportAll;
    }

    /**
     * Sampling period when cruising
     *
     * @return milliseconds
     */
    @Override
    public long getPeriod() {
        return period;
    }

    @Override
    public boolean isStationary() {
        return stationaryStatus.stationary;
    }

    private boolean isFirstReport() {
        return (getLastTrackedPosition() == null);
    }

    private boolean shallReport(GeoPositionT p) {
        if (reportAll)
            return true;
        else {
            boolean anchor = stationaryStatus.isAnchor(p.getTimestamp());
            long dt = p.getTimestamp() - getLastTrackedTime();
            long checkPeriod = (anchor ? getStaticPeriod() : getPeriod());
            return dt >= checkPeriod;
        }
    }

    @Override
    public TrackPoint processPosition(GeoPositionT p, double sog) {

        stats.addPosition(p);

        maxSpeed = Math.max(maxSpeed, sog);
        TrackPoint res = null;

        if (isFirstReport()) {
            stationaryStatus.init();
            if (lastPoint != null) {
                stationaryStatus.updateStationaryStatus(p.getTimestamp(), isStationary(lastPoint, p));
                setLastTrackedPosition(p);
                maxSpeed = sog;
                res = fillPoint(stationaryStatus.isAnchor(p.getTimestamp()), lastPoint, p);
            }
        } else {
            long dt = p.getTimestamp() - getLastTrackedTime();
            if (dt >= getPeriod()) {
                stationaryStatus.updateStationaryStatus(p.getTimestamp(), isStationary(getLastTrackedPosition(), p));
                if (shallReport(p)) {
                    boolean anchor = stationaryStatus.isAnchor(p.getTimestamp());
                    GeoPositionT posToTrack = anchor ? new GeoPositionT(p.getTimestamp(), stats.getAveragePosition()) : p;
                    res = fillPoint(anchor, getLastTrackedPosition(), posToTrack);
                    setLastTrackedPosition(p);
                    maxSpeed = 0.0; // reset maxSpeed for the new sampling period
                }
            }
        }
        lastPoint = p;

        return res;
    }

    private TrackPoint fillPoint(boolean anchor, GeoPositionT prevPos, GeoPositionT p) {
        Course c = new Course(prevPos, p);
        double speed = c.getSpeed();
        speed = Double.isNaN(speed) ? 0.0 : speed;
        double dist = c.getDistance();
        dist = Double.isNaN(speed) ? 0.0 : dist;
        int timePeriod = (int) (c.getInterval() / 1000);
        TrackPointBuilder builder = ThingsFactory.getInstance(TrackPointBuilder.class);
        return builder.withPosition(p).withAnchor(anchor).withDistance(dist).withSpeed(speed, maxSpeed)
                .withPeriod(timePeriod).getPoint();
    }

    /**
     * Set the sampling time in ms.
     *
     * @param period The sampling period for normal navigation
     */
    @Override
    public void setPeriod(long period) {
        this.period = period;
    }

    @Override
    public long getStaticPeriod() {
        return staticPeriod;
    }

    /**
     * Set the sampling time in ms.
     *
     * @param period The sampling period when at anchor
     */
    @Override
    public void setStaticPeriod(long period) {
        this.staticPeriod = period;
    }

    private long getLastTrackedTime() {
        return (lastTrackedPoint != null) ? lastTrackedPoint.getTimestamp() : 0;
    }

    private void setLastTrackedPosition(GeoPositionT lastKnownPosition) {
        lastTrackedPoint = lastKnownPosition;
    }

    @Override
    public GeoPositionT getLastTrackedPosition() {
        return lastTrackedPoint;
    }

    private boolean isStationary(GeoPositionT p1, GeoPositionT p2) {
        if (p1 == null || p2 == null) {
            return false;
        } else {
            double dist = p1.distanceTo(p2); // distance in meters
            long dTime = Math.abs(p2.getTimestamp() - p1.getTimestamp()); // d-time in milliseconds
            // calc the speed but only if the two points are at least 500ms apart
            double speed =
                    dTime > 500 ? ((dist / dTime) * 1000.0) : 0.0; // meter/second
            speed *= 1.94384; // speed in knots
            return speed <= MOVE_THRESHOLD_SPEED_KN && dist < MOVE_THRESHOLD_POS_METERS;
        }
    }

    @Override
    public Position getAverage() {
        return stats.getAveragePosition();
    }

    private static class StationaryManager {

        boolean initialized = false;
        boolean stationary = true;
        long stationarySince = 0;

        void init() {
            if (!initialized) {
                this.stationary = true;
                initialized = true;
            }
        }

        boolean isAnchor(long t) {
            return stationary && ((t - stationarySince) > STATIC_THRESHOLD_TIME);
        }

        void updateStationaryStatus(long t, boolean stationary) {
            if (this.stationary != stationary) {
                this.stationarySince = stationary ? t : 0;
                this.stationary = stationary;
            }
        }
    }
}
