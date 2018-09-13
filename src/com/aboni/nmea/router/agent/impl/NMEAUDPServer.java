package com.aboni.nmea.router.agent.impl;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;

import com.aboni.nmea.router.NMEACache;

import com.aboni.nmea.router.agent.NMEAAgent;
import com.aboni.nmea.router.agent.QOS;
import com.aboni.utils.ServerLog;

import net.sf.marineapi.nmea.sentence.Sentence;

public class NMEAUDPServer extends NMEAAgentImpl {

	private DatagramSocket serverSocket;
	private int portTarget;
	private Set<InetAddress> targets;

	public NMEAUDPServer(NMEACache cache, String name, QOS qos, int portTarget) {
		super(cache, name, qos);
		this.portTarget = portTarget;
        setSourceTarget(false, true);
        targets = new HashSet<InetAddress>();
	}

    @Override
    public String getType() {
    	return "UDP Server";
    }
	
    @Override
    public String getDescription() {
    	String res = "Port " + getPort() + "<br>";
    	for (InetAddress a: targets) res += a.getHostName() + " ";
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
	
	private String sending = "";
	private int nSentences = 0;
	
	@Override
	protected void doWithSentence(Sentence s, NMEAAgent src) {
		String toSend = getOutSentence(s);
		
		if (nSentences==3) {
			try {
				for (InetAddress i: targets) {
					DatagramPacket packet = new DatagramPacket(sending.getBytes(), sending.length(), i, portTarget);
					serverSocket.send(packet);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			nSentences = 0;
			sending = "";
		} else {
			if (toSend!=null) {
				if(!sending.isEmpty()) sending += "\r\n";
				sending += toSend;
				nSentences++;
			}
		}
		
	}
	
	protected String getOutSentence(Sentence s) {
		String s1 = s.toSentence();
		return s1;
	}
}
