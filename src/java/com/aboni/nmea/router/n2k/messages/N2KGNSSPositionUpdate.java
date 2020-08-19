package com.aboni.nmea.router.n2k.messages;

import net.sf.marineapi.nmea.util.Position;

import java.time.Instant;

public interface N2KGNSSPositionUpdate {

    int PGN = 129029;

    int getSID();

    boolean isValidSID();

    Instant getTimestamp();

    Position getPosition();

    double getAltitude();

    boolean isValidAltitude();

    String getGnssType();

    String getMethod();

    String getIntegrity();

    int getNSatellites();

    boolean isValidNSatellites();

    double getHDOP();

    boolean isHDOP();

    double getPDOP();

    boolean isPDOP();

    double getGeoidalSeparation();

    boolean isValidGeoidalSeparation();

    int getReferenceStations();

    String getReferenceStationType();

    int getReferenceStationId();

    double getAgeOfDgnssCorrections();

    boolean isValidAgeOfDgnssCorrections();
}
