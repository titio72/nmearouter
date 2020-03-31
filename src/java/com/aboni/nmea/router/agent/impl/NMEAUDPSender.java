package com.aboni.nmea.router.agent.impl;

import com.aboni.nmea.router.NMEACache;
import com.aboni.nmea.router.OnSentence;
import com.aboni.nmea.router.agent.QOS;
import com.aboni.utils.ServerLog;
import net.sf.marineapi.nmea.sentence.Sentence;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;

public class NMEAUDPSender extends NMEAAgentImpl {

    private DatagramSocket serverSocket;
    private int portTarget = 1113;
    private final Set<InetAddress> targets;
    private boolean setup = false;

    @Inject
    public NMEAUDPSender(@NotNull NMEACache cache) {
        super(cache);
        setSourceTarget(false, true);
        targets = new HashSet<>();
    }

    public void setup(String name, QOS qos, int port) {
        if (!setup) {
            setup = true;
            setup(name, qos);
            portTarget = port;
            getLogger().info(String.format("Setting up UDP sender: Port {%d}", portTarget));
        } else {
            getLogger().info("Cannot setup UDP sender - already set up");
        }
    }

    @Override
    public String getType() {
        return "UDP Server";
    }

    @Override
    public String getDescription() {
        StringBuilder res = new StringBuilder("UDP Sender Port " + getPort() + "<br>");
        for (InetAddress a : targets) res.append(a.getHostName()).append(" ");
    	return res.toString();
    }
    
    @Override
    public String toString() {
        return "{UDP " + getPort() + " T}";
    }
    
	public int getPort() {
		return portTarget;
	}

	public void addTarget(String target) {
		try {
			targets.add(InetAddress.getByName(target));
		} catch (UnknownHostException e) {
			getLogger().error("Invalid target {" + target + "}");
		}
	}
	
	@Override
	protected boolean onActivate() {
		try {
			serverSocket = new DatagramSocket();
			return true;
		} catch (IOException e) {
			ServerLog.getLogger().error("Cannot open datagram server", e);
		}
		return false;
	}
	
	@Override
    protected void onDeactivate() {
        try {
            serverSocket.close();
        } catch (Exception e) {
            ServerLog.getLogger().error("Cannot close datagram server", e);
        }
    }

    private String sending = "";
    private int nSentences = 0;

    @OnSentence
    public void onSentence(Sentence s, String src) {
        String toSend = getOutSentence(s);

        if (nSentences == 3) {
            try {
                for (InetAddress i : targets) {
                    DatagramPacket packet = new DatagramPacket(sending.getBytes(), sending.length(), i, portTarget);
                    serverSocket.send(packet);
                }
            } catch (IOException e) {
                ServerLog.getLogger().error("Error sending datagram packet", e);
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
		return s.toSentence();
	}
}
