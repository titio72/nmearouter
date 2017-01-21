package com.aboni.nmea.router.agent;

import com.aboni.nmea.router.impl.NMEAAgentImpl;

import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;
import net.sf.marineapi.nmea.parser.SentenceFactory;
import net.sf.marineapi.nmea.sentence.Sentence;

public class NMEASerialSourceJSSC extends NMEAAgentImpl {
	
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
                    for (int i = 0; i<buffer.length; i++) {
                        if (buffer[i]!=13 && buffer[i]!=10) {
                        	internalBuffer[write] = buffer[i];
                        	write = (write + 1) % SIZE;
                        }
                    	if (buffer[i]==13) {
                		    String s = new String(internalBuffer, 0, write);
                    	    processSentence(s);
                    	    write = 0;
                        }
                    }
                } catch (SerialPortException ex) {
                    getLogger().Error("Error reading serial", ex);
                }
            }			
		}
	}
	
	private String portName;
	private int speed;
	private SerialPort port;
	
	public NMEASerialSourceJSSC(String name, String portName, int speed) {
		this(name, portName, speed, null);
	}
	
	public NMEASerialSourceJSSC(String name, String portName, int speed, QOS qos) {
        super(name, qos);
        this.portName = portName;
        this.speed = speed;
        setSourceTarget(true, false);
	}
	
	@Override
	public String toString() {
		return super.toString() + " {Serial port " + portName + " " + speed + "}";
	}
	
	@Override
	protected boolean onActivate() {
		if (port==null) {
			try {
				getLogger().Info("Creating Port {" + portName + "} Speed {" + speed + "}");
				port = new SerialPort(portName);
				if (port.openPort()) {

					port.setParams(speed, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
					int mask = SerialPort.MASK_RXCHAR + SerialPort.MASK_CTS + SerialPort.MASK_DSR;//Prepare mask
					port.setEventsMask(mask);
					SerialListener l = new SerialListener();
			        port.addEventListener(l);//Add SerialPortEventListener
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
    			e.printStackTrace();
    		} finally {
    			port = null;
    		}
		}
	}

	private void sentenceRead(Sentence e) {
		notify(e);
	}

	private void processSentence(String s) {
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
    protected void doWithSentence(Sentence s, NMEAAgent source) {
        // do nothing - this is a pure source
    }
}