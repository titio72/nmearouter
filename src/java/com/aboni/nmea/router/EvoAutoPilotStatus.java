package com.aboni.nmea.router;

import com.aboni.nmea.router.message.PilotMode;

public interface EvoAutoPilotStatus {
    double getApHeading();

    double getApLockedHeading();

    double getApWindDatum();

    double getApAverageWind();

    PilotMode getMode();
}
