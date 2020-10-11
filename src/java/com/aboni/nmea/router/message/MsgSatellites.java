package com.aboni.nmea.router.message;

import java.util.List;

public interface MsgSatellites extends Message {

    int getSID();

    int getNumberOfSats();

    List<Satellite> getSatellites();
}
