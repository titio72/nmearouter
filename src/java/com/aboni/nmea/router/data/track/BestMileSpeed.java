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

package com.aboni.nmea.router.data.track;

import com.aboni.utils.Pair;

import java.util.LinkedList;
import java.util.List;

class BestMileSpeed {

    private final double d;
    private final String tag;
    private final List<Pair<Long, Double>> sss = new LinkedList<>();
    private double distance = 0;

    private long t0Max = 0;
    private long t1Max = 0;
    private double speedMax = 0.0;

    public BestMileSpeed(double distance, String tag) {
        d = distance;
        this.tag = tag;
    }

    public void addSample(Pair<Long, Double> p) {
        sss.add(p);
        distance += p.second;
        if (distance > d) {
            // pull the first item from the rolling window
            Pair<Long, Double> p0 = sss.get(0);
            while (distance > d) {
                distance -= p0.second;
                sss.remove(0);
                if (distance>d) {
                    p0 = sss.get(0);
                }
            }

            long dT = p.first - p0.first;
            if (dT > 0) {
                double speed = distance / (dT / 3600000d);
                if (speed > speedMax) {
                    t0Max = p0.first;
                    t1Max = p.first;
                    speedMax = speed;
                }
            }
        }
    }

    public String getTag() {
        return tag;
    }

    public double getMaxSpeed() {
        return speedMax;
    }

    public long getMaxSpeedT0() {
        return t0Max;
    }

    public long getMaxSpeedT1() {
        return t1Max;
    }

    public boolean hasMax() {
        return t1Max!=0;
    }
}
