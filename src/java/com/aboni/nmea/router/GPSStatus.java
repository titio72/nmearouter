package com.aboni.nmea.router;

import com.aboni.geo.GeoPositionT;
import com.aboni.nmea.router.agent.impl.NMEAGPSStatusAgent;
import net.sf.marineapi.nmea.util.Position;

import java.time.Instant;
import java.util.List;

public interface GPSStatus {
    NMEAGPSStatusAgent.GPSFix getGPSFix();

    double getHDOP();

    GeoPositionT getPosition();

    double getCOG();

    double getSOG();

    Instant getPositionTime();

    Position getAveragePosition();

    boolean isAnchor(long now);

    int getUsedSatellites();

    List<NMEAGPSStatusAgent.SatInfo> getSatellites();
}
