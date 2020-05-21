package com.aboni.nmea.router.services;

import com.aboni.nmea.router.NMEAStream;
import com.aboni.nmea.router.OnSentence;
import com.aboni.utils.ServerLog;
import org.json.JSONObject;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.util.HashMap;
import java.util.Map;

@ClientEndpoint
@ServerEndpoint(value = "/events")
public class EventSocket {
	private static final Map<Session, MySession> sessions = new HashMap<>();
	private static NMEAStream stream;

	public static void setNMEAStream(NMEAStream stream) {
		EventSocket.stream = stream;
	}

	public static int getSessions() {
		synchronized (sessions) {
			return sessions.size();
		}
	}

	@OnOpen
	public void onWebSocketConnect(Session session) {
		synchronized (sessions) {
			ServerLog.getLogger().info("Started web-socket session {" + session.getId() + "}");
			MySession s = new MySession(session);
			sessions.put(session, s);
			s.start(stream);
		}
	}

    @OnClose
    public void onWebSocketClose(Session session) {
        synchronized (sessions) {
            ServerLog.getLogger().info("Closed web-socket session {" + session.getId() + "}");
            if (sessions.containsKey(session)) {
                MySession s = sessions.get(session);
                ServerLog.getLogger().info("Stopping updates for web-socket id {" + s.id + "}");
                s.stop(stream);
                sessions.remove(session);
            }
        }
    }
    	
    @OnError
    public void onWebSocketError(Throwable cause)
    {
		ServerLog.getLogger().error("Error handling web sockets", cause);
    }
    	
    public static class MySession {
		private final Session session;
    	private static long sc;
    	private final long id;
    	private RemoteEndpoint.Async remote;

    	MySession(Session s) {
			session = s;
    		remote = null;
    		id = sc++;
    	}
    	
	    private void start(NMEAStream stream) {
	    	synchronized (this) {
				ServerLog.getLogger().info("Start new WS session {" + session.getId() + "} ID {" + id + "} ");
		    	stream.subscribe(this);
	    	}
	    }
	    
	    private void stop(NMEAStream stream) {
	    	synchronized (this) {
				ServerLog.getLogger().info("Close WS session {" + session.getId() + "} ID {" + id + "} ");
		    	stream.unsubscribe(this);
	    	}
		}

		@OnSentence
		public void onSentence(JSONObject obj) {
			synchronized (this) {
				if (obj!=null) {
					try {
						if (session.isOpen()) {
							if (remote == null) {
								remote = session.getAsyncRemote();
								remote.setSendTimeout(1000);
							}
							remote.sendText(obj.toString());
						}
					} catch (Exception e) {
						ServerLog.getLogger().error("Error sending json to WS {" + id + "}", e);
					}
				}
			}
		}
    }
}
