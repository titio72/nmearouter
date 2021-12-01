/*
 * Copyright (c) 2021,  Andrea Boni
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

package com.aboni.nmea.router.impl;

import com.aboni.misc.Utils;
import com.aboni.nmea.router.AutoPilotDriver;
import com.aboni.nmea.router.EvoAutoPilotStatus;
import com.aboni.nmea.router.TimestampProvider;
import com.aboni.nmea.router.message.PilotMode;
import com.aboni.nmea.router.n2k.N2KMessage;
import com.aboni.nmea.router.n2k.N2KMessageHandler;
import com.aboni.nmea.router.n2k.N2KMessageHeader;
import com.aboni.nmea.router.n2k.evo.EVO;
import com.aboni.nmea.router.n2k.evo.EVOImpl;
import com.aboni.utils.Log;
import com.aboni.utils.LogStringBuilder;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class EvoAPDriver implements AutoPilotDriver {

    public static final String AP_DRIVER = "APDriver";
    private final EvoAutoPilotStatus evoAutoPilotStatus;
    private final EVO evo;
    private final Log log;
    private static final int SOURCE = 1;
    private static final String DRIVER_HOST = "192.168.3.99";
    private final TimestampProvider tp;
    private final N2KMessageHandler msgSender;

    private class Request {
        double requestValue = Double.NaN;
        long requestTime = 0;

        public double getValue(long now) {
            if (Utils.isOlderThan(requestTime, now, 500)) {
                if (evoAutoPilotStatus.getMode()==PilotMode.AUTO) return evoAutoPilotStatus.getApLockedHeading();
                else if (evoAutoPilotStatus.getMode()==PilotMode.VANE) return evoAutoPilotStatus.getApWindDatum();
                else return Double.NaN;
            } else {
                return requestValue;
            }
        }

        public void update(double head, long now) {
            requestTime = now;
            requestValue = head;
        }
    }

    private final Request windRequest = new Request();
    private final Request headRequest = new Request();

    @Inject
    public EvoAPDriver(@NotNull Log log, @NotNull EvoAutoPilotStatus autoPilotStatus, @NotNull TimestampProvider tp) {
        this.log = log;
        this.tp = tp;
        this.evoAutoPilotStatus = autoPilotStatus;
        this.msgSender = this::sendMessageToPilot;
        evo = new EVOImpl(tp, SOURCE);
    }

    public EvoAPDriver(@NotNull Log log, @NotNull EvoAutoPilotStatus autoPilotStatus,
                       @NotNull TimestampProvider tp, @NotNull N2KMessageHandler msgSender) {
        this.log = log;
        this.tp = tp;
        this.evoAutoPilotStatus = autoPilotStatus;
        this.msgSender = msgSender;
        evo = new EVOImpl(tp, SOURCE);
    }

    @Override
    public void setAuto() {
        N2KMessage m = evo.getAUTOMessage();
        try {
            LogStringBuilder.start(AP_DRIVER).wO("Set AUTO").info(log);
            msgSender.onMessage(m);
        } catch (Exception e) {
            LogStringBuilder.start(AP_DRIVER).wO("Set AUTO").error(log, e);
        }
    }


    @Nonnull
    private String getURL(N2KMessage m) {
        N2KMessageHeader h = m.getHeader();
        String sURL = String.format("http://%s/message?pgn=%d&src=%d&dest=%d&priority=%d&data=",
                DRIVER_HOST, h.getPgn(), h.getSource(), h.getDest(), h.getPriority());
        StringBuilder builder = new StringBuilder(sURL);
        for (byte b : m.getData()) builder.append(String.format("%02x", b));
        sURL = builder.toString();
        return sURL;
    }

    @Override
    public void setStandby() {
        N2KMessage m = evo.getSTDBYMessage();
        LogStringBuilder.start(AP_DRIVER).wO("Set STANDBY").info(log);
        msgSender.onMessage(m);
    }

    @Override
    public void setWindVane() {
        N2KMessage m = evo.getVANEMessage();
        LogStringBuilder.start(AP_DRIVER).wO("Set WIND VANE").info(log);
        msgSender.onMessage(m);
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

    private void handlePlusMinus(int increment) {
        if (evoAutoPilotStatus.getMode()==PilotMode.AUTO) {
            long now = tp.getNow();
            double v = headRequest.getValue(now);
            if (!Double.isNaN(v)) {
                v += increment;
                setHeading(v);
            }
            headRequest.update(v, now);
        } else if (evoAutoPilotStatus.getMode()==PilotMode.VANE) {
            long now = tp.getNow();
            double datum = windRequest.getValue(now);
            if (!Double.isNaN(datum)) {
                datum -= increment;
                setWindDatum(datum);
            }
            windRequest.update(datum, now);
        }
    }

    private void setHeading(double head) {
        head = Utils.normalizeDegrees0To360(Math.round(head));
        N2KMessage m = evo.getLockHeadingMessage(head);
        try {
            LogStringBuilder.start(AP_DRIVER).wO("Set locked heading").wV("head", head).info(log);
            msgSender.onMessage(m);
        } catch (Exception e) {
            LogStringBuilder.start(AP_DRIVER).wO("Set Set locked heading").error(log, e);
        }
    }

    private void setWindDatum(double datum) {
        datum = Utils.normalizeDegrees0To360(Math.round(datum));
        N2KMessage m = evo.getWindDatumMessage(datum);
        try {
            LogStringBuilder.start(AP_DRIVER).wO("Set wind datum").wV("wind datum", datum).info(log);
            msgSender.onMessage(m);
        } catch (Exception e) {
            LogStringBuilder.start(AP_DRIVER).wO("Set wind datum").error(log, e);
        }
    }

    private void sendMessageToPilot(N2KMessage m) {
        try {
            String sURL = getURL(m);
            URL url = new URL(sURL);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setConnectTimeout(5000);
            con.setReadTimeout(5000);
            int res = con.getResponseCode();
            if (res == 200) {
                LogStringBuilder.start(AP_DRIVER).wO("send msg").wV("message", sURL).wV("res", res).info(log);
            } else {
                LogStringBuilder.start(AP_DRIVER).wO("send msg").wV("message", sURL).wV("res", res).warn(log);
            }
            con.disconnect();
        } catch (IOException e) {
            LogStringBuilder.start(AP_DRIVER).wO("send msg to AP").wV("msg", m).error(log, e);
        }
    }
}
