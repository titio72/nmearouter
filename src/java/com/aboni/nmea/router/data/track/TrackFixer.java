/*
 * Copyright (c) 2022,  Andrea Boni
 * This file is part of NMEARouter.
 * NMEARouter is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * NMEARouter is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with NMEARouter.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.aboni.nmea.router.data.track;

import com.aboni.geo.Course;
import com.aboni.geo.GeoPositionT;

public class TrackFixer {
    private double totDist;
    private int i = 0;
    private int points = 0;
    private GeoPositionT last = null;

    public TrackPoint onTrackPoint(TrackPointBuilder builder, TrackPoint point) {
        TrackPoint res = null;
        points++;
        if (last != null) {
            Course c = new Course(last, point.getPosition());
            double intervalH = (c.getInterval()) / (1000.0 * 60.0 * 60.0); // interval in hours
            if (intervalH < 5 /* less than 5 hours between two consecutive points */ &&
                    Math.abs(point.getPeriod() * 1000L - c.getInterval()) > 2000L /* the dTime is different from the difference of timestamps */) {
                double speed = c.getSpeed();
                double maxSpeed = point.getMaxSpeed();
                if ((maxSpeed - speed) < 0.01) {
                    // the existing maxSpeed is lower than the calculated average speed or too close, so replace
                    // with the calculated average speed
                    maxSpeed = speed;
                }
                // update point
                res = builder.getNew().
                        withPosition(point.getPosition()).
                        withAnchor(point.isAnchor()).
                        withEngine(point.getEngine()).
                        withDistance(c.getDistance()).
                        withSpeed(c.getSpeed(), maxSpeed).
                        withPeriod((int) c.getInterval() / 1000).
                        getPoint();
                i++;
            }
        }
        if (!point.isAnchor()) totDist += (res == null) ? point.getDistance() : res.getDistance();
        last = point.getPosition();
        return res;
    }

    public double getTotDist() {
        return totDist;
    }

    public int getChangedPoints() {
        return i;
    }

    public int getPoints() {
        return points;
    }
}
