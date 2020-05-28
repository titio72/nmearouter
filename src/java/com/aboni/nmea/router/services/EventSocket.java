package com.aboni.nmea.router.services;

import com.aboni.nmea.router.NMEAStream;
import com.aboni.nmea.router.OnSentence;
import com.aboni.utils.ServerLog;
import org.json.JSONObject;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.util.concurrent.atomic.AtomicInteger;

@ServerEndpoint(value = "/events")
public class EventSocket {

    private static NMEAStream stream;
    private static final AtomicInteger sessions = new AtomicInteger(0);

    private MySession session;

    public static void setNMEAStream(NMEAStream stream) {
        EventSocket.stream = stream;
    }

    public static int getSessions() {
        return sessions.get();
    }

    @OnOpen
	public void onWebSocketConnect(Session session) {
        ServerLog.getLogger().info("Started web-socket session {" + session.getId() + "}");
        this.session = new MySession(session);
        this.session.start(stream);
        sessions.incrementAndGet();
    }

    @OnClose
    public void onWebSocketClose(Session session) {
        ServerLog.getLogger().info("Closed web-socket session {" + session.getId() + "}");
        this.session.stop(stream);
        sessions.decrementAndGet();
    }
    	
    @OnError
    public void onWebSocketError(Throwable cause)
    {
		ServerLog.getLogger().error("Error handling web sockets", cause);
    }

    public static class MySession {
        private final Session session;
        private final String id;

        MySession(Session s) {
            session = s;
            id = session.getId();
        }

        private void start(NMEAStream stream) {
            synchronized (this) {
                ServerLog.getLogger().info("Start new WS session {" + id + "}");
                stream.subscribe(this);
            }
	    }
	    
	    private void stop(NMEAStream stream) {
	    	synchronized (this) {
                ServerLog.getLogger().info("Close WS session {" + id + "}");
                stream.unsubscribe(this);
            }
		}

		@OnSentence
		public void onSentence(JSONObject obj) {
			synchronized (this) {
				if (obj!=null) {
					try {
						if (session.isOpen()) {
                            RemoteEndpoint.Async remote = session.getAsyncRemote();
                            remote.setSendTimeout(1000);
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
