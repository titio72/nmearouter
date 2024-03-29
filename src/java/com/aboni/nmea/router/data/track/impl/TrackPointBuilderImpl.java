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

import com.aboni.geo.GeoPositionT;
import com.aboni.nmea.router.data.track.TrackPoint;
import com.aboni.nmea.router.data.track.TrackPointBuilder;
import com.aboni.sensors.EngineStatus;

public class TrackPointBuilderImpl implements TrackPoint, TrackPointBuilder {

    private GeoPositionT position;
    private boolean anchor = false;
    private double distance;
    private double averageSpeed;
    private double maxSpeed;
    private int period = 30;
    private EngineStatus engine = EngineStatus.UNKNOWN;

    @Override
    public TrackPointBuilder getNew() {
        return new TrackPointBuilderImpl();
    }

    @Override
    public synchronized TrackPointBuilderImpl withPoint(TrackPoint point) {
        this.position = point.getPosition();
        this.anchor = point.isAnchor();
        this.distance = point.getDistance();
        this.averageSpeed = point.getAverageSpeed();
        this.maxSpeed = point.getMaxSpeed();
        this.period = point.getPeriod();
        this.engine = point.getEngine();
        return this;
    }

    @Override
    public synchronized TrackPointBuilder withPosition(GeoPositionT pos) {
        position = pos;
        return this;
    }

    @Override
    public synchronized TrackPointBuilder withSpeed(double speed, double maxSpeed) {
        this.maxSpeed = maxSpeed;
        this.averageSpeed = speed;
        return this;
    }

    @Override
    public synchronized TrackPointBuilder withAnchor(boolean anchor) {
        this.anchor = anchor;
        return this;
    }

    @Override
    public synchronized TrackPointBuilder withDistance(double distance) {
        this.distance = distance;
        return this;
    }

    @Override
    public synchronized TrackPointBuilder withPeriod(int periodSeconds) {
        this.period = periodSeconds;
        return this;
    }

    @Override
    public synchronized TrackPointBuilder withEngine(EngineStatus engine) {
        this.engine = engine;
        return this;
    }

    @Override
    public synchronized TrackPoint getPoint() {
        return new TrackPointImpl(this);
    }

    @Override
    public GeoPositionT getPosition() {
        return position;
    }

    @Override
    public boolean isAnchor() {
        return anchor;
    }

    @Override
    public double getDistance() {
        return distance;
    }

    @Override
    public double getAverageSpeed() {
        return averageSpeed;
    }

    @Override
    public double getMaxSpeed() {
        return maxSpeed;
    }

    @Override
    public int getPeriod() {
        return period;
    }

    @Override
    public EngineStatus getEngine() {
        return engine;
    }
}
