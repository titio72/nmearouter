package com.aboni.nmea.router.agent.impl;

import com.aboni.misc.Utils;
import com.aboni.nmea.router.NMEACache;
import com.aboni.nmea.router.agent.QOS;
import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;
import net.sf.marineapi.nmea.parser.SentenceFactory;
import net.sf.marineapi.nmea.sentence.Sentence;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.atomic.AtomicBoolean;

public class NMEASerial extends NMEAAgentImpl {
    
    private static final int FAST_STATS_PERIOD = 1000;
    private static final int STATS_PERIOD = 60000;

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
    
    private final StatsSpeed fastStats;
    private final Stats      stats;
    
    private final AtomicBoolean run = new AtomicBoolean(false);

    private final String portName;
    private final int speed;
    private SerialPort port;
    private final boolean receive;
    private final boolean trasmit;

    public NMEASerial(NMEACache cache, String name, String portName, int speed, boolean rec,
            boolean tran, QOS qos) {
        super(cache);
        setup(name, qos);
        this.portName = portName;
        this.speed = speed;
        this.receive = rec;
        this.trasmit = tran;
        fastStats = new StatsSpeed();
        stats = new Stats();
        setSourceTarget(rec, tran);
    }

    @Override
    protected final void onSetup(String name, QOS q) {
        // do nothing
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
                getLogger().info("Creating Port {" + portName + "} Speed {" + speed + "} Mode {" + (receive ? "R" : "")
                        + (trasmit ? "X" : "") + "}");
                port = SerialPort.getCommPort(portName);
                port.setComPortParameters(speed, 8, SerialPort.ONE_STOP_BIT, SerialPort.NO_PARITY);
                port.openPort();
                port.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 500, 0);
                getLogger().info("Port Opened");
                run.set(true);
                if (receive) {
                	port.addDataListener(new SerialPortDataListener() {
						
						@Override
						public void serialEvent(SerialPortEvent event) {
						    // do nothing
						}
						
						@Override
						public int getListeningEvents() {
							return SerialPort.LISTENING_EVENT_DATA_AVAILABLE |
									/*SerialPort.LISTENING_EVENT_DATA_RECEIVED |*/
									SerialPort.LISTENING_EVENT_DATA_WRITTEN;
						}
					});
                    startReader();
                }
                return true;
            } catch (Exception e) {
                getLogger().error("Error initializing serial {" + portName + "}", e);
                port = null;
            }
        }
        return false;
    }

    private void startReader() {
        Thread thread = new Thread(() -> {
            BufferedReader reader = new BufferedReader(new InputStreamReader(port.getInputStream()));
            while (run.get()) {
                String s = "";
                try {
                    s = reader.readLine();
                    if (s != null) {
                        updateReadStats(s);
                        Sentence sentence = SentenceFactory.getInstance().createParser(s);
                        onSentenceRead(sentence);
                    }
                } catch (IOException e) {
                    Utils.pause(100);
                } catch (Exception e) {
                    getLogger().warning("Error reading from serial {" + e.getMessage() + "} {" + s + "}");
                }
            }
            try {
                reader.close();
            } catch (IOException e) {
                getLogger().warning("Error serial reader " + e.getMessage());
            }
        });
        thread.start();
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
        if (port != null) {
            run.set(false);
            try {
                port.closePort();
            } catch (Exception e) {
                getLogger().error("Error closing serial {" + portName + "}", e);
            } finally {
                port = null;
            }
        }
    }

    private void onSentenceRead(Sentence e) {
        notify(e);
    }

    @Override
    protected void doWithSentence(Sentence s, String src) {
        if (isStarted() && trasmit) {
            try {
                String strSentence = s.toSentence() + "\r\n";
                byte[] b = strSentence.getBytes();
                port.writeBytes(b, b.length);
                updateWriteStats(b);
            } catch (Exception e) {
                getLogger().error("ERROR: cannot write on port " + portName, e);
                stop();
            }
        }
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