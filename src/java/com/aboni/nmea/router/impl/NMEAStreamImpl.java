package com.aboni.nmea.router.impl;

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
					msg = sendJsonObject(msg, s, i);
				} catch (Exception e) {
					ServerLog.getLogger().warning("Error dispatching event to listener {" + s + "} error {" + e.getMessage() + "}");
				} catch (Error t) {
					ServerLog.getLogger().errorForceStacktrace("Error dispatching event to listener", t);
				}
			}
		}
	}

	private JSONObject sendJsonObject(JSONObject msg, Sentence s, ListenerWrapper i) {
		if (i.isJSON()) {
			msg = getJsonObject(msg, s);
			if (msg!=null) {
				i.onSentence(msg);
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
