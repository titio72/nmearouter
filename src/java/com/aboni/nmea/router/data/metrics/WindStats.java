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

package com.aboni.nmea.router.data.metrics;

import com.aboni.utils.Utils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

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

    public JSONObject toJSON() {
        JSONObject res = new JSONObject();
        List<JSONObject> l = new ArrayList<>(360);
        for (int i = 0; i < sectors; i++) {
            JSONObject sample = new JSONObject();
            sample.put("angle", i * (360 / sectors));
            sample.put("windDistance", Utils.round(getWindDistance(i), 1));
            sample.put("windMaxSpeed", Utils.round(getWindMaxSpeed(i), 1));
            if (getWindTime(i) != 0)
                sample.put("windAvgSpeed", Utils.round(getWindDistance(i) / (getWindTime(i) / 3600.0), 1));
            else
                sample.put("windAvgSpeed", 0.0);
            l.add(sample);
        }
        res.put("values", new JSONArray(l));
        res.put("interval", getTotalTime());
        res.put("maxValue", Utils.round(getMaxWindDistance(), 1));
        res.put("maxValueH", Utils.round(getMaxWindDistance() / (getTotalTime() / 3600.0), 1));
        res.put("tot", getTot());
        return res;
    }
}
