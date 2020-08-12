package com.aboni.nmea.router.data.meteo;

import com.aboni.misc.Utils;

public class WindStats {

    private final double[] m;
    private double max = 0.0;
    private double tot = 0.0;
    private long totalTime = 0;
    private final int sectors;
    private final double sectorSize;

    public WindStats(int sectors) {
        this.sectors = sectors;
        this.sectorSize = (360.0 / sectors);
        this.m = new double[sectors];
    }

    public WindStats() {
        this.sectors = 360;
        this.sectorSize = 1.0;
        this.m = new double[360];
    }

    public void addSample(int intervalInSecond, double angle, double windSpeedInKnots) {
        angle = Utils.normalizeDegrees0To360(angle);
        int iAngle = (int) Math.round(angle / sectorSize) % sectors;
        double dist = (intervalInSecond / 3600.0) * windSpeedInKnots;
        m[iAngle] += dist;
        tot += dist;
        max = Math.max(m[iAngle], max);
        totalTime += intervalInSecond;

    }

    public double getWindDistance(int sector) {
        sector = ((sector % sectors) + sectors) % sectors;
        return m[sector];
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
