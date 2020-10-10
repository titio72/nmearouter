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
import com.aboni.nmea.router.NMEATrafficStats;
import com.aboni.nmea.router.OnSentence;
import com.aboni.nmea.router.TimestampProvider;
import com.aboni.nmea.router.conf.QOS;
import com.aboni.utils.Log;
import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortIOException;
import com.fazecast.jSerialComm.SerialPortTimeoutException;
import net.sf.marineapi.nmea.sentence.Sentence;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.atomic.AtomicBoolean;

public class NMEASerial extends NMEAAgentImpl {

    private static final int FAST_STATS_PERIOD = 1; // number of timer ticks
    private static final int STATS_PERIOD = 60; // number of timer ticks

    private static final int PORT_TIMEOUT = 1000;
    private static final int PORT_OPEN_RETRY_TIMEOUT = 5000;
    private static final int PORT_WAIT_FOR_DATA = 500;
    private final Log log;

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

    private final TimestampProvider timestampProvider;

    @Inject
    public NMEASerial(@NotNull Log log, @NotNull TimestampProvider tp) {
        super(log, tp, true, false);
        this.log = log;
        this.timestampProvider = tp;
        config = new Config();
        input = new NMEAInputManager(log);
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
            getLogBuilder().w("activate").wV("device", toString()).error(log, e);
            port = null;
            bufferedReader = null;
        }
        return false;
    }

    private SerialPort getPort() {
        synchronized (this) {
            long now = timestampProvider.getNow();
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
        });
        thread.start();
    }

    private void readLIneFromBuffer(BufferedReader reader) {
        String s = "";
        try {
            s = reader.readLine();
            handleStringMessage(s);
        } catch (SerialPortTimeoutException e) {
            getLogBuilder().wO("read").wV("status", "timeout").info(log);
            Utils.pause(PORT_WAIT_FOR_DATA);
        } catch (SerialPortIOException e) {
            getLogBuilder().wO("read").wV("status", "reset").error(log, e);
            resetPortAndReader();
        } catch (IllegalArgumentException e) {
            getLogBuilder().wO("read").wV("line", s).error(log, e);
        } catch (Exception e) {
            getLogBuilder().wO("read").error(log, e);
        }
    }

    private void handleStringMessage(String s) {
        if (s != null) {
            updateReadStats(s);
            NMEAInputManager.Output out = input.getMessage(s);
            if (out != null && out.hasMessages()) {
                for (Sentence sentence : out.nmeaSentences) {
                    if (sentence != null) {
                        updateReadStats();
                        notify(sentence);
                    }
                }
                if (out.n2KMessage != null) {
                    notify(out.n2KMessage);
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
            getLogBuilder().wO("deactivate").error(log, e);
        } finally {
            resetPortAndReader();
        }
    }

    @OnSentence
    public void onSentence(Sentence s) {
        if (run.get() && config.isTransmit()) {
            String strSentence = s.toSentence() + "\r\n";
            byte[] b = strSentence.getBytes();
            SerialPort p = getPort();
            if (p != null) {
                if (p.writeBytes(b, b.length) != -1) {
                    updateWriteStats(strSentence);
                    updateWriteStats();
                } else {
                    getLogBuilder().wO("received").wV("sentence", s).wV("error", "cannot write to serial port").error(log);
                    resetPortAndReader();
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
        long t = timestampProvider.getNow();
        synchronized (stats) {
            stats.onTimer(t);
            fastStats.onTimer(t);
        }
        super.onTimer();
    }
}