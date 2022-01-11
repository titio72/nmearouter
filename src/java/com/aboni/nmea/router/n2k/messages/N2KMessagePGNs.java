/*
 * Copyright (c) 2020,  Andrea Boni
 * This file is part of NMEARouter.
 * NMEARouter is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * NMEARouter is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with NMEARouter.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.aboni.nmea.router.n2k.messages;

public class N2KMessagePGNs {

    private N2KMessagePGNs() {
    }

    public static final int ATTITUDE_PGN = 127257;
    public static final int ENVIRONMENT_TEMPERATURE_PGN = 130312;
    public static final int ENVIRONMENT_HUMIDITY_PGN = 130313;
    public static final int ENVIRONMENT_PRESSURE_PGN = 130314;
    public static final int GNSS_POSITION_UPDATE_PGN = 129029;
    public static final int POSITION_UPDATE_RAPID = 129025;
    public static final int NAV_DATA = 129284;
    public static final int RATE_OF_TURN_PGN = 127251;
    public static final int RUDDER_PGN = 127245;
    public static final int SATELLITES_IN_VIEW_PGN = 129540;
    public static final int SEATALK_PILOT_HEADING_PGN = 65359;
    public static final int SEATALK_PILOT_LOCKED_HEADING_PGN = 65360;
    public static final int SEATALK_PILOT_MODE_PGN = 65379;
    public static final int SEATALK_PILOT_WIND_DATUM_PGN = 65345;
    public static final int SEATALK_ALARM_PGN = 65288;
    public static final int SPEED_PGN = 128259;
    public static final int SOG_COG_RAPID_PGN = 129026;
    public static final int SYSTEM_TIME_PGN = 126992;
    public static final int DEPTH_PGN = 128267;
    public static final int WIND_PGN = 130306;
    public static final int HEADING_PGN = 127250;
    public static final int GNSS_DOP_PGN = 129539;
    public static final int BATTERY_PGN = 127508;
    public static final int DC_DETAILED_STATUS_PGN = 127506;

    public static final int AIS_ATON_PGN = 129041;
    public static final int AIS_POSITION_REPORT_CLASS_A_PGN = 129038;
    public static final int AIS_POSITION_REPORT_CLASS_B_PGN = 129039;
    public static final int AIS_POSITION_REPORT_CLASS_B_EXT_PGN = 129040;
    public static final int AIS_STATIC_DATA_CLASS_A_PGN = 129794;
    public static final int AIS_STATIC_DATA_CLASS_B_PART_A_PGN = 129809;
    public static final int AIS_STATIC_DATA_CLASS_B_PART_B_PGN = 129810;
    public static final int AIS_UTC_REPORT_PGN = 129793;
}
