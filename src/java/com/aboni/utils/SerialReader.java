package com.aboni.utils;

import com.aboni.misc.Utils;
import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortTimeoutException;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

public class SerialReader {

    private static final int PORT_TIMEOUT = 1000;
    private static final int PORT_OPEN_RETRY_TIMEOUT = 5000;

    private static class Config {
        private String portName;
        private int speed;
        private int bufferSize = 4096;

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

    public SerialReader() {
        config = new Config();
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

    public boolean activate() {
        try {
            run.set(true);
            startReader();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
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
                try {
                    int r = p.getInputStream().read();
                    b[offset] = r & 0xFF;
                    if (callback != null) {
                        if (callback.onRead(b, offset)) {
                            offset = 0;
                        }
                    }
                    offset++;
                    offset = offset % b.length;
                } catch (SerialPortTimeoutException e) {
                    Utils.pause(250);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    private void resetPortAndReader() {
        if (port != null) {
            port.closePort();
        }
        port = null;
    }
}
