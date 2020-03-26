package com.aboni.nmea.router;

public class Constants {

    private Constants() {
    }

    public static final String LOG = "log/router.log";
    public static final String CONF_DIR = "conf";
    public static final String ROUTER_CONF = CONF_DIR + "/router.xml";
    public static final String WMM = CONF_DIR + "/WMM.COF";
    public static final String DEVIATION = CONF_DIR + "/deviation.csv";
    public static final String SENSOR = CONF_DIR + "/sensors.properties";
    public static final String DB = CONF_DIR + "/db.properties";

    public static final String TAG_TRACK = "track";
    public static final String TAG_TRIP = "trip";
    public static final String TAG_SPEED = "speed";
    public static final String TAG_METEO = "meteo";
    public static final String TAG_AGENT = "agent";


}
