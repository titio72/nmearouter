package com.aboni.nmea.router.agent.impl;

import com.aboni.nmea.router.NMEACache;
import com.aboni.nmea.router.agent.NMEAAgent;
import com.aboni.nmea.router.agent.QOS;
import net.sf.marineapi.nmea.event.SentenceEvent;
import net.sf.marineapi.nmea.event.SentenceListener;
import net.sf.marineapi.nmea.io.SentenceReader;
import net.sf.marineapi.nmea.sentence.Sentence;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

public class NMEASocketClient extends NMEAAgentImpl {

	private Socket socket;
	private final String server;
	private final int port;
	private SentenceReader reader;
    private final boolean receive;
    private final boolean transmit;
	
	public NMEASocketClient(NMEACache cache, String name, String server, int port, boolean rec, boolean trans, QOS qos) {
        super(cache, name, qos);
        setSourceTarget(true, false);
        this.server = server;
        this.port = port;
        this.receive = rec;
        this.transmit = trans;        
	}
	
	@Override
	public String getDescription() {
		return "TCP " + server + ":" + port;
	}
	
	@Override
	protected boolean onActivate() {

	    synchronized (this) {
	        if (socket == null) {
    	        try {
                    getLogger().info("Creating Socket {" + server + ":" + port + "}");
                    socket = new Socket(server, port);
                    InputStream iStream = socket.getInputStream();
                    getLogger().info("Opened Socket {" + server + ":" + port + "}");
    
                    if (receive) {
                        reader = new SentenceReader(iStream);
                        reader.addSentenceListener(new SentenceListener() {
        
                            @Override
                            public void readingPaused() { /* not needed */ }
        
                            @Override
                            public void readingStarted() { /* not needed */ }
        
                            @Override
                            public void readingStopped() { /* not needed */ }
        
                            @Override
                            public void sentenceRead(SentenceEvent event) { onSentenceRead(event.getSentence()); }
                            
                        });
                        reader.start();
                    }
                    
                    return true;
                } catch (Exception e) {
                    getLogger().error("Error initializing socket {" + server + ":" + port + "} ", e);
                    socket = null;
                }
            }
	    }
        return false;
	}
	
	@Override
	protected void onDeactivate() {
	    synchronized (this) {
    	    if (socket!=null) {
    	        reader.stop();
    	        try {
                    socket.close();
                } catch (IOException e) {
    	            getLogger().error("Error trying to close socket", e);
                } finally {
                    socket = null;
                }
    	    }
	    }
	}

	private void onSentenceRead(Sentence e) {
		notify(e);
	}
	
    @Override
    public String toString() {
        return " {TCP " + server + ":" + port+ " " + (receive ? "R" : "")
                + (transmit ? "X" : "") + "}";
    }

    @Override
    protected void doWithSentence(Sentence s, NMEAAgent source) {
    	try {
    	    if (socket!=null && transmit) {
    	        socket.getOutputStream().write(s.toSentence().getBytes());
    	        socket.getOutputStream().write("\r".getBytes());
    	    }
    	} catch (Exception e) {
            getLogger().info("Error sending data {" + e.getMessage() + "}");
    	}
    }
}