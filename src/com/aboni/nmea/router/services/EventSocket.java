package com.aboni.nmea.router.services;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.websocket.ClientEndpoint;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.json.JSONObject;

import com.aboni.nmea.router.NMEAStreamProvider;
import com.aboni.nmea.router.agent.NMEAAgent;
import com.aboni.nmea.router.agent.NMEASentenceListener;
import com.aboni.nmea.sentences.NMEA2JSONb;
import com.aboni.utils.ServerLog;

import net.sf.marineapi.nmea.sentence.Sentence;

@ClientEndpoint
@ServerEndpoint(value="/events/")
public class EventSocket
{
	private static Map<Session, MySession> sessions = new HashMap<>();

	public EventSocket() {
	}
	
    @OnOpen
    public void onWebSocketConnect(Session sess)
    {
    	synchronized (sessions) {
	    	MySession s = new MySession(sess);
	        s.start();
    	}
    }
    
    @OnMessage
    public void onWebSocketText(String message)
    {
    }
    
    /*@OnClose
    public void onWebSocketClose(CloseReason reason)
    {
        //stop();
    }*/

    @OnClose
    public void onWebSocketClose(Session sess)
    {
    	synchronized (sessions) {
	    	if (sessions.containsKey(sess)) {
	    		MySession s = sessions.get(sess);
	    		s.stop();
	    		sessions.remove(sess);
	    	}
    	}
    }
    	@OnError
    public void onWebSocketError(Throwable cause)
    {
        ServerLog.getLogger().Error("Error handling websockets", cause);
    }
    
    	
    private static class MySession implements NMEASentenceListener {
    	private Session sess;
		private NMEA2JSONb nmea2json;
    	private static long sc;
    	private long id;
    	
    	MySession(Session s) {
    		sess = s;
    		nmea2json = new NMEA2JSONb();
    		id = sc++;
    	}
    	
	    private void start() {
	    	synchronized (this) {
		    	System.out.println("WS Session Opened " + id);
		    	ServerLog.getLogger().Info("Start new WS session " + id);
		    	NMEAStreamProvider.getStreamInstance().addSentenceListener(this);
	    	}
	    }
	    
	    private void stop() {
	    	synchronized (this) {
		    	System.out.println("WS Session Closed " + id);
		    	ServerLog.getLogger().Info("Close WS session " + id);
		    	NMEAStreamProvider.getStreamInstance().dropSentenceListener(this);
	    	}
		}

		@Override
		public void onSentence(Sentence s, NMEAAgent src) {
			synchronized (this) {
				JSONObject obj = nmea2json.convert(s);
				if (obj!=null) {
					try {
						sess.getBasicRemote().sendText(obj.toString());
					} catch (IOException e) {
						ServerLog.getLogger().Error("Error sending json to WS", e);
						e.printStackTrace();
					}
				}
			}
			
		}
    }
}
