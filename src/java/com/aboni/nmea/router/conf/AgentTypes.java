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

package com.aboni.nmea.router.conf;

public class AgentTypes {

    private AgentTypes() {
    }

    public static final String SIMULATOR_X = "SimulatorX";
    public static final String SIMULATOR = "Simulator";
    public static final String CONSOLE = "Console";
    public static final String METEO = "Meteo";
    public static final String TCP = "TCP";
    public static final String UDP = "UDP";
    public static final String TRACK = "Track";
    public static final String SERIAL = "Serial";
    public static final String CAN_SERIAL = "CanSerial";
    public static final String CAN_SOCKET = "CanSocket";
    public static final String GPS = "GPSStatus";
    public static final String SEATALK_ALARMS = "STAlarms";
    public static final String AIS = "AIS";
    public static final String SENSOR = "Sensor";
    public static final String GYRO = "Gyro";
    public static final String VOLT = "Voltage";
    public static final String JSON = "JSON";
    public static final String GPX_PLAYER = "GPXPlayerAgent";
    public static final String PLAYER = "PlayerAgent";
    public static final String NEXTION = "Nextion";
}
