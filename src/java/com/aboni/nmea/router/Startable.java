package com.aboni.nmea.router;

public interface Startable {

    void start();

    void stop();

    boolean isStarted();

}