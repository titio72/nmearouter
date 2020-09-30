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

package com.aboni.nmea.router.impl;

import com.aboni.nmea.router.ListenerWrapper;
import com.aboni.nmea.router.NMEAStream;
import com.aboni.nmea.router.RouterMessage;
import com.aboni.nmea.sentences.NMEA2JSONb;
import com.aboni.utils.ServerLog;
import net.sf.marineapi.nmea.sentence.Sentence;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class NMEAStreamImpl implements NMEAStream {

	private final Map<Object, ListenerWrapper> annotatedListeners;
	private final NMEA2JSONb jsonConverter;

	public NMEAStreamImpl() {
		annotatedListeners = new HashMap<>();
		jsonConverter = new NMEA2JSONb();
	}

	@Override
	public void pushSentence(RouterMessage msg) {
		synchronized (this) {
			push(msg);
		}
	}

	@Override
	public void subscribe(Object b) {
		synchronized (annotatedListeners) {
			annotatedListeners.put(b, new ListenerWrapper(b));
		}
	}

	@Override
	public void unsubscribe(Object b) {
		synchronized (annotatedListeners) {
			annotatedListeners.remove(b);
		}
	}

	private void push(RouterMessage message) {
		synchronized (annotatedListeners) {
			Object payload = message.getPayload();
			JSONObject msg = (payload instanceof JSONObject)?(JSONObject)payload:null;
			Sentence s = (payload instanceof Sentence)?(Sentence)payload:null;
			for (ListenerWrapper i: annotatedListeners.values()) {
                try {
                    sendNMEASentence(message, s, i);
                    msg = sendJsonObject(msg, s, message.getSource(), i);
                } catch (Exception e) {
                    ServerLog.getLogger().warning("Error dispatching event to listener {" + s + "} error {" + e.getMessage() + "}");
                }
            }
        }
    }

    private JSONObject sendJsonObject(JSONObject msg, Sentence s, String src, ListenerWrapper i) {
        if (i.isJSON()) {
            msg = getJsonObject(msg, s);
            if (msg != null) {
                i.onSentence(msg, src);
            }
        }
        return msg;
    }

    private void sendNMEASentence(RouterMessage message, Sentence s, ListenerWrapper i) {
		if (i.isNMEA() && s != null) {
			i.onSentence(s, message.getSource());
		}
	}

	private JSONObject getJsonObject(JSONObject msg, Sentence s) {
		if (msg==null && s!=null) {
			msg = jsonConverter.convert(s);
		}
		return msg;
	}
}
