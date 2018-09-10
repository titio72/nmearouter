package com.aboni.nmea.router.agent.impl;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import com.aboni.nmea.router.NMEACache;
//import com.aboni.nmea.router.NMEAStream;
import com.aboni.nmea.router.agent.NMEAAgent;
import com.aboni.nmea.router.agent.QOS;

import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;
import net.sf.marineapi.nmea.parser.SentenceFactory;
import net.sf.marineapi.nmea.sentence.Sentence;

public class NMEASerial extends NMEAAgentImpl {
	
	private class SerialListener implements SerialPortEventListener {

		private byte[] internalBuffer;
		private int write = 0;
		private static final int SIZE = 1024;

		public SerialListener() {
			internalBuffer = new byte[SIZE];
		}
		
		@Override
		public void serialEvent(SerialPortEvent event) {
			if(event.isRXCHAR()){//If data is available
                try {
                    byte buffer[] = port.readBytes();
                    synchronized (NMEASerial.this) {
                        bytes += buffer.length;
                    }
                    for (int i = 0; i<buffer.length; i++) {
                        if (buffer[i]!=13 && buffer[i]!=10) {
                        	internalBuffer[write] = buffer[i];
                        	write = (write + 1) % SIZE;
                        }
                    	if (buffer[i]==13) {
                		    String s = new String(internalBuffer, 0, write);
                    	    processLine(s);
                    	    write = 0;
                        }
                    }
                } catch (SerialPortException ex) {
                    getLogger().Error("Error reading serial", ex);
                }
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
	
	public NMEASerial(NMEACache cache, /*NMEAStream stream, */String name, String portName, int speed, boolean rec, boolean tran) {
		this(cache, /*stream, */name, portName, speed, rec, tran, null);
	}
	
	public NMEASerial(NMEACache cache, /*NMEAStream stream, */String name, String portName, int speed, boolean rec, boolean tran, QOS qos) {
        super(cache, /*stream, */name, qos);
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
		return String.format("Device %s %dbps (%s) In %dbps Out %dbps", portName, speed, (receive?"R":"") + (trasmit?"X":""), bps * 8, bpsOut * 8);
	}
	
	@Override
	public String toString() {
		return super.toString() + " {Serial port " + portName + " " + speed + " " + (receive?"R":"") + (trasmit?"X":"") + "}";
	}
	
	@Override
	protected boolean onActivate() {
		if (port==null) {
			try {
				getLogger().Info("Creating Port {" + portName + "} Speed {" + speed + "} Mode {" + (receive?"R":"") + (trasmit?"X":"") + "}");
				port = new SerialPort(portName);
				if (port.openPort()) {

					port.setParams(speed, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
					
					if (receive) {
						int mask = SerialPort.MASK_RXCHAR;//Prepare mask
						port.setEventsMask(mask);
						SerialListener l = new SerialListener();
				        port.addEventListener(l);//Add SerialPortEventListener
					}
					getLogger().Info("Port Opened");
					return true;
				} else {
					getLogger().Error("Cannot open port");
					port = null;
				}
			} catch (Exception e) {
                getLogger().Error("Error initializing serial {" + portName + "}", e);
                port = null;
			}
		}
		return false;
	}
	
	@Override
	protected void onDeactivate() {
		if (port!=null) {
    	    try {
    			port.closePort();
    		} catch (SerialPortException e) {
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
            if (s.length()>0) {
                Sentence sentence = SentenceFactory.getInstance().createParser(s);
                sentenceRead(sentence);
            }
	    } catch (Exception e) {
	        if (s!=null && s.length()>256) s = s.substring(0, 255);
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
						port.writeBytes(b);
						synchronized (NMEASerial.this) {
							bytesOut += b.length;
						}
					} catch (SerialPortException e) {
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
		if (resetTime==0) {
			resetTime = t;
		} else if ((t-resetTime)>1000) {
			synchronized (this) {
				bps = (long)(bytes / ((t-resetTime) / 1000.0));
				bytes = 0;
				bpsOut = (long)(bytesOut / ((t-resetTime) / 1000.0));
				bytesOut = 0;
			}
			resetTime = t;
		}
	}
}

/*

XDR - Transducer Measurements

Measurement data from transducers that measure physical quantities
such as temperature, force, pressure, frequency, angular or linear
displacement, etc. Data from a variable number transducers measuring
the same or different quantities can be mixed in the same sentence.
This sentence is designed for use by integrated systems as well as
transducers that may be connected in a 'chain' where each transducer
receives the sentence as an input and adds its own data fields on
before retransmitting the sentence.

$--XDR,a,x.x,a,c--c, ________ ...
       | | | | |
       | | | | +---------Data for variable # of transducers
       | | | +----------------\Transducer #1 ID
       | | +-------------------|Units of measure, Transducer #1 [2]
       | +----------------------|Measurement data, Transducer #1
       +-------------------------/Transducer type, Transducer #1 [2]


a,x.x,a,c--c*hh<CR><LF>
| | | |
| | | +--------------------\
| | +-----------------------|
| +--------------------------|
+-----------------------------/Transducer 'n' [1]


Notes:

[1] Sets of the four fields 'Type-Data-Units-ID' are allowed for an unde-
fined number of transducers. Up to 'n' transducers may be included
within the limits of allowed sentence length, null fields are not re-
quired except where portions of the 'Type-Data-Units-ID' combination are
not available.

[2] Allowed transducer types and their units of measure are:

Transducer				Type	Field Units 			Field Comments

temperature				C 		C = degrees Celsius
angular displacement 	A 		D = degrees 			"-" = anticlockwise
linear displacement 	D 		M = meters 				"-" = compression
frequency 				F 		H = Hertz
force 					N 		N = Newtons 			"-" = compression
pressure 				P 		B = Bars 				"-" = vacuum
flow rate 				R 		l = liters/second
tachometer 				T 		R = RPM
humidity 				H 		P = Percent
volume 					V 		M = cubic meters

Custom (ABONI)			V		V = Volts					
Custom (ABONI)			F		B = bps


*/