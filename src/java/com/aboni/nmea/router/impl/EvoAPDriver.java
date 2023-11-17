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

import com.aboni.nmea.message.PilotMode;
import com.aboni.nmea.n2k.N2KMessage;
import com.aboni.nmea.router.AutoPilotDriver;
import com.aboni.nmea.router.EvoAutoPilotStatus;
import com.aboni.nmea.n2k.N2KMessageHeader;
import com.aboni.utils.TimestampProvider;
import com.aboni.nmea.n2k.N2KMessageHandler;
import com.aboni.nmea.n2k.evo.EVO;
import com.aboni.nmea.n2k.evo.EVOImpl;
import com.aboni.log.Log;
import com.aboni.log.SafeLog;
import com.aboni.log.LogStringBuilder;
import com.aboni.utils.Utils;

import javax.inject.Inject;
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
                if (evoAutoPilotStatus.getMode()== PilotMode.AUTO) return evoAutoPilotStatus.getApLockedHeading();
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
    public EvoAPDriver(Log log, EvoAutoPilotStatus autoPilotStatus, TimestampProvider tp) {
        if (autoPilotStatus==null) throw new IllegalArgumentException("Autopilot driver is null");
        if (tp==null) throw new IllegalArgumentException("Timestamp provider is null");
        this.log = SafeLog.getSafeLog(log);
        this.tp = tp;
        this.evoAutoPilotStatus = autoPilotStatus;
        this.msgSender = this::sendMessageToPilot;
        evo = new EVOImpl(tp, SOURCE);
    }

    public EvoAPDriver(Log log, EvoAutoPilotStatus autoPilotStatus,
                       TimestampProvider tp, N2KMessageHandler msgSender) {
        if (autoPilotStatus==null) throw new IllegalArgumentException("Autopilot driver is null");
        if (tp==null) throw new IllegalArgumentException("Timestamp provider is null");
        if (msgSender==null) throw new IllegalArgumentException("N2K message sender is null");
        this.log = SafeLog.getSafeLog(log);
        this.tp = tp;
        this.evoAutoPilotStatus = autoPilotStatus;
        this.msgSender = msgSender;
        evo = new EVOImpl(tp, SOURCE);
    }

    @Override
    public void setAuto() {
        N2KMessage m = evo.getAUTOMessage();
        try {
            log.info(() -> LogStringBuilder.start(AP_DRIVER).wO("Set AUTO").toString());
            msgSender.onMessage(m);
        } catch (Exception e) {
            log.error(() -> LogStringBuilder.start(AP_DRIVER).wO("Set AUTO").toString(), e);
        }
    }

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
        log.info(() -> LogStringBuilder.start(AP_DRIVER).wO("Set STANDBY").toString());
        msgSender.onMessage(m);
    }

    @Override
    public void setWindVane() {
        N2KMessage m = evo.getVANEMessage();
        log.info(() -> LogStringBuilder.start(AP_DRIVER).wO("Set WIND VANE").toString());
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
        double dHead = Utils.normalizeDegrees0To360(Math.round(head));
        N2KMessage m = evo.getLockHeadingMessage(dHead);
        try {
            log.info(() -> LogStringBuilder.start(AP_DRIVER).wO("Set locked heading").wV("head", dHead).toString());
            msgSender.onMessage(m);
        } catch (Exception e) {
            log.error(() -> LogStringBuilder.start(AP_DRIVER).wO("Set Set locked heading").toString(), e);
        }
    }

    private void setWindDatum(double datum) {
        double dDatum = Utils.normalizeDegrees0To360(Math.round(datum));
        N2KMessage m = evo.getWindDatumMessage(dDatum);
        try {
            log.info(() -> LogStringBuilder.start(AP_DRIVER).wO("Set wind datum").wV("wind datum", dDatum).toString());
            msgSender.onMessage(m);
        } catch (Exception e) {
            log.error(() -> LogStringBuilder.start(AP_DRIVER).wO("Set wind datum").toString(), e);
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
                log.info(() -> LogStringBuilder.start(AP_DRIVER).wO("send msg").wV("message", sURL).wV("res", res).toString());
            } else {
                log.warning(() -> LogStringBuilder.start(AP_DRIVER).wO("send msg").wV("message", sURL).wV("res", res).toString());
            }
            con.disconnect();
        } catch (IOException e) {
            log.error(() -> LogStringBuilder.start(AP_DRIVER).wO("send msg to AP").wV("msg", m).toString(), e);
        }
    }
}
