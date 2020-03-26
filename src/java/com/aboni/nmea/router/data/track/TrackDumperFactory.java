package com.aboni.nmea.router.data.track;

import com.aboni.nmea.router.data.track.impl.Track2GPX;
import com.aboni.nmea.router.data.track.impl.Track2JSON;
import com.aboni.nmea.router.data.track.impl.Track2KML;
import com.aboni.utils.ThingsFactory;

public class TrackDumperFactory {

    private TrackDumperFactory() {
    }

    public static TrackDumper getDumper(String type) {
        switch (type) {
            case "gpx":
                return ThingsFactory.getInstance(Track2GPX.class);
            case "kml":
                return ThingsFactory.getInstance(Track2KML.class);
            case "json":
                return ThingsFactory.getInstance(Track2JSON.class);
            default:
                return null;
        }
    }
}
