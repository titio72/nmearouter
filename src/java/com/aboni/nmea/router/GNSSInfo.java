package com.aboni.nmea.router;

import net.sf.marineapi.nmea.util.Position;

public interface GNSSInfo {

    Position getPosition();

    double getCOG();

    double getSOG();
}
