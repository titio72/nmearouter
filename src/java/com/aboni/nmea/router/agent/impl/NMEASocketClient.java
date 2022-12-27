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

import com.aboni.nmea.router.OnRouterMessage;
import com.aboni.nmea.router.RouterMessage;
import com.aboni.nmea.router.TimestampProvider;
import com.aboni.nmea.router.conf.NetConf;
import com.aboni.nmea.router.conf.QOS;
import com.aboni.nmea.router.nmea0183.Message2NMEA0183;
import com.aboni.nmea.router.utils.Log;
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

    private final Log log;
    private final Message2NMEA0183 converter;

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
            NMEASocketClient.this.postMessage(event.getSentence());
        }
    }

    @Inject
    public NMEASocketClient(@NotNull Log log, @NotNull TimestampProvider tp, @NotNull Message2NMEA0183 converter) {
        super(log, tp, true, false);
        this.log = log;
        this.converter = converter;
    }

    public void setup(String name, QOS qos, NetConf conf) {
        if (this.server == null) {
            setup(name, qos);
            this.server = conf.getServer();
            this.port = conf.getPort();
            this.receive = conf.isRx();
            this.transmit = conf.isTx();
            log.info(() -> getLogBuilder().wO("init").wV("server", server).wV("port", port).wV("rx", receive).wV("tx", transmit).toString());
        } else {
            log.warning(() -> getLogBuilder().wO("init").wV("error", "already setup").toString());
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
                    socket = new Socket(server, port);
                    InputStream iStream = socket.getInputStream();

                    if (receive) {
                        reader = new SentenceReader(iStream);
                        reader.addSentenceListener(new InternalSentenceReader());
                        reader.start();
                    }

                    return true;
                } catch (Exception e) {
                    log.errorForceStacktrace(() -> getLogBuilder().wO("activate").toString(), e);
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
                    log.errorForceStacktrace(() -> getLogBuilder().wO("deactivate").toString(), e);
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

    @OnRouterMessage
    public void onMessage(RouterMessage rm) {
        try {
            if (socket != null && transmit) {
                Sentence[] s = converter.convert(rm.getMessage());
                if (s!=null) {
                    for (Sentence sentence: s) {
                        socket.getOutputStream().write(sentence.toSentence().getBytes());
                        socket.getOutputStream().write("\r".getBytes());
                    }
                }
            }
        } catch (Exception e) {
            log.error(() -> getLogBuilder().wO("message").toString(), e);
        }
    }
}