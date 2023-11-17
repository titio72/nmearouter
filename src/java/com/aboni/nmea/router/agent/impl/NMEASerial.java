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

import com.aboni.nmea.router.conf.QOS;
import com.aboni.nmea.router.NMEATrafficStats;
import com.aboni.nmea.router.OnRouterMessage;
import com.aboni.nmea.router.RouterMessage;
import com.aboni.utils.TimestampProvider;
import com.aboni.nmea.message.Message;
import com.aboni.nmea.nmea0183.Message2NMEA0183;
import com.aboni.nmea.nmea0183.NMEA0183MessageFactory;
import com.aboni.log.Log;
import com.aboni.utils.Utils;
import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortIOException;
import com.fazecast.jSerialComm.SerialPortTimeoutException;
import net.sf.marineapi.nmea.sentence.Sentence;

import javax.inject.Inject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.atomic.AtomicBoolean;

public class NMEASerial extends NMEAAgentImpl {

    private static final int FAST_STATS_PERIOD = 1; // number of timer ticks
    private static final int STATS_PERIOD = 60; // number of timer ticks

    private static final int PORT_TIMEOUT = 1000;
    private static final int PORT_OPEN_RETRY_TIMEOUT = 5000;
    private static final int PORT_WAIT_FOR_DATA = 500;
    private final Message2NMEA0183 converter;

    private static class Config {
        private String portName;
        private int speed;
        private boolean receive;
        private boolean transmit;

        String getPortName() {
            return portName;
        }

        void setPortName(String portName) {
            this.portName = portName;
        }

        int getSpeed() {
            return speed;
        }

        void setSpeed(int speed) {
            this.speed = speed;
        }

        boolean isReceive() {
            return receive;
        }

        void setReceive(boolean receive) {
            this.receive = receive;
        }

        boolean isTransmit() {
            return transmit;
        }

        void setTransmit(boolean transmit) {
            this.transmit = transmit;
        }
    }

    private final NMEATrafficStats fastStats;
    private final NMEATrafficStats stats;
    private String description;
    private final Config config;

    private final AtomicBoolean run = new AtomicBoolean(false);

    private SerialPort port;
    private BufferedReader bufferedReader;

    private long lastPortRetryTime;

    private final NMEAInputManager input;

    @Inject
    public NMEASerial(Log log, TimestampProvider tp, Message2NMEA0183 converter, NMEA0183MessageFactory messageFactory) {
        super(log, tp, true, false);
        if (converter==null) throw new IllegalArgumentException("NMEA converter cannot be null");
        this.converter = converter;
        config = new Config();
        input = new NMEAInputManager(log, messageFactory);
        fastStats = new NMEATrafficStats(this::onFastStatsExpired);
        stats = new NMEATrafficStats(this::onStatsExpired);
    }

    public void setup(String name, String portName, int speed, boolean rec, boolean tran, QOS qos) {
        setup(name, qos);
        config.setPortName(portName);
        config.setSpeed(speed);
        config.setReceive(rec);
        config.setTransmit(tran);
        setSourceTarget(rec, tran);
        fastStats.setup(FAST_STATS_PERIOD, rec, tran);
        stats.setup(STATS_PERIOD, rec, tran);

    }

    private void onFastStatsExpired(NMEATrafficStats s, long time) {
        synchronized (this) {
            if (port != null)
                description = String.format("Device %s %d baud (%s)", config.getPortName(), config.getSpeed(),
                        (config.isReceive() ? "R" : "") + (config.isTransmit() ? "X" : "")) + "<br>" + s.toString(time);
            else
                description = String.format("Device %s %d bps (%s) Disconnected", config.getPortName(), config.getSpeed(),
                        (config.isReceive() ? "R" : "") + (config.isTransmit() ? "X" : ""));
        }
    }

    private void onStatsExpired(NMEATrafficStats s, long time) {
        synchronized (this) {
            getLogBuilder().wO("stats").w(" " + s.toString(time));
        }
    }

    @Override
    public String getType() {
        return "Serial in/out";
    }

    @Override
    public String getDescription() {
        synchronized (this) {
            return description;
        }
    }

    @Override
    public String toString() {
        return "Serial port " + config.getPortName() + " " + config.getSpeed() + " " + (config.isReceive() ? "R" : "")
                + (config.isTransmit() ? "X" : "");
    }

    @Override
    protected boolean onActivate() {
        try {
            run.set(true);
            if (config.isReceive()) {
                startReader();
            }
            return true;
        } catch (Exception e) {
            getLog().error(() -> getLogBuilder().w("activate").wV("device", toString()).toString(), e);
            port = null;
            bufferedReader = null;
        }
        return false;
    }

