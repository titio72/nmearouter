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
import org.eclipse.jetty.websocket.api.RemoteEndpoint;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.json.JSONObject;

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
    private boolean err;

    public EventSocket(NMEAStream stream) {
        log = ThingsFactory.getInstance(Log.class);
        this.stream = stream;
    }

    @OnWebSocketConnect
    public void onWebSocketConnect(Session s) {
        session = s;
        int i = sessions.incrementAndGet();
        id = "S" + i;
        log.info(LogStringBuilder.start(WEB_SOCKET_CATEGORY).wO("connect").wV("id", id).toString());
        stream.subscribe(this);
    }

    @OnWebSocketClose
    public void onWebSocketClose(int i, String str) {
        log.info(LogStringBuilder.start(WEB_SOCKET_CATEGORY).wO("close").wV("id", id)
                .wV("status", i).wV("reason", str).toString());
        stream.unsubscribe(this);
        sessions.decrementAndGet();
    }

    @OnWebSocketError
    public void onWebSocketError(Throwable t) {
        log.error(LogStringBuilder.start(WEB_SOCKET_CATEGORY).wO("error").toString(), t);
        err = true;
        //stream.unsubscribe(this);
        sessions.decrementAndGet();
    }

    public boolean isErr() {
        return err;
    }

    @OnJSONMessage
    public void onSentence(JSONObject obj) {
        if (obj != null) {
            try {
                RemoteEndpoint remote = session.getRemote();
                remote.sendString(obj.toString());
            } catch (Exception e) {
                log.errorForceStacktrace(LogStringBuilder.start(WEB_SOCKET_CATEGORY).wO("message").wV("id", id).toString(), e);
            }
        }
    }
}
