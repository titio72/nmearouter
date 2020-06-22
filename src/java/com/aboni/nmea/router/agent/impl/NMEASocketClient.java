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
import com.aboni.nmea.router.OnSentence;
import com.aboni.nmea.router.agent.QOS;
import com.aboni.nmea.router.conf.net.NetConf;
import net.sf.marineapi.nmea.event.SentenceEvent;
import net.sf.marineapi.nmea.event.SentenceListener;
import net.sf.marineapi.nmea.io.SentenceReader;
import net.sf.marineapi.nmea.sentence.Sentence;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

public class NMEASocketClient extends NMEAAgentImpl {

    private Socket socket;
    private String server;
    private int port;
    private SentenceReader reader;
    private boolean receive;
    private boolean transmit;

    private class InternalSentenceReader implements SentenceListener {

        @Override
        public void readingPaused() {
            // not needed
        }

        @Override
        public void readingStarted() {
            // not needed
        }

        @Override
        public void readingStopped() {
            // not needed
        }

        @Override
        public void sentenceRead(SentenceEvent event) {
            NMEASocketClient.this.notify(event.getSentence());
        }
    }

    @Inject
    public NMEASocketClient(@NotNull NMEACache cache) {
        super(cache);
        setSourceTarget(true, false);
    }

    public void setup(String name, QOS qos, NetConf conf) {
        if (this.server == null) {
            setup(name, qos);
            this.server = conf.getServer();
            this.port = conf.getPort();
            this.receive = conf.isRx();
            this.transmit = conf.isTx();
            getLogger().info(String.format("Setting up TCP client: Server {%s} Port {%d} RX {%b %b}", server, port, receive, transmit));
        } else {
            getLogger().info("Cannot setup TCP client - already set up");
        }
    }

    @Override
    public String getDescription() {
        return "TCP " + server + ":" + port;
    }

    @Override
    protected boolean onActivate() {

        synchronized (this) {
            if (socket == null) {
                try {
                    getLogger().info("Creating Socket {" + server + ":" + port + "}");
                    socket = new Socket(server, port);
                    InputStream iStream = socket.getInputStream();
                    getLogger().info("Opened Socket {" + server + ":" + port + "}");

                    if (receive) {
                        reader = new SentenceReader(iStream);
                        reader.addSentenceListener(new InternalSentenceReader());
                        reader.start();
                    }

                    return true;
                } catch (Exception e) {
                    getLogger().error("Error initializing socket {" + server + ":" + port + "} ", e);
                    socket = null;
                }
            }
        }
        return false;
    }

    @Override
    protected void onDeactivate() {
        synchronized (this) {
            if (socket!=null) {
                reader.stop();
                try {
                    socket.close();
                } catch (IOException e) {
                    getLogger().error("Error trying to close socket", e);
                } finally {
                    socket = null;
                }
            }
        }
    }

    @Override
    public String toString() {
        return " TCP " + server + ":" + port + " " + (receive ? "R" : "")
                + (transmit ? "X" : "");
    }

    @OnSentence
    public void onSentence(Sentence s, String source) {
        try {
            if (socket != null && transmit) {
                socket.getOutputStream().write(s.toSentence().getBytes());
                socket.getOutputStream().write("\r".getBytes());
            }
        } catch (Exception e) {
            getLogger().info("Error sending data {" + e.getMessage() + "}");
        }
    }
}