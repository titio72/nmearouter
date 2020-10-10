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

package com.aboni.nmea.router.data.meteo;

import com.aboni.misc.Utils;

public class WindStats {

    private final double[] m;
    private final double[] s;
    private final long[] t;
    private double max = 0.0;
    private double tot = 0.0;
    private long totalTime = 0;
    private final int sectors;
    private final double sectorSize;

    public WindStats(int sectors) {
        this.sectors = sectors;
        this.sectorSize = (360.0 / sectors);
        this.m = new double[sectors];
        this.s = new double[sectors];
        this.t = new long[sectors];
    }

    public void addSample(int intervalInSecond, double angle, double windSpeedInKnots) {
        angle = Utils.normalizeDegrees0To360(angle);
        int iAngle = (int) Math.round(angle / sectorSize) % sectors;
        double dist = (intervalInSecond / 3600.0) * windSpeedInKnots;
        m[iAngle] += dist;
        s[iAngle] = Math.max(s[iAngle], windSpeedInKnots);
        t[iAngle] += intervalInSecond;
        tot += dist;
        max = Math.max(m[iAngle], max);
        totalTime += intervalInSecond;

    }

    public long getWindTime(int sector) {
        sector = ((sector % sectors) + sectors) % sectors;
        return t[sector];
    }

    public double getWindDistance(int sector) {
        sector = ((sector % sectors) + sectors) % sectors;
        return m[sector];
    }

    public double getWindMaxSpeed(int sector) {
        sector = ((sector % sectors) + sectors) % sectors;
        return s[sector];
    }

    public long getTotalTime() {
        return totalTime;
    }

    public double getMaxWindDistance() {
        return max;
    }

    public double getTot() {
        return tot;
    }
}
