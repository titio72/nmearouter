/*
(C) 2020, Andrea Boni
This file is part of NMEARouter.
NMEARouter is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.
NMEARouter is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.
You should have received a copy of the GNU General Public License
along with NMEARouter.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.aboni.nmea.router.services;

import com.aboni.nmea.router.NMEAStream;
import com.aboni.nmea.router.OnJSONMessage;
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

        @OnJSONMessage
        public void onSentence(JSONObject obj, String src) {
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
