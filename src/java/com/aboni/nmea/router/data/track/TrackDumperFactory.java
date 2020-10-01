package com.aboni.nmea.router.data.track;

public interface TrackDumperFactory {
    TrackDumper getDumper(String type);
}
