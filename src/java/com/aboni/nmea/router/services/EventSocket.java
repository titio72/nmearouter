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

    public static void setNMEAStream(NMEAStream stream) {
        EventSocket.stream = stream;
    }

    public static int getSessions() {
        return sessions.get();
    }

    private MySession session;

    @OnOpen
    public void onWebSocketConnect(Session s) {
        if (stream != null) {
            ServerLog.getLogger().info("Started web-socket session {" + s.getId() + "}");
            session = new MySession(s);
            doStart();
        }
    }

    @OnClose
    public void onWebSocketClose(Session s) {
        if (stream != null) {
            ServerLog.getLogger().info("Closed web-socket session {" + s.getId() + "}");
            if (session != null && session.isActive()) {
                doClose();
            }
        }
    }

    private void doStart() {
        sessions.incrementAndGet();
        session.start(stream);
    }

    private void doClose() {
        session.stop(stream);
        sessions.decrementAndGet();
    }

    @OnError
    public void onWebSocketError(Throwable cause) {
        ServerLog.getLogger().error("Error handling web sockets", cause);
        doClose();
    }

    public static class MySession {
        private final Session session;
        private final String id;
        private boolean active;

        MySession(Session s) {
            session = s;
            id = session.getId();
            active = false;
        }

        private boolean isActive() {
            synchronized (this) {
                return active;
            }
        }

        private void start(NMEAStream stream) {
            synchronized (this) {
                ServerLog.getLogger().info("Start new WS session {" + id + "}");
                active = true;
                stream.subscribe(this);
            }
        }

        private void stop(NMEAStream stream) {
	    	synchronized (this) {
                ServerLog.getLogger().info("Close WS session {" + id + "}");
                stream.unsubscribe(this);
                active = false;
            }
		}

		@OnSentence
		public void onSentence(JSONObject obj) {
			synchronized (this) {
                if (active && obj != null) {
                    try {
                        RemoteEndpoint.Async remote = session.getAsyncRemote();
                        remote.setSendTimeout(1000);
                        remote.sendText(obj.toString());
                    } catch (Exception e) {
                        ServerLog.getLogger().error("Error sending json to WS {" + id + "}", e);
                    }
                }
            }
		}
    }
}
