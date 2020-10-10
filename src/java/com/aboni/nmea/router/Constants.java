/*
(C) 2020, Andrea Boni
This file is part of NMEARouter.
NMEARouter is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.
NMEARouter is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.
You should have received a copy of the GNU General Public License
along with NMEARouter.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.aboni.nmea.router;

public class Constants {

    private Constants() {
    }

    public static final String LOG = "log/router.log";
    public static final String CONF_DIR = "conf";
    public static final String ROUTER_CONF_JSON = CONF_DIR + "/router.json";
    public static final String WMM = CONF_DIR + "/WMM.cof";
    public static final String DEVIATION = CONF_DIR + "/deviation.csv";
    public static final String SENSOR = CONF_DIR + "/sensors.properties";
    public static final String DB = CONF_DIR + "/db.properties";

    public static final String TAG_TRACK = "track";
    public static final String TAG_TRIP = "trip";
    public static final String TAG_SPEED = "speed";
    public static final String TAG_METEO = "meteo";
    public static final String TAG_AGENT = "agent";

    public static final String TAG_JSON = "json";

}
