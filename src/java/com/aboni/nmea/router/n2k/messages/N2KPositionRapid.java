package com.aboni.nmea.router.n2k.messages;

import net.sf.marineapi.nmea.util.Position;

public interface N2KPositionRapid {

    int PGN = 129025;

    Position getPosition();
}