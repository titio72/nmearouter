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
import com.aboni.nmea.router.TimestampProvider;
import com.aboni.nmea.router.message.PilotMode;
import com.aboni.nmea.router.n2k.EVO;
import com.aboni.nmea.router.n2k.N2KMessage;
import com.aboni.utils.Log;
import org.json.JSONObject;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.sql.Time;

public class AutoPilotService extends JSONWebService {

    private final AutoPilotDriver auto;

    private class EvoAPDriver implements AutoPilotDriver {

        private final EVO evo;

        private EvoAPDriver(TimestampProvider tp) {
            evo = new EVO(tp);
        }

        @Override
        public void setAuto() {
            N2KMessage m = evo.getAUTOMessage();
            String url = "192.168.3.99/message?"
        }

        @Override
        public void setStdby() {

        }

        @Override
        public void setWindVane() {

        }

        @Override
        public void port1() {

        }

        @Override
        public void port10() {

        }

        @Override
        public void starboard1() {

        }

        @Override
        public void starboard10() {

        }

        @Override
        public PilotMode getMode() {
            return null;
        }

        @Override
        public JSONObject getModeDescription() {
            return null;
        }
    }



    @Inject
    public AutoPilotService(@NotNull TimestampProvider tp, @NotNull Log log) {
        super(log);
        this.auto = new EvoAPDriver(tp);
        setLoader((ServiceConfig config) -> {
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
                    auto.setAuto();
                    break;
                case CMD_STANDBY:
                    auto.setStdby();
                    break;
                case CMD_WIND_VANE:
                    auto.setWindVane();
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
    private static final String CMD_AUTO = "Auto";
    private static final String CMD_STANDBY = "Standby";
    private static final String CMD_WIND_VANE = "Wind";

}
