package com.aboni.nmea.router.agent;

import com.aboni.nmea.router.NMEACache;
import com.aboni.nmea.router.NMEAStream;
import com.aboni.nmea.router.agent.impl.NMEAAgentImpl;
import com.aboni.utils.ServerLog;

import jssc.SerialPort;
import jssc.SerialPortException;
import net.sf.marineapi.nmea.sentence.Sentence;

public class NMEASerialTargetJSSC extends NMEAAgentImpl {

	private SerialPort port;
	
	private String portName;
	private int speed;
	
	public NMEASerialTargetJSSC(NMEACache cache, NMEAStream stream, String name, String portName, int speed, QOS q) {
		super(cache, stream, name, q);
		this.portName = portName;
		this.speed = speed;
        setSourceTarget(false, true);
	}

	@Override
	public String getDescription() {
		return "Serial " + portName + " " + speed;
	}
	
	@Override
	public String toString() {
		return super.toString() + " {Serial port " + portName + " " + speed + "}";
	}
	
	@Override
	protected void doWithSentence(Sentence s, NMEAAgent src) {
		if (isStarted()) {
			try {
				String toSend = s.toSentence() + "\r\n";
				port.writeBytes(toSend.getBytes());
			} catch (SerialPortException e) {
				getLogger().Error("ERROR: cannot write on port " + portName, e);
				stop();
			}
		}
	}

	@Override
	protected boolean onActivate() {
		try {
			if (port==null) {
				port = getSerialPort(portName, speed);
				return (port!=null);
			}
			return true;
		} catch (Exception e) {
			ServerLog.getLogger().Error("ERROR: Cannot open port " + portName, e);
			return false;
		}
	}
	
	@Override
	protected void onDeactivate() {
		if (port!=null) {
			try {
				port.closePort();
			} catch (SerialPortException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
			} finally {
				port = null;
			}
		}
	}
	
	private SerialPort getSerialPort(String portname, int speed) {
		try {
			getLogger().Info("Creating Port {" + 
						portname + "} Speed {" + speed + "}");
			SerialPort p = new SerialPort(portname);
			if (p.openPort()) {
				p.setParams(speed, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
				getLogger().Info("Port Opened");
				return p;
			} else {
				getLogger().Error("Cannot open port");
			}
		} catch (Exception e) {
			getLogger().Error("Cannot open port", e);
		}
		return null;
	}
}
