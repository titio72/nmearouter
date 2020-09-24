package com.aboni.nmea.router.n2k.messages;

import net.sf.marineapi.nmea.util.Position;

import java.time.Instant;

public interface N2KGNSSPositionUpdate {

    int getSID();

    boolean isValidSID();

    Instant getTimestamp();

    Position getPosition();

    double getAltitude();

    String getGnssType();

    String getMethod();

    String getIntegrity();

    /**
     * Number of satellites used for the position calc.
     *
     * @return N of sats is successful, 0xFF if not available
     */
    int getNSatellites();

    double getHDOP();

    boolean isHDOP();

    double getPDOP();

    boolean isPDOP();

    double getGeoidalSeparation();

    int getReferenceStations();

    String getReferenceStationType();

    int getReferenceStationId();

    double getAgeOfDgnssCorrections();
}
