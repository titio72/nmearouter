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

import com.aboni.nmea.router.NMEAStream;
import com.aboni.nmea.router.OnSentence;
import com.aboni.utils.ServerLog;

@ClientEndpoint
@ServerEndpoint(value="/events")
public class EventSocket
{
	private static Map<Session, MySession> sessions = new HashMap<>();

	private static NMEAStream stream;
	
	public static void setNMEAStream(NMEAStream stream) {
		EventSocket.stream = stream;
	}
	
	public EventSocket() {
		ServerLog.getLogger().Info("Init web-socket");
	}
	
    @OnOpen
    public void onWebSocketConnect(Session sess)
    {
    	synchronized (sessions) {
    		ServerLog.getLogger().Info("Started web-socket session {" + sess.getId() + "}");
	    	MySession s = new MySession(sess);
    		sessions.put(sess,  s);
	        s.start(stream);
    	}
    }
    
    @OnMessage
    public void onWebSocketText(String message)
    {
    }

    @OnClose
    public void onWebSocketClose(Session sess)
    {
    	synchronized (sessions) {
    		ServerLog.getLogger().Info("Closed web-socket session {" + sess.getId() + "}");
	    	if (sessions.containsKey(sess)) {
	    		MySession s = sessions.get(sess);
	    		ServerLog.getLogger().Info("Stopping updates for web-socket id {" + s.id + "}");
	    		s.stop(stream);
	    		sessions.remove(sess);
	    	}
    	}
    }
    	
    @OnError
    public void onWebSocketError(Throwable cause)
    {
        ServerLog.getLogger().Error("Error handling websockets", cause);
    }
    	
    public static class MySession {
    	private Session sess;
    	private static long sc;
    	private long id;
    	
    	MySession(Session s) {
    		sess = s;
    		id = sc++;
    	}
    	
	    private void start(NMEAStream stream) {
	    	synchronized (this) {
		    	ServerLog.getLogger().Info("Start new WS session {" + sess.getId() + "} ID {" + id + "} ");
		    	stream.subscribe(this);
	    	}
	    }
	    
	    private void stop(NMEAStream stream) {
	    	synchronized (this) {
		    	ServerLog.getLogger().Info("Close WS session {" + sess.getId() + "} ID {" + id + "} ");
		    	stream.unsubscribe(this);
	    	}
		}

		@OnSentence
		public void onSentence(JSONObject obj) {
			synchronized (this) {
				if (obj!=null) {
					try {
						sess.getBasicRemote().sendText(obj.toString());
					} catch (IOException e) {
						ServerLog.getLogger().Error("Error sending json to WS {" + id + "}", e);
					}
				}
			}
			
		}
    }
}
