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

import com.aboni.nmea.router.NMEACache;
import com.aboni.nmea.router.NMEATrafficStats;
import com.aboni.nmea.router.OnSentence;
import com.aboni.nmea.router.agent.QOS;
import com.aboni.utils.ServerLog;
import net.sf.marineapi.nmea.sentence.Sentence;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
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

    private final NMEATrafficStats fastStats;
    private final NMEATrafficStats stats;
    private String description;
    private String baseDescription;

    @Inject
    public NMEAUDPSender(@NotNull NMEACache cache) {
        super(cache);
        setSourceTarget(false, true);
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
            getLogger().info(s.toString(time));
        }
    }

    public void setup(String name, QOS qos, int port) {
        if (!setup) {
            setup = true;
            setup(name, qos);
            portTarget = port;

            baseDescription = "UDP Sender Port " + getPort() + "<br>";
            description = baseDescription;

            getLogger().info(String.format("Setting up UDP sender: Port {%d}", portTarget));
        } else {
            getLogger().info("Cannot setup UDP sender - already set up");
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
        } catch (UnknownHostException e) {
            getLogger().error("Invalid target {" + target + "}");
        }
    }

    @Override
    protected boolean onActivate() {
        try {
            serverSocket = new DatagramSocket();
            return true;
        } catch (IOException e) {
            ServerLog.getLogger().error("Cannot open datagram server", e);
        }
        return false;
    }

    @Override
    protected void onDeactivate() {
        try {
            serverSocket.close();
        } catch (Exception e) {
            ServerLog.getLogger().error("Cannot close datagram server", e);
        }
    }

    @OnSentence
    public void onSentence(Sentence s, String src) {
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
            ServerLog.getLogger().error("Error sending datagram packet", e);
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
        long t = getCache().getNow();
        synchronized (stats) {
            stats.onTimer(t);
            fastStats.onTimer(t);
        }
        super.onTimer();
    }
}
