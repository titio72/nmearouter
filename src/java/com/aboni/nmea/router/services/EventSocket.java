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
import com.aboni.utils.Log;
import com.aboni.utils.LogStringBuilder;
import com.aboni.utils.ThingsFactory;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.json.JSONObject;

import javax.websocket.*;
import java.util.concurrent.atomic.AtomicInteger;

@WebSocket
public class EventSocket {

    private static final AtomicInteger sessions = new AtomicInteger(0);
    public static int getSessions() {
        return sessions.get();
    }

    private final NMEAStream stream;
    private static final String WEB_SOCKET_CATEGORY = "WebSocket";
    private final Log log;
    private Session session;
    private String id;

    public EventSocket(NMEAStream stream) {
        log = ThingsFactory.getInstance(Log.class);
        this.stream = stream;
    }

    @OnOpen
    public void onWebSocketConnect(Session s) {
        log.info(LogStringBuilder.start(WEB_SOCKET_CATEGORY).withOperation("connect").withValue("id", s.getId()).toString());
        session = s;
        sessions.incrementAndGet();
        id = s.getId();
        stream.subscribe(this);
    }

    @OnClose
    public void onWebSocketClose(Session s) {
        log.info(LogStringBuilder.start(WEB_SOCKET_CATEGORY).withOperation("close").withValue("id", s.getId()).toString());
        stream.unsubscribe(this);
        sessions.decrementAndGet();
    }

    @SuppressWarnings("unused")
    @OnError
    public void onWebSocketError(Throwable cause) {
        log.errorForceStacktrace(LogStringBuilder.start(WEB_SOCKET_CATEGORY).withOperation("error").toString(), cause);
        stream.unsubscribe(this);
        sessions.decrementAndGet();
    }


    @OnJSONMessage
    public void onSentence(JSONObject obj) {
        if (obj != null) {
            try {
                RemoteEndpoint.Async remote = session.getAsyncRemote();
                remote.setSendTimeout(1000);
                remote.sendText(obj.toString());
            } catch (Exception e) {
                log.errorForceStacktrace(LogStringBuilder.start(WEB_SOCKET_CATEGORY).withOperation("message").withValue("id", id).toString(), e);
            }
        }
    }
}
