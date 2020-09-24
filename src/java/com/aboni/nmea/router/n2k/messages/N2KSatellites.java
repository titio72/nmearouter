package com.aboni.nmea.router.n2k.messages;

import com.aboni.nmea.router.n2k.Satellite;

import java.util.List;

public interface N2KSatellites {

    int getSID();

    int getNumberOfSats();

    List<Satellite> getSatellites();
}