    private SerialPort getPort() {
        synchronized (this) {
            long now = getTimestampProvider().getNow();
            if ((port == null && Utils.isOlderThan(lastPortRetryTime, now, PORT_OPEN_RETRY_TIMEOUT))) {
                resetPortAndReader();
                getLogBuilder().wO("create port").wV("device", toString());
                SerialPort p = SerialPort.getCommPort(config.getPortName());
                p.setComPortParameters(config.getSpeed(), 8, SerialPort.ONE_STOP_BIT, SerialPort.NO_PARITY);
                p.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, PORT_TIMEOUT, PORT_TIMEOUT);
                if (!p.openPort()) {
                    lastPortRetryTime = now;
                } else {
                    lastPortRetryTime = 0;
                    port = p;
                }
            }
            return port;
        }
    }

    private BufferedReader getBufferedReader() {
        synchronized (this) {
            if (bufferedReader == null && getPort() != null) {
                bufferedReader = new BufferedReader(new InputStreamReader(port.getInputStream()));
            }
            return bufferedReader;
        }
    }

    private void startReader() {
        Thread thread = new Thread(() -> {
            while (run.get()) {
                BufferedReader reader;
                if ((reader = getBufferedReader()) != null) {
                    readLIneFromBuffer(reader);
                } else {
                    Utils.pause(PORT_OPEN_RETRY_TIMEOUT);
                }
            }
        }, "Serial [" + getName() + "]");
        thread.start();
    }

    private void readLIneFromBuffer(BufferedReader reader) {
        String s = "";
        try {
            s = reader.readLine();
            handleStringMessage(s);
        } catch (SerialPortTimeoutException e) {
            getLog().info(() -> getLogBuilder().wO("read").wV("status", "timeout").toString());
            Utils.pause(PORT_WAIT_FOR_DATA);
        } catch (SerialPortIOException e) {
            getLog().error(() -> getLogBuilder().wO("read").wV("status", "reset").toString(), e);
            resetPortAndReader();
        } catch (IllegalArgumentException e) {
            String sLine = s;
            getLog().error(() -> getLogBuilder().wO("read").wV("line", sLine).toString(), e);
        } catch (Exception e) {
            getLog().error(() -> getLogBuilder().wO("read").toString(), e);
        }
    }

    private void handleStringMessage(String s) {
        if (s != null) {
            updateReadStats(s);
            Message[] out = input.getMessage(s);
            if (out != null) {
                for (Message m: out) {
                    updateReadStats();
                    postMessage(m);
                }
            }
        }
    }

    private void updateReadStats() {
        synchronized (stats) {
            fastStats.updateReadStats(false);
            stats.updateReadStats(false);
        }
    }

    private void updateWriteStats() {
        synchronized (stats) {
            fastStats.updateWriteStats(false);
            stats.updateWriteStats(false);
        }
    }

    private void updateReadStats(String s) {
        synchronized (stats) {
            fastStats.updateReadStats(s);
            stats.updateReadStats(s);
        }
    }

    private void updateWriteStats(String s) {
        synchronized (stats) {
            fastStats.updateWriteStats(s);
            stats.updateWriteStats(s);
        }
    }

    @Override
    protected void onDeactivate() {
        run.set(false);
        try {
            if (port != null && port.isOpen()) {
                port.closePort();
            }
        } catch (Exception e) {
            getLog().error(() -> getLogBuilder().wO("deactivate").toString(), e);
        } finally {
            resetPortAndReader();
        }
    }

    private Sentence[] getSentenceToSend(RouterMessage rm) {
        return converter.convert(rm.getPayload());
    }

    @OnRouterMessage
    public void onSentence(RouterMessage rm) {
        if (run.get() && config.isTransmit()) {
            Sentence[] toSend = getSentenceToSend(rm);
            for (Sentence s: toSend) {
                String strSentence = s.toSentence() + "\r\n";
                byte[] b = strSentence.getBytes();
                SerialPort p = getPort();
                if (p != null) {
                    if (p.writeBytes(b, b.length) != -1) {
                        updateWriteStats(strSentence);
                        updateWriteStats();
                    } else {
                        getLog().error(() -> getLogBuilder().wO("received").wV("sentence", s).wV("error", "cannot write to serial port").toString());
                        resetPortAndReader();
                    }
                }
            }
        }
    }

    private void resetPortAndReader() {
        if (port != null) {
            port.closePort();
        }
        port = null;
        bufferedReader = null;
    }

    @Override
    public void onTimer() {
        super.onTimer();
        long t = getTimestampProvider().getNow();
        synchronized (stats) {
            stats.onTimer(t);
            fastStats.onTimer(t);
        }
        super.onTimer();
    }
}