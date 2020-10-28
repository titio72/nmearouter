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

import com.aboni.misc.Utils;
import com.aboni.nmea.router.AutoPilotDriver;
import com.aboni.nmea.router.EvoAutoPilotStatus;
import com.aboni.nmea.router.NMEARouter;
import com.aboni.nmea.router.TimestampProvider;
import com.aboni.nmea.router.n2k.EVO;
import com.aboni.nmea.router.n2k.N2KMessage;
import com.aboni.utils.Log;
import com.aboni.utils.LogStringBuilder;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class AutoPilotService extends JSONWebService {

    public static final String AP_DRIVER = "APDriver";
    private final AutoPilotDriver auto;
    private final Log log;
    private final EvoAutoPilotStatus status;
    private class EvoAPDriver implements AutoPilotDriver {

        private final EVO evo;

        private EvoAPDriver(TimestampProvider tp) {
            evo = new EVO(tp);
        }

        @Override
        public void setAuto() {
            N2KMessage m = evo.getAUTOMessage();
            try {
                LogStringBuilder.start(AP_DRIVER).wO("Set AUTO").info(log);
                handle(m);
            } catch (Exception e) {
                LogStringBuilder.start(AP_DRIVER).wO("Set AUTO").error(log, e);
            }
        }

        private void handle(N2KMessage m) throws IOException {
            String sURL = "http://192.168.3.99/message?pgn=126208&src=1&dest=204&priority=3&data=";
            StringBuilder builder = new StringBuilder(sURL);
            for (byte b: m.getData()) builder.append(String.format("%02x", b));
            sURL = builder.toString();
            URL url = new URL(sURL);
            HttpURLConnection con = (HttpURLConnection)url.openConnection();
            con.setRequestMethod("GET");
            con.setConnectTimeout(5000);
            con.setReadTimeout(5000);
            int res = con.getResponseCode();
            if (res==200) {
                LogStringBuilder.start(AP_DRIVER).wO("send msg").wV("message", sURL).wV("res", res).info(log);
            } else {
                LogStringBuilder.start(AP_DRIVER).wO("send msg").wV("message", sURL).wV("res", res).warn(log);
            }
            con.disconnect();
        }

        @Override
        public void setStdby() {
            N2KMessage m = evo.getSTDBYMessage();
            try {
                LogStringBuilder.start(AP_DRIVER).wO("Set STANDBY").info(log);
                handle(m);
            } catch (Exception e) {
                LogStringBuilder.start(AP_DRIVER).wO("Set STANDBY").error(log, e);
            }
        }

        @Override
        public void setWindVane() {
            N2KMessage m = evo.getVANEMessage();
            try {
                LogStringBuilder.start(AP_DRIVER).wO("Set WINDVANE").info(log);
                handle(m);
            } catch (Exception e) {
                LogStringBuilder.start(AP_DRIVER).wO("Set WINDVANE").error(log, e);
            }
        }

        private void setHeading(double head) {
            head = Utils.normalizeDegrees0To360(Math.round(head));
            N2KMessage m = evo.getLockHeadingMessage(head);
            try {
                LogStringBuilder.start(AP_DRIVER).wO("Set locked heading").wV("head", head).info(log);
                handle(m);
            } catch (Exception e) {
                LogStringBuilder.start(AP_DRIVER).wO("Set Set locked heading").error(log, e);
            }
        }

        private void handlePlusMinus(int incr) {
            double datum = status.getApWindDatum();
            if (!Double.isNaN(datum)) {
                datum += incr;
                setWindDatum(datum);
            } else {
                double head = status.getApLockedHeading();
                if (!Double.isNaN(head)) {
                    head += incr;
                    setHeading(head);
                }
            }
        }

        private void setWindDatum(double datum) {
            datum = Utils.normalizeDegrees0To360(Math.round(datum));
            N2KMessage m = evo.getWindDatumMessage(datum);
            try {
                LogStringBuilder.start(AP_DRIVER).wO("Set wind datum").wV("wind datum", datum).info(log);
                handle(m);
            } catch (Exception e) {
                LogStringBuilder.start(AP_DRIVER).wO("Set wind datum").error(log, e);
            }
        }

        @Override
        public void port1() {
            handlePlusMinus(-1);
        }

        @Override
        public void port10() {
            handlePlusMinus(-10);
        }

        @Override
        public void starboard1() {
            handlePlusMinus(1);
        }

        @Override
        public void starboard10() {
            handlePlusMinus(10);
        }
    }

    @Inject
    public AutoPilotService(NMEARouter router, @NotNull TimestampProvider tp, @NotNull Log log) {
        super(log);
        this.log = log;
        this.auto = new EvoAPDriver(tp);
        status = (EvoAutoPilotStatus)router.getAgent("EvoAP");
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
    private static final String CMD_AUTO = "AUTO";
    private static final String CMD_STANDBY = "STANDBY";
    private static final String CMD_WIND_VANE = "VANE";

}
