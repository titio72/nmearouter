package com.aboni.nmea.router.agent.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.atomic.AtomicBoolean;

import com.aboni.nmea.router.NMEACache;
import com.aboni.nmea.router.agent.NMEAAgent;
import com.aboni.nmea.router.agent.QOS;
import com.fazecast.jSerialComm.SerialPort;

import net.sf.marineapi.nmea.parser.SentenceFactory;
import net.sf.marineapi.nmea.sentence.Sentence;

public class NMEASerial extends NMEAAgentImpl {
    
    private static final int STATS_PERIOD = 1000;
	private long bps = 0;
    private long bytes = 0;
    private long bpsOut = 0;
    private long bytesOut = 0;
    private long resetTime = 0;

    private AtomicBoolean run = new AtomicBoolean(false);
    
    private String portName;
    private int speed;
    private SerialPort port;
    private boolean receive;
    private boolean trasmit;

    public NMEASerial(NMEACache cache, String name, String portName, int speed, boolean rec,
            boolean tran) {
        this(cache, name, portName, speed, rec, tran, null);
    }

    public NMEASerial(NMEACache cache, String name, String portName, int speed, boolean rec,
            boolean tran, QOS qos) {
        super(cache, name, qos);
        this.portName = portName;
        this.speed = speed;
        this.receive = rec;
        this.trasmit = tran;
        setSourceTarget(rec, tran);
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
        return "{Serial port " + portName + " " + speed + " " + (receive ? "R" : "")
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
                port.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 500, 0);
                getLogger().Info("Port Opened");
                run.set(true);
                if (receive) {
                    thread = new Thread(new Runnable() {
                        
                        @Override
                        public void run() {
                            BufferedReader reader = new BufferedReader(new InputStreamReader(port.getInputStream()));
                            while (run.get()) {
                                String s = "";
                                try {
                                    s = reader.readLine();
                                    if (s!=null) {
                                        bytes += s.length();
                                        Sentence sentence = SentenceFactory.getInstance().createParser(s);
                                        onSentenceRead(sentence);
                                    }
                                } catch (IOException e) {
                                    try { Thread.sleep(100); } catch (InterruptedException e1) {}
                                } catch (Exception e) {
                                    getLogger().Warning("Error reading from serial {" + e.getMessage() + "} {" + s + "}") ;
                                }
                            }
                            try {
                                reader.close();
                            } catch (IOException e) {
                                getLogger().Warning("Error serial reader " + e.getMessage());
                            }
                        }
                    });
                	thread.start();
                }
                return true;
            } catch (Exception e) {
                getLogger().Error("Error initializing serial {" + portName + "}", e);
                port = null;
            }
        }
        return false;
    }

    private Thread thread;
    
    @Override
    protected void onDeactivate() {
        if (port != null) {
            run.set(false);
            try {
                port.closePort();
            } catch (Exception e) {
                getLogger().Error("Error closing serial {" + portName + "}", e);
            } finally {
                port = null;
            }
        }
    }

    private void onSentenceRead(Sentence e) {
    	/*if (e instanceof RMCSentence) {
    		if (System.currentTimeMillis() % 11 == 0) {
    			RMCSentence s = (RMCSentence)e;
    			s.setPosition(new Position(-s.getPosition().getLatitude(), s.getPosition().getLongitude()));
    		}
    	}*/
        notify(e);
    }

    @Override
    protected void doWithSentence(Sentence s, NMEAAgent src) {
        if (isStarted() && trasmit) {
            try {
                String _s = s.toSentence() + "\r\n";
                byte[] b = _s.getBytes();
                port.writeBytes(b, b.length);
                synchronized (NMEASerial.this) {
                    bytesOut += b.length;
                }
            } catch (Exception e) {
                getLogger().Error("ERROR: cannot write on port " + portName, e);
                stop();
            }
        }
    }

    @Override
    public void onTimer() {
        long t = System.currentTimeMillis();
        if (resetTime == 0) {
            resetTime = t;
        } else if ((t - resetTime) > STATS_PERIOD) {
            synchronized (this) {
                bps = (long) (bytes / ((t - resetTime) / 1000.0));
                bytes = 0;
                bpsOut = (long) (bytesOut / ((t - resetTime) / 1000.0));
                bytesOut = 0;
            }
            resetTime = t;
        }
        super.onTimer();
    }
}