package com.aboni.nmea.router.agent.impl;

import com.aboni.nmea.router.NMEACache;
import com.aboni.nmea.router.agent.QOS;
import net.sf.marineapi.nmea.parser.SentenceFactory;
import net.sf.marineapi.nmea.sentence.Sentence;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class NMEAUDPReceiver extends NMEAAgentImpl {

	private DatagramSocket socket;
	private final int port;
	private boolean stop;
	
	public NMEAUDPReceiver(NMEACache cache, String name, int port, QOS qos) {
        super(cache, name, qos);
        setSourceTarget(true, false);
        this.port = port;
	}
	
	@Override
	public String getDescription() {
		return "UDP Receiver " + port;
	}
	
	@Override
	protected boolean onActivate() {

	    synchronized (this) {
	        if (socket == null) {
    	        try {
                    socket = new DatagramSocket(port);
                    getLogger().info("Opened Datagram socket {" + port + "}");
                    
                    //$GPRMC,054922.00,A,4337.80466,N,01017.61149,E,0.767,,051018,,*1D
                    
                    Thread t = new Thread(() -> {
						byte[] buffer = new byte[256];
						while (!stop) {
							try {
								DatagramPacket p = new DatagramPacket(buffer, 256);
								socket.receive(p);
								String sSentence = new String(p.getData(), 0, p.getLength());
								Sentence s = SentenceFactory.getInstance().createParser(sSentence);
								onSentenceRead(s);
							} catch (Exception e) {
								getLogger().warning("Error receiveing sentence {" + e.getMessage() + "}");
							}
						}
						socket.close();
					});
                    t.start();
                    
                    return true;
                } catch (Exception e) {
                    getLogger().error("Error initializing socket {" + port + "} ", e);
                    socket = null;
                }
            }
	    }
        return false;
	}
	
	@Override
	protected void onDeactivate() {
	    synchronized (this) {
	    	stop = true;
	    }
	}

	private void onSentenceRead(Sentence e) {
		notify(e);
	}
	
    @Override
    public String toString() {
        return " {UDP " + port + " R}";
    }

}