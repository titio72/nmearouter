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
import com.aboni.nmea.router.EvoAutoPilotStatus;
import com.aboni.nmea.router.NMEARouter;
import com.aboni.nmea.router.TimestampProvider;
import com.aboni.nmea.router.agent.NMEAAgent;
import com.aboni.nmea.router.impl.EvoAPDriver;
import com.aboni.utils.Log;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;

public class AutoPilotService extends JSONWebService {

    private AutoPilotDriver auto;
    private final NMEARouter router;
    private final Log log;
    private final TimestampProvider tp;

    private AutoPilotDriver getDriver() {
        if (auto==null) {
            for (String agentName : router.getAgents()) {
                NMEAAgent agent = router.getAgent(agentName);
                if (agent instanceof EvoAutoPilotStatus) {
                    auto = new EvoAPDriver(log, (EvoAutoPilotStatus)agent, tp);
                }
            }
        }
        return auto;
    }

    @Inject
    public AutoPilotService(NMEARouter router, @NotNull TimestampProvider tp, @NotNull Log log) {
        super(log);
        this.log = log;
        this.router = router;
        this.tp = tp;
        setLoader((ServiceConfig config) -> {
            String command = config.getParameter("command");
            switch (command) {
                case CMD_PORT_10:
                    getDriver().port10();
                    break;
                case CMD_PORT_1:
                    getDriver().port1();
                    break;
                case CMD_STARBOARD_10:
                    getDriver().starboard10();
                    break;
                case CMD_STARBOARD_1:
                    getDriver().starboard1();
                    break;
                case CMD_AUTO:
                    getDriver().setAuto();
                    break;
                case CMD_STANDBY:
                    getDriver().setStandby();
                    break;
                case CMD_WIND_VANE:
                    getDriver().setWindVane();
                    break;
                default:
                    break;
            }
            return getOk();
        });
    }

    private static final String CMD_STARBOARD_1 = "S1";
    private static final String CMD_PORT_1 = "P1";
    private static final String CMD_STARBOARD_10 = "S10";
    private static final String CMD_PORT_10 = "P10";
    private static final String CMD_AUTO = "AUTO";
    private static final String CMD_STANDBY = "STANDBY";
    private static final String CMD_WIND_VANE = "VANE";

}
