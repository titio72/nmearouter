package com.aboni.nmea.router.agent.impl;

import com.aboni.misc.Utils;
import com.aboni.nmea.router.NMEACache;
import com.aboni.nmea.router.OnSentence;
import com.aboni.nmea.router.agent.QOS;
import com.fazecast.jSerialComm.SerialPort;
import net.sf.marineapi.nmea.parser.SentenceFactory;
import net.sf.marineapi.nmea.sentence.Sentence;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.atomic.AtomicBoolean;

public class NMEASerial extends NMEAAgentImpl {

    private static final int FAST_STATS_PERIOD = 1000;
    private static final int STATS_PERIOD = 60000;

    private static final int PORT_TIMEOUT = 1000;
    private static final int PORT_OPEN_RETRY_TIMEOUT = 5000;

    private long bps;
    private long bpsOut;

    private static class StatsSpeed {
        long bytes = 0;
        long bytesOut = 0;
        long resetTime = 0;

        long getBps(long time) {
        	return (bytes * 1000) / (time - resetTime);
        }
        
        long getBpsOut(long time) {
        	return (bytesOut * 1000) / (time - resetTime);
        }
        
        void reset(long time) {
        	bytes = 0;
        	bytesOut = 0;
        	resetTime = time;
        }
    }

    private static class Stats extends StatsSpeed {
        long sentences = 0;
        long sentenceErrs = 0;

        @Override
        void reset(long time) {
            super.reset(time);
            sentenceErrs = 0;
            sentences = 0;
        }
    }

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

    private final StatsSpeed fastStats;
    private final Stats stats;
    private final Config config;

    private final AtomicBoolean run = new AtomicBoolean(false);


    private SerialPort port;
    private BufferedReader bufferedReader;

    private long lastPortRetryTime;

    @Inject
    public NMEASerial(@NotNull NMEACache cache) {
        super(cache);
        config = new Config();
        fastStats = new StatsSpeed();
        stats = new Stats();
    }

    public void setup(String name, String portName, int speed, boolean rec, boolean tran, QOS qos) {
        setup(name, qos);
        config.setPortName(portName);
        config.setSpeed(speed);
        config.setReceive(rec);
        config.setTransmit(tran);
        setSourceTarget(rec, tran);
    }

    @Override
    public String getType() {
        return "Serial in/out";
    }

    @Override
    public String getDescription() {
        synchronized (this) {
            if (port != null)
                return String.format("Device %s %d bps (%s) In %d bps Out %d bps", config.getPortName(), config.getSpeed(),
                        (config.isReceive() ? "R" : "") + (config.isTransmit() ? "X" : ""), bps * 8, bpsOut * 8);
            else
                return String.format("Device %s %d bps (%s) Disconnected", config.getPortName(), config.getSpeed(),
                        (config.isReceive() ? "R" : "") + (config.isTransmit() ? "X" : ""));
        }
    }

    @Override
    public String toString() {
        return "{Serial port " + config.getPortName() + " " + config.getSpeed() + " " + (config.isReceive() ? "R" : "")
                + (config.isTransmit() ? "X" : "") + "}";
    }

    @Override
    protected boolean onActivate() {
        try {
            getLogger().info("Port Opened");
            run.set(true);
            if (config.isReceive()) {
                startReader();
            }
            return true;
        } catch (Exception e) {
            getLogger().error("Error initializing serial agent {" + config.getPortName() + "}", e);
            port = null;
            bufferedReader = null;
        }
        return false;
    }

    private SerialPort getPort() {
        synchronized (this) {
            long now = getCache().getNow();
            if ((port == null && Utils.isOlderThan(lastPortRetryTime, now, PORT_OPEN_RETRY_TIMEOUT))) {
                resetPortAndReader();
                getLogger().info("Creating Port {" + config.getPortName() + "} " +
                        "Speed {" + config.getSpeed() + "} " +
                        "Mode {" + (config.isReceive() ? "R" : "") + (config.isTransmit() ? "X" : "") + "}");
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
                getLogger().info("Port {" + config.getPortName() + "} open, creating reader");
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
                    Utils.pause(5000);
                }
            }
        });
        thread.start();
    }

    private void readLIneFromBuffer(BufferedReader reader) {
        String s = "";
        try {
            s = reader.readLine();
            if (s != null) {
                updateReadStats(s);
                Sentence sentence = SentenceFactory.getInstance().createParser(s);
                onSentenceRead(sentence);
            }
        } catch (IOException e) {
            getLogger().warning("Port {" + config.getPortName() + "} read failure {" + e.getMessage() + "}, resetting");
            resetPortAndReader();
        } catch (Exception e) {
            getLogger().warning("Error reading from serial {" + e.getMessage() + "} {" + s + "}");
        }
    }

    private void updateReadStats(String s) {
        synchronized (stats) {
            int l = s.length() + 2;
            fastStats.bytes += l;
            stats.bytes += l;
            stats.sentences++;
        }
    }
	
	private void updateWriteStats(byte[] b) {
		synchronized (stats) {
		    fastStats.bytesOut += (b.length + 2);
		    stats.bytesOut += (b.length + 2);
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
            getLogger().error("Error closing serial {" + config.getPortName() + "}", e);
        } finally {
            resetPortAndReader();
        }
    }

    private void onSentenceRead(Sentence e) {
        notify(e);
    }

    @OnSentence
    public void onSentence(Sentence s, String src) {
        if (run.get() && config.isTransmit()) {
            String strSentence = s.toSentence() + "\r\n";
            byte[] b = strSentence.getBytes();
            SerialPort p = getPort();
            if (p != null) {
                if (p.writeBytes(b, b.length) != -1) {
                    updateWriteStats(b);
                } else {
                    getLogger().warning("Port {" + config.getPortName() + "} write failure, resetting");
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
        long t = getCache().getNow();
        synchronized (stats) {
            if (fastStats.resetTime == 0) {
                fastStats.reset(t);
                stats.reset(t);
            } else {
                if ((t - fastStats.resetTime) > FAST_STATS_PERIOD) {
	                bps = fastStats.getBps(t);
	                bpsOut = fastStats.getBpsOut(t);
	                fastStats.reset(t);
		        } 
		        if ((t - stats.resetTime) > STATS_PERIOD) {
	            	getLogger().info(String.format("BIn {%d} bpsIn {%d} bpsOut {%d} BOut {%d} Msg {%d} Err {%d}", 
	            			stats.bytes, (stats.bytes*8*1000)/(t - stats.resetTime), 
	            			stats.bytesOut, (stats.bytesOut*8*1000)/(t - stats.resetTime), 
	            			stats.sentences, stats.sentenceErrs));
	                stats.reset(t);
		        }
	        }
        }
        super.onTimer();
    }
}