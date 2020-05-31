package com.aboni.nmea.router.services;

import com.aboni.nmea.router.NMEARouter;
import com.aboni.nmea.router.agent.AgentStatusManager;
import com.aboni.nmea.router.services.impl.AgentListSerializer;
import com.aboni.utils.ServerLog;
import com.aboni.utils.ThingsFactory;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.util.HashMap;
import java.util.Map;

@ServerEndpoint(value = "/agents")
public class AgentStatusSocket {
	private static final Map<Session, MySession> sessions = new HashMap<>();

	public static int getSessions() {
		synchronized (sessions) {
			return sessions.size();
		}
	}

	private final AgentStatusManager agentStatusManager;

	public AgentStatusSocket() {
		agentStatusManager = ThingsFactory.getInstance(AgentStatusManager.class);
	}

	@OnOpen
	public void onWebSocketConnect(Session session) {
		synchronized (sessions) {
			ServerLog.getLogger().info("Started agents web-socket session {" + session.getId() + "}");
			MySession s = new MySession(session);
			sessions.put(session, s);
		}
	}

	@OnClose
	public void onWebSocketClose(Session session) {
		synchronized (sessions) {
			ServerLog.getLogger().info("Closed agents web-socket session {" + session.getId() + "}");
			if (sessions.containsKey(session)) {
				MySession s = sessions.get(session);
				ServerLog.getLogger().info("Stopping agents updates for web-socket id {" + s.id + "}");
				s.stop();
				sessions.remove(session);
			}
		}
	}

	@OnError
	public void onWebSocketError(Throwable cause) {
		ServerLog.getLogger().error("Error handling agents web sockets", cause);
	}

	public void broadcast() {
		String agents = new AgentListSerializer(agentStatusManager).getJSON(ThingsFactory.getInstance(NMEARouter.class), "").toString();
		synchronized (sessions) {
			for (MySession s : sessions.values()) {
				s.send(agents);
			}
		}
	}

	private static class MySession {
		private final Session session;
		private final String id;

		MySession(Session s) {
			session = s;
			id = session.getId();
		}

		private void stop() {
			synchronized (this) {
				ServerLog.getLogger().info("Close agents WS session {" + id + "}");
			}
		}

		void send(String msg) {
			synchronized (this) {
				if (msg != null) {
					try {
						if (session.isOpen()) {
							RemoteEndpoint.Async remote = session.getAsyncRemote();
							remote.setSendTimeout(1000);
							remote.sendText(msg);
						}
					} catch (Exception e) {
						ServerLog.getLogger().error("Error sending json to agents WS {" + id + "}", e);
					}
				}
			}
		}
	}
}
