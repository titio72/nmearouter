package com.aboni.nmea.router;

import com.aboni.nmea.message.PilotMode;

public interface EvoAutoPilotStatus {

    interface PilotStatusListener {
        void onPilotStatus(PilotMode previousMode, PilotMode newMode, double value, long timstamp);
    }

    void listen(PilotStatusListener listener);

    void stopListening(PilotStatusListener listener);

    double getApHeading();

    double getApLockedHeading();

    double getApWindDatum();

    double getApAverageWind();

    PilotMode getMode();
}
