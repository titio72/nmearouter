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

import com.aboni.misc.Utils;
import com.aboni.nmea.router.NMEACache;
import com.aboni.nmea.router.NMEATrafficStats;
import com.aboni.nmea.router.conf.QOS;
import net.sf.marineapi.nmea.sentence.Sentence;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

public class NMEAUDPReceiver extends NMEAAgentImpl {

    private static final int FAST_STATS_PERIOD = 1; //number of timer ticks
    private static final int STATS_PERIOD = 60;

    private static final int OPEN_SOCKET_RETRY_TIME = 3000;
    private static final int SOCKET_READ_TIMEOUT = 60000;

    private int port;
    private boolean stop;
    private boolean setup;
    private final NMEAInputManager input;
    private final NMEATrafficStats fastStats;
    private final NMEATrafficStats stats;
    private final byte[] buffer = new byte[2048];
    private String description;

    @Inject
    public NMEAUDPReceiver(@NotNull NMEACache cache) {
        super(cache);
        setSourceTarget(true, false);
        input = new NMEAInputManager(getLogger());
        fastStats = new NMEATrafficStats(this::onFastStatsExpired, FAST_STATS_PERIOD, true, false);
        stats = new NMEATrafficStats(this::onStatsExpired, STATS_PERIOD, true, false);
        description = "UDP Receiver";
    }

    private void onFastStatsExpired(NMEATrafficStats s, long time) {
        if (isStarted()) {
            synchronized (this) {
                description = String.format("UDP Receiver port %d", port) + "<br>" + s.toString(time);
            }
        } else {
            synchronized (this) {
                description = String.format("UDP Receiver port %d", port);
            }
        }
    }

    private void onStatsExpired(NMEATrafficStats s, long time) {
        synchronized (this) {
            getLogger().info(s.toString(time));
        }
    }

    public void setup(String name, QOS q, int port) {
        if (!setup) {
            setup = true;
            setup(name, q);
            this.port = port;
            getLogger().info(String.format("Setting up UDP receiver: Port {%d}", port));
        } else {
            getLogger().info("Cannot setup UDP receiver - already set up");
        }
    }

    @Override
    public String getDescription() {
        synchronized (this) {
            return description;
        }
    }

    private void loop() {
        while (!stop) {
            try (DatagramSocket socket = new DatagramSocket(port)) {
                socket.setSoTimeout(SOCKET_READ_TIMEOUT);
                getLogger().info("Opened Datagram socket {" + port + "}");
                while (!stop) {
                    loopRead(socket);
                }
            } catch (SocketException e) {
                getLogger().error("Error opening datagram socket", e);
            }
            if (!stop) Utils.pause(OPEN_SOCKET_RETRY_TIME);
        }
    }

    private void loopRead(DatagramSocket socket) {
        String sSentence = null;
        try {
            DatagramPacket p = new DatagramPacket(buffer, buffer.length);
            socket.receive(p);
            sSentence = new String(p.getData(), 0, p.getLength());
            updateReadStats(sSentence);
            NMEAInputManager.Output out = input.getMessage(sSentence);
            if (out != null && out.hasMessages()) {
                for (Sentence sentence : out.nmeaSentences) {
                    if (sentence != null) {
                        updateReadSentencesStats(false);
                        notify(sentence);
                    }
                }
                if (out.n2KMessage != null) {
                    notify(out.n2KMessage);
                }

            }
        } catch (SocketTimeoutException e) {
            // read timeout
            getLogger().debug("Datagram socket read timeout");
            Utils.pause(1000);
        } catch (Exception e) {
            getLogger().warning("Error receiving sentence {" + sSentence + "} {" + e.getMessage() + "}");
        }
        updateReadSentencesStats(true);
    }

    @Override
    protected boolean onActivate() {
        synchronized (this) {
            if (!isStarted()) {
                Thread t = new Thread(this::loop);
                t.start();
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onDeactivate() {
        synchronized (this) {
            stop = true;
        }
    }

    @Override
    public String toString() {
        return "UDP " + port + " R";
    }

    private void updateReadStats(String s) {
        synchronized (stats) {
            stats.updateReadStats(s);
            fastStats.updateReadStats(s);
        }
    }

    private void updateReadSentencesStats(boolean fail) {
        synchronized (stats) {
            stats.updateReadStats(fail);
            fastStats.updateReadStats(fail);
        }
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