package com.aboni.nmea.router.n2k.messages;

import com.aboni.nmea.router.n2k.PilotMode;

public interface N2KSeatalkPilotMode {

    int PGN = 65379;

    PilotMode getMode();
}
