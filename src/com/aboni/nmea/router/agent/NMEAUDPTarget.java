package com.aboni.nmea.router.agent;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;

import com.aboni.nmea.router.impl.NMEAAgentImpl;
import com.aboni.utils.ServerLog;

import net.sf.marineapi.nmea.sentence.Sentence;

public class NMEAUDPTarget extends NMEAAgentImpl {

	private DatagramSocket serverSocket;
	private int portTarget;
	private Set<InetAddress> targets;

	public NMEAUDPTarget(String name, QOS qos, int portTarget) {
		super(name, qos);
		this.portTarget = portTarget;
        setSourceTarget(false, true);
        targets = new HashSet<InetAddress>();
	}

    @Override
    public String getDescription() {
    	String res = "Port " + getPort() + " ";
    	for (InetAddress a: targets) res += " " + a.getHostName();
    	return res;
    }
    
	public int getPort() {
		return portTarget;
	}

	public void addTarget(String target) {
		try {
			targets.add(InetAddress.getByName(target));
		} catch (UnknownHostException e) {
			getLogger().Error("Invalid target {" + target + "}");
		}
	}
	
	@Override
	protected boolean onActivate() {
		try {
			serverSocket = new DatagramSocket();
			return true;
		} catch (IOException e) {
			ServerLog.getLogger().Error("Cannot open datagram server", e);
		}
		return false;
	}
	
	@Override
	protected void onDeactivate() {
		try {
			serverSocket.close();
		} catch (Exception e) {
			ServerLog.getLogger().Error("Cannot close datagram server", e);
		}
	}
	
	
	
	@Override
	protected void doWithSentence(Sentence s, NMEAAgent src) {
		String toSend = getOutSentence(s);
		if (toSend!=null) {
			try {
				for (InetAddress i: targets) {
					DatagramPacket packet = new DatagramPacket(toSend.getBytes(), toSend.length(), i, portTarget);
					serverSocket.send(packet);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	protected String getOutSentence(Sentence s) {
		String s1 = s.toSentence();
		return s1;
	}
}
