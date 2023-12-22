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

package com.aboni.nmea.router.agent.impl;

import com.aboni.log.Log;
import com.aboni.nmea.nmea0183.Message2NMEA0183;
import com.aboni.nmea.router.NMEATrafficStats;
import com.aboni.nmea.router.OnRouterMessage;
import com.aboni.nmea.router.RouterMessage;
import com.aboni.nmea.router.RouterMessageFactory;
import com.aboni.nmea.router.conf.QOS;
import com.aboni.utils.TimestampProvider;
import net.sf.marineapi.nmea.sentence.Sentence;

import javax.inject.Inject;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;

public class NMEAUDPSender extends NMEAAgentImpl {

    private static final int FAST_STATS_PERIOD = 1; //number of timer ticks
    private static final int STATS_PERIOD = 60;

    private DatagramSocket serverSocket;
    private int portTarget = 1113;
    private final Set<InetAddress> targets;
    private boolean setup = false;
    private final Message2NMEA0183 converter;
    private final NMEATrafficStats fastStats;
    private final NMEATrafficStats stats;
    private String description;
    private String baseDescription;

    @Inject
    public NMEAUDPSender(Log log, TimestampProvider tp, RouterMessageFactory messageFactory, Message2NMEA0183 converter) {
        super(log, tp, messageFactory, false, true);
        if (converter==null) throw new IllegalArgumentException("NMEA converter cannot be null");
        this.converter = converter;
        targets = new HashSet<>();
        baseDescription = "UDP Sender";
        description = baseDescription;
        fastStats = new NMEATrafficStats(this::onFastStatsExpired, FAST_STATS_PERIOD, false, true);
        stats = new NMEATrafficStats(this::onStatsExpired, STATS_PERIOD, false, true);
    }

    private void onFastStatsExpired(NMEATrafficStats s, long time) {
        if (isStarted()) {
            synchronized (this) {
                description = baseDescription + "<br>" + s.toString(time);
            }
        } else {
            synchronized (this) {
                description = baseDescription;
            }
        }
    }

    private void onStatsExpired(NMEATrafficStats s, long time) {
        synchronized (this) {
            getLog().info(() -> getLogBuilder().wO("stats").w(s.toString(time)).toString());
        }
    }

    public void setup(String name, QOS qos, int port) {
        if (!setup) {
            setup = true;
            setup(name, qos);
            portTarget = port;

            baseDescription = "UDP Sender Port " + getPort() + "<br>";
            description = baseDescription;

            getLog().info(() -> getLogBuilder().wO("init").wV("port", portTarget).toString());
        } else {
            getLog().error(() -> getLogBuilder().wO("init").wV("error", "already initialized").toString());
        }
    }

    @Override
    public String getType() {
        return "UDP Sender";
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return "UDP " + getPort() + " T";
    }

    public int getPort() {
        return portTarget;
    }

    public void addTarget(String target) {
        try {
            InetAddress address = InetAddress.getByName(target);
            targets.add(address);
            baseDescription += " " + address.getHostName();
            description = baseDescription;
            getLog().info(() -> getLogBuilder().wO("init").wV("target", target).toString());
        } catch (UnknownHostException e) {
            getLog().error(() -> getLogBuilder().wO("init").wV("target", target).toString(), e);
        }
    }

    @Override
    protected boolean onActivate() {
        try {
            serverSocket = new DatagramSocket();
            return true;
        } catch (IOException e) {
            getLog().errorForceStacktrace(() -> getLogBuilder().wO("activate").toString(), e);
        }
        return false;
    }

    @Override
    protected void onDeactivate() {
        try {
            serverSocket.close();
        } catch (Exception e) {
            getLog().errorForceStacktrace(() -> getLogBuilder().wO("deactivate").toString(), e);
        }
    }

    private Sentence[] getSentenceToSend(RouterMessage rm) {
        return converter.convert(rm.getPayload());
    }

    @OnRouterMessage
    public void onMessage(RouterMessage rm) {
        Sentence[] sending = getSentenceToSend(rm);
        for (Sentence s: sending) {
            String toSend = getOutSentence(s);
            try {
                updateStats(toSend);
                for (InetAddress i : targets) {
                    byte[] bytes = toSend.getBytes();
                    DatagramPacket packet = new DatagramPacket(bytes, bytes.length, i, portTarget);
                    serverSocket.send(packet);
                }
                updateStats(false);
            } catch (IOException e) {
                updateStats(true);
                getLog().errorForceStacktrace(() -> getLogBuilder().wO("message").toString(), e);
            }
        }
    }

    private void updateStats(boolean fail) {
        synchronized (stats) {
            fastStats.updateWriteStats(fail);
            stats.updateWriteStats(fail);
        }
    }

    private void updateStats(String toSend) {
        synchronized (stats) {
            fastStats.updateWriteStats(toSend);
            stats.updateWriteStats(toSend);
        }
    }

    protected String getOutSentence(Sentence s) {
        return s.toSentence() + "\r\n";
    }

    @Override
    public void onTimer() {
        super.onTimer();
        if (isStarted()) {
            long t = getTimestampProvider().getNow();
            synchronized (stats) {
                stats.onTimer(t);
                fastStats.onTimer(t);
            }
        }
    }
}
