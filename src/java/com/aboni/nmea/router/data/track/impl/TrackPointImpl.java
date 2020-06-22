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
import com.aboni.sensors.EngineStatus;

public class TrackPointImpl implements TrackPoint {

    private final GeoPositionT position;
    private final boolean anchor;
    private final double distance;
    private final double averageSpeed;
    private final double maxSpeed;
    private final int period;
    private final EngineStatus engine;

    TrackPointImpl(TrackPoint point) {
        this.position = point.getPosition();
        this.anchor = point.isAnchor();
        this.distance = point.getDistance();
        this.averageSpeed = point.getAverageSpeed();
        this.maxSpeed = point.getMaxSpeed();
        this.period = point.getPeriod();
        this.engine = point.getEngine();
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