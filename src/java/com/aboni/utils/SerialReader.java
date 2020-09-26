package com.aboni.utils;

import com.aboni.misc.Utils;
import com.aboni.nmea.router.TimestampProvider;
import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortTimeoutException;

import javax.inject.Inject;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

public class SerialReader {

    private static final int PORT_TIMEOUT = 1000;
    private static final int PORT_OPEN_RETRY_TIMEOUT = 5000;

    public class Stats {
        private long bytesRead;
        private long overFlows;
        private long lastBytesReadReset;

        public long getBytesRead() {
            synchronized (this) {
                return bytesRead;
            }
        }

        public long getOverFlows() {
            synchronized (this) {
                return overFlows;
            }
        }

        public void reset() {
            synchronized (this) {
                bytesRead = 0;
                overFlows = 0;
                lastBytesReadReset = ts.getNow();
            }
        }

        private void incrBytesRead(int n) {
            synchronized (this) {
                if (bytesRead<Long.MAX_VALUE) bytesRead++;
            }
        }

        private void incrOverflows(int n) {
            synchronized (this) {
                if (overFlows<Long.MAX_VALUE) overFlows++;
            }
        }

        public long getLastResetTime() {
            synchronized (this) {
                return lastBytesReadReset;
            }
        }

        public String toString(long t) {
            synchronized (this) {
                return String.format("Bytes {%d} Overflows {%d} Period {%d}", bytesRead, overFlows, t - lastBytesReadReset);
            }
        }

    }

    private final Stats stats;

    private TimestampProvider ts;

    private static class Config {
        private String portName;
        private int speed;
        private int bufferSize = 256;

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

    @Inject
    public SerialReader(TimestampProvider ts) {
        this.ts = ts;
        config = new Config();
        stats = new Stats();
    }

    @Inject
    public SerialReader() {
        this.ts = ThingsFactory.getInstance(TimestampProvider.class);
        config = new Config();
        stats = new Stats();
    }


    public void setup(String portName, int speed, ReaderCallback callback) {
        config.setPortName(portName);
        config.setSpeed(speed);
        this.callback = callback;
    }

    public void setup(String portName, int speed, int bufferSize, ReaderCallback callback) {
        config.setPortName(portName);
        config.setSpeed(speed);
        config.bufferSize = bufferSize;
        this.callback = callback;
    }

    public Stats getStats() {
        return stats;
    }

    public boolean activate() {
        try {
            run.set(true);
            stats.reset();
            startReader();
            return true;
        } catch (Exception e) {
            resetPortAndReader();
        }
        return false;
    }

    public void deactivate() {
        run.set(false);
    }

    private SerialPort getPort() {
        synchronized (this) {
            long now = System.currentTimeMillis();
            if ((port == null && (now - lastPortRetryTime) > PORT_OPEN_RETRY_TIMEOUT)) {
                resetPortAndReader();
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

    private void startReader() {
        Thread thread = new Thread(() -> {
            int offset = 0;
            int[] b = new int[config.bufferSize];
            while (run.get()) {
                SerialPort p = getPort();
                if (p != null) {
                    offset = readByte(offset, b, p);
                } else {
                    Utils.pause(500);
                }
            }
        });
        thread.start();
    }

    private int readByte(int offset, int[] b, SerialPort p) {
        try {
            int r = p.getInputStream().read();
            stats.incrBytesRead(1);
            b[offset] = r & 0xFF;
            if (callback != null && callback.onRead(b, offset)) {
                offset = 0;
            }
            offset++;
            if (offset>=b.length) {
                stats.incrOverflows(1);
                offset = 0;
            }
        } catch (SerialPortTimeoutException e) {
            Utils.pause(250);
        } catch (IOException e) {
            resetPortAndReader();
        }
        return offset;
    }

    private void resetPortAndReader() {
        if (port != null) {
            port.closePort();
        }
        port = null;
    }
}
