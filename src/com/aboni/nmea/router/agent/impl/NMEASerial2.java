package com.aboni.nmea.router.agent.impl;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import com.aboni.nmea.router.NMEACache;
import com.aboni.nmea.router.NMEAStream;
import com.aboni.nmea.router.agent.NMEAAgent;
import com.aboni.nmea.router.agent.QOS;
import com.fazecast.jSerialComm.SerialPort;

import net.sf.marineapi.nmea.parser.SentenceFactory;
import net.sf.marineapi.nmea.sentence.Sentence;

public class NMEASerial2 extends NMEAAgentImpl {

    private class SerialListener {

        private byte[] internalBuffer;
        private int write = 0;
        private static final int SIZE = 32 * 1024;

        public SerialListener() {
            internalBuffer = new byte[SIZE];
        }

        void read(byte[] buffer, int l) {
            try {
                synchronized (NMEASerial2.this) {
                    bytes += l;
                }
                for (int i = 0; i < l; i++) {
                    if (buffer[i] != 13 && buffer[i] != 10) {
                        internalBuffer[write] = buffer[i];
                        write = (write + 1);
                        if (write>=SIZE) {
                            getLogger().Error("Buffer exceeded");
                        }
                        write %= write;
                    }
                    if (buffer[i] == 13) {
                        String s = new String(internalBuffer, 0, write).trim();
                        processLine(s);
                        write = 0;
                    }
                }
            } catch (Exception ex) {
                getLogger().Error("Error reading serial", ex);
            }
        }
    }

    private long bps = 0;
    private long bytes = 0;
    private long bpsOut = 0;
    private long bytesOut = 0;
    private long resetTime = 0;

    private String portName;
    private int speed;
    private SerialPort port;
    private boolean receive;
    private boolean trasmit;

    private Executor sender;

    public NMEASerial2(NMEACache cache, NMEAStream stream, String name, String portName, int speed, boolean rec,
            boolean tran) {
        this(cache, stream, name, portName, speed, rec, tran, null);
    }

    public NMEASerial2(NMEACache cache, NMEAStream stream, String name, String portName, int speed, boolean rec,
            boolean tran, QOS qos) {
        super(cache, stream, name, qos);
        this.portName = portName;
        this.speed = speed;
        this.receive = rec;
        this.trasmit = tran;
        setSourceTarget(rec, tran);
        sender = Executors.newSingleThreadExecutor();
    }

    @Override
    public String getType() {
        return "Serial in/out";
    }

    @Override
    public String getDescription() {
        return String.format("Device %s %dbps (%s) In %dbps Out %dbps", portName, speed,
                (receive ? "R" : "") + (trasmit ? "X" : ""), bps * 8, bpsOut * 8);
    }

    @Override
    public String toString() {
        return super.toString() + " {Serial port " + portName + " " + speed + " " + (receive ? "R" : "")
                + (trasmit ? "X" : "") + "}";
    }

    @Override
    protected boolean onActivate() {
        if (port == null) {
            try {
                getLogger().Info("Creating Port {" + portName + "} Speed {" + speed + "} Mode {" + (receive ? "R" : "")
                        + (trasmit ? "X" : "") + "}");
                port = SerialPort.getCommPort(portName);
                port.setComPortParameters(speed, 8, SerialPort.ONE_STOP_BIT, SerialPort.NO_PARITY);
                port.openPort();
                port.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 0, 0);
                getLogger().Info("Port Opened");
                if (receive)
                    startReceive();
                return true;
            } catch (Exception e) {
                getLogger().Error("Error initializing serial {" + portName + "}", e);
                port = null;
            }
        }
        return false;
    }

    private void startReceive() {
        SerialListener l = new SerialListener();
        Thread t = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    byte[] readBuffer = new byte[1024];
                    while (true) {
                        int numRead = port.readBytes(readBuffer, readBuffer.length);
                        if (numRead > 0) {
                            l.read(readBuffer, numRead);
                        }
                    }
                } catch (Exception e) {
                    getLogger().Error("Error reading serial {" + getName() + "}", e);
                }
                stop();
            }
        });
        t.setDaemon(true);
        t.start();
    }

    @Override
    protected void onDeactivate() {
        if (port != null) {
            try {
                port.closePort();
            } catch (Exception e) {
                getLogger().Error("Error closing serial {" + portName + "}", e);
            } finally {
                port = null;
            }
        }
    }

    private void sentenceRead(Sentence e) {
        notify(e);
    }

    private void processLine(String s) {
        try {
            if (s.length() > 0 && s.startsWith("$")) {
                Sentence sentence = SentenceFactory.getInstance().createParser(s);
                sentenceRead(sentence);
            }
        } catch (Exception e) {
            if (s != null && s.length() > 256) {
                s = s.substring(0, 255) + "...";
            }
            getLogger().Debug("Cannot process sentence {" + s + "} msg {" + e.getMessage() + "}");
        }
    }

    @Override
    protected void doWithSentence(Sentence s, NMEAAgent src) {
        if (isStarted() && trasmit) {
            sender.execute(new Runnable() {

                @Override
                public void run() {
                    try {
                        String _s = s.toSentence() + "\r\n";
                        byte[] b = _s.getBytes();
                        port.writeBytes(b, b.length);
                        synchronized (NMEASerial2.this) {
                            bytesOut += b.length;
                        }
                    } catch (Exception e) {
                        getLogger().Error("ERROR: cannot write on port " + portName, e);
                        stop();
                    }
                }

            });
        }
    }

    @Override
    public void onTimer() {
        long t = System.currentTimeMillis();
        if (resetTime == 0) {
            resetTime = t;
        } else if ((t - resetTime) > 1000) {
            synchronized (this) {
                bps = (long) (bytes / ((t - resetTime) / 1000.0));
                bytes = 0;
                bpsOut = (long) (bytesOut / ((t - resetTime) / 1000.0));
                bytesOut = 0;
            }
            resetTime = t;
        }
    }
}