package com.aboni.nmea.router.agent.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.atomic.AtomicBoolean;

import com.aboni.nmea.router.NMEACache;
import com.aboni.nmea.router.agent.NMEAAgent;
import com.aboni.nmea.router.agent.QOS;
import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;

import net.sf.marineapi.nmea.parser.SentenceFactory;
import net.sf.marineapi.nmea.sentence.Sentence;

public class NMEASerial extends NMEAAgentImpl {
    
    private static final int FAST_STATS_PERIOD = 1000;
    private static final int STATS_PERIOD = 60000;

    private long bps;
    private long bpsOut;
    
    private class StatsSpeed {
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
    
    private class Stats extends StatsSpeed {
        long sentences = 0;
        long sentenceErrs = 0;
        
        void reset(long time) {
        	super.reset(time);
        	sentenceErrs = 0;
        	sentences = 0;
        }
    }
    
    private StatsSpeed fastStats = 	new StatsSpeed();
    private Stats      stats = 		new Stats();
    
    private AtomicBoolean run = new AtomicBoolean(false);

    private Thread thread;

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
        fastStats = new StatsSpeed();
        stats = new Stats();
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
                	port.addDataListener(new SerialPortDataListener() {
						
						@Override
						public void serialEvent(SerialPortEvent event) {
							if (event.getEventType()==SerialPort.LISTENING_EVENT_DATA_AVAILABLE) {
								
							}
							
						}
						
						@Override
						public int getListeningEvents() {
							return SerialPort.LISTENING_EVENT_DATA_AVAILABLE |
									/*SerialPort.LISTENING_EVENT_DATA_RECEIVED |*/
									SerialPort.LISTENING_EVENT_DATA_WRITTEN;
						}
					});
                	
                	
                    thread = new Thread(new Runnable() {
                        
                        @Override
                        public void run() {
                            BufferedReader reader = new BufferedReader(new InputStreamReader(port.getInputStream()));
                            while (run.get()) {
                                String s = "";
                                try {
                                    s = reader.readLine();
                                    if (s!=null) {
                                    	synchronized (stats) {
	                                        fastStats.bytes += (s.length() + 2);
                                    	    stats.bytes += (s.length() + 2);
	                                        stats.sentences++;
                                    	}
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
        notify(e);
    }

    @Override
    protected void doWithSentence(Sentence s, NMEAAgent src) {
        if (isStarted() && trasmit) {
            try {
                String _s = s.toSentence() + "\r\n";
                byte[] b = _s.getBytes();
                port.writeBytes(b, b.length);
                synchronized (stats) {
                    fastStats.bytesOut += (b.length + 2);
                    stats.bytesOut += (b.length + 2);
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
	            	getLogger().Info(String.format("BIn {%d} bpsIn {%d} bpsOut {%d} BOut {%d} Msg {%d} Err {%d}", 
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