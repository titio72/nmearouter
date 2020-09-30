package com.aboni.nmea.router;

public interface GPSSat {

    int getPrn();

    int getSvn();

    String getName();

    String getDate();

    String getOrbit();

    String getSignal();

    String getClock();

}
