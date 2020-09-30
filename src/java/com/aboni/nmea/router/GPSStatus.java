package com.aboni.nmea.router;

import com.aboni.geo.GeoPositionT;

import java.time.Instant;
import java.util.List;

public interface GPSStatus {
    String getGPSFix();

    double getHDOP();

    GeoPositionT getPosition();

    double getCOG();

    double getSOG();

    Instant getPositionTime();

    int getUsedSatellites();

    List<SatInfo> getSatellites();
}
