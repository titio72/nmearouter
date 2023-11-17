/*
 * Copyright (c) 2022,  Andrea Boni
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

package com.aboni.nmea.router.utils;

import com.aboni.log.Log;
import com.aboni.utils.TimestampProvider;
import com.aboni.utils.Utils;
import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortTimeoutException;

import javax.inject.Inject;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

public class SerialReader {

    private static final int PORT_TIMEOUT = 1000;
    private static final int PORT_OPEN_RETRY_TIMEOUT = 5000;
    private static final int DEFAULT_BUFFER_SIZE = 256;
    private String threadName = "Serial reader";

    public class Stats {
        private long bytesRead;
        private long overFlows;
        private long lastBytesReadReset;
        private int timeouts;

        public void reset() {
            synchronized (this) {
                bytesRead = 0;
                overFlows = 0;
                lastBytesReadReset = ts.getNow();
            }
        }

        public String toString(long t) {
            synchronized (this) {
                return String.format("Bytes {%d} Overflows {%d} Timeouts {%d} Period {%d} Last read {%d}",
                        bytesRead, overFlows, timeouts, t - lastBytesReadReset, t - lastSuccessfulLoop);
            }
        }

        private void incrementBytesRead() {
            synchronized (this) {
                if (bytesRead < Long.MAX_VALUE) bytesRead += 1;
            }
        }

        private void incrementOverflows() {
            synchronized (this) {
                if (overFlows < Long.MAX_VALUE) overFlows += 1;
            }
        }

        private void incrementTimeouts() {
            synchronized (this) {
                timeouts++;
            }
        }
    }

    private final Stats stats;

    private final TimestampProvider ts;

    private static class Config {
        private String portName;
        private int speed;
        private int bufferSize = DEFAULT_BUFFER_SIZE;

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

        public int getBufferSize() {
            return bufferSize;
        }

        public void setBufferSize(int bufferSize) {
            this.bufferSize = bufferSize;
        }
    }

    private final Config config;
    private final AtomicBoolean run = new AtomicBoolean(false);
    private SerialPort port;
    private long lastPortRetryTime;
    private ReaderCallback callback;
    private final Log logger;

    private long lastSuccessfulLoop;

    @Inject
    public SerialReader(TimestampProvider ts, Log logger) {
        this.ts = ts;
        this.logger = logger;
        config = new Config();
        stats = new Stats();
    }

    @Inject
    public SerialReader(Log logger) {
        this(ThingsFactory.getInstance(TimestampProvider.class), logger);
    }

    public void setup(String threadName, String portName, int speed, ReaderCallback callback) {
        setup(portName, speed, DEFAULT_BUFFER_SIZE, callback);
        this.threadName = threadName;
        config.setPortName(portName);
        config.setSpeed(speed);
        this.callback = callback;
    }

    public void setup(String portName, int speed, int bufferSize, ReaderCallback callback) {
        config.setPortName(portName);
        config.setSpeed(speed);
        config.setBufferSize(bufferSize);
        this.callback = callback;
    }

    public Stats getStats() {
        return stats;
    }

    public void activate() {
        try {
            run.set(true);
            stats.reset();
            startReader();
        } catch (Exception e) {
            resetPortAndReader();
        }
    }

    public void deactivate() {
        run.set(false);
    }

    private SerialPort getPort() {
        synchronized (this) {
            long now = ts.getNow();
            if ((port == null && (now - lastPortRetryTime) > PORT_OPEN_RETRY_TIMEOUT)) {
                resetPortAndReader();
                SerialPort p = SerialPort.getCommPort(config.getPortName());
                p.setComPortParameters(config.getSpeed(), 8, SerialPort.ONE_STOP_BIT, SerialPort.NO_PARITY);
                p.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, PORT_TIMEOUT, PORT_TIMEOUT);
                if (p.openPort()) {
                    lastPortRetryTime = 0;
                    port = p;
                    logger.info(String.format("Serial port opened {%s} at {%d} baud", config.getPortName(), config.getSpeed()));
                } else {
                    lastPortRetryTime = now;
                }
            }
            return port;
        }
    }

    private void startReader() {
        Thread thread = new Thread(() -> {
            int offset = 0;
            int[] b = new int[config.getBufferSize()];
            while (run.get()) {
                try {
                    SerialPort p = getPort();
                    if (p != null) {
                        lastSuccessfulLoop = ts.getNow();
                        offset = readByte(offset, b, p);
                    } else {
                        Utils.pause(500);
                    }
                } catch (Exception e) {
                    logger.errorForceStacktrace("Serial port reading loop stopped unexpectedly", e);
                }
            }
        }, threadName);
        thread.start();
    }

    private int readByte(int offset, int[] b, SerialPort p) {
        try {
            int r = p.getInputStream().read();
            stats.incrementBytesRead();
            b[offset] = r & 0xFF;
            if (callback != null && callback.onRead(b, offset)) {
                offset = 0;
            } else {
                offset++;
            }
            if (offset>=b.length) {
                stats.incrementOverflows();
                offset = 0;
            }
        } catch (SerialPortTimeoutException e) {
            Utils.pause(250);
            stats.incrementTimeouts();
        } catch (IOException e) {
            logger.error("Serial port read error: resetting", e);
            resetPortAndReader();
        }
        return offset;
    }

    private void resetPortAndReader() {
        synchronized (this) {
            if (port != null) {
                port.closePort();
            }
            port = null;
        }
    }
}
