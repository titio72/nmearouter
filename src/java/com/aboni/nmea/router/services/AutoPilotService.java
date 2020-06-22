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

package com.aboni.nmea.router.services;

import com.aboni.nmea.router.AutoPilotDriver;
import com.aboni.nmea.router.NMEARouter;
import com.aboni.utils.ServerLog;

import javax.inject.Inject;

public class AutoPilotService implements WebService {

    private final AutoPilotDriver auto;

    @Inject
    public AutoPilotService(NMEARouter router) {
        this.auto = (AutoPilotDriver) router.getAgent("SmartPilot");
    }

    private static final String CMD_STARBOARD_1 = "S1";
    private static final String CMD_PORT_1 = "P1";
    private static final String CMD_STARBOARD_10 = "S10";
    private static final String CMD_PORT_10 = "P10";
    private static final String CMD_AUTO = "Auto";
    private static final String CMD_STANDBY = "Standby";
    private static final String CMD_WIND_VANE = "Wind";

    @Override
    public void doIt(ServiceConfig config, ServiceOutput response) {
        try {
            String command = config.getParameter("command");
            switch (command) {
                case CMD_PORT_10:
                    auto.port10();
                    break;
                case CMD_PORT_1:
                    auto.port1();
                    break;
                case CMD_STARBOARD_10:
                    auto.starboard10();
                    break;
                case CMD_STARBOARD_1:
                    auto.starboard1();
                    break;
                case CMD_AUTO:
                    auto.enable();
                    break;
                case CMD_STANDBY:
                    auto.standBy();
                    break;
                case CMD_WIND_VANE:
                    auto.windVane();
                    break;
                default:
                    break;
            }
            response.setContentType("application/json");
            response.getWriter().println("{}");
            response.ok();
        } catch (Exception e) {
            response.setContentType("application/json");
            try {
                response.getWriter().println("{\"error\", \"" + e.getMessage() + "\"}");
            } catch (Exception ee) {
                ServerLog.getLogger().error("Error sending response to web client", ee);
            }
            response.ok();
        }
    }
}
