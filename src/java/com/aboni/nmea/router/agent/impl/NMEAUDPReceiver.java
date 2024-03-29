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
import com.aboni.nmea.message.Message;
import com.aboni.nmea.n2k.N2KMessage;
import com.aboni.nmea.router.NMEATrafficStats;
import com.aboni.nmea.router.RouterMessageFactory;
import com.aboni.nmea.router.conf.QOS;
import com.aboni.nmea.router.message.PositionAndVectorStream;
import com.aboni.nmea.router.message.SpeedAndHeadingStream;
import com.aboni.utils.TimestampProvider;
import com.aboni.utils.Utils;

import javax.inject.Inject;
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
    private final PositionAndVectorStream positionAndVectorStream;
    private final SpeedAndHeadingStream speedAndHeadingStream;
    private final byte[] buffer = new byte[2048];
    private String description;

    @Inject
    public NMEAUDPReceiver(Log log, TimestampProvider tp, RouterMessageFactory messageFactory) {
        super(log, tp, messageFactory, true, false);
        input = new NMEAInputManager(log);
        positionAndVectorStream = new PositionAndVectorStream(tp);
        positionAndVectorStream.setListener(this::postMessage);
        speedAndHeadingStream = new SpeedAndHeadingStream(tp);
        speedAndHeadingStream.setListener(this::postMessage);
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
            getLog().info(() -> getLogBuilder().wO("stats").w(s.toString(time)).toString());
        }
    }

    public void setup(String name, QOS q, int port) {
        if (!setup) {
            setup = true;
            setup(name, q);
            this.port = port;
            getLog().info(() -> getLogBuilder().wO("init").wV("port", port).toString());
        } else {
            getLog().error(() -> getLogBuilder().wO("init").wV("error", "already initialized").toString());
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
                while (!stop) {
                    loopRead(socket);
                }
            } catch (SocketException e) {
                getLog().error(() -> getLogBuilder().wO("open datagram socket").toString(), e);
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
            Message[] out = input.getMessage(sSentence);
            if (out != null) {
                updateReadSentencesStats(false);
                for (Message m : out) {
                    if (m instanceof N2KMessage) {
                        positionAndVectorStream.onMessage(m);
                        speedAndHeadingStream.onMessage(m);
                    }
                    postMessage(m);
                }
            }
        } catch (SocketTimeoutException e) {
            // read timeout
            getLog().error(() -> getLogBuilder().wO("read").toString(), e);
            Utils.pause(1000);
        } catch (Exception e) {
            String logSentence = sSentence;
            getLog().warning(() -> getLogBuilder().wO("read").wV("sentence", logSentence).toString(), e);
        }
        updateReadSentencesStats(true);
    }

    @Override
    protected boolean onActivate() {
        synchronized (this) {
            if (!isStarted()) {
                Thread t = new Thread(this::loop, "UDP receiver [" + getName() + "]");
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