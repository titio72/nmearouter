package com.aboni.nmea.router.impl;

import com.aboni.nmea.router.NMEAStream;
import com.aboni.nmea.router.OnSentence;
import com.aboni.nmea.router.RouterMessage;
import com.aboni.nmea.sentences.NMEA2JSONb;
import com.aboni.utils.ServerLog;
import net.sf.marineapi.nmea.sentence.Sentence;
import org.json.JSONObject;

import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.util.*;

public class NMEAStreamImpl implements NMEAStream {

	private final Map<Object, ListenerWrapper> annotatedListeners;
	private final NMEA2JSONb jsonConv;
	
	public NMEAStreamImpl() {
		annotatedListeners = new HashMap<>();
		jsonConv = new NMEA2JSONb();
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
				}
			}
		}
	}

	@Nullable
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
			msg = jsonConv.convert(s);
		}
		return msg;
	}

	private class ListenerWrapper {
		
		private final List<Method> listeners;
		private final List<Method> listenersJSON;
		private final Object o;
		
		private void fillMethodsAnnotatedWith() {
		    Class<?> klass = o.getClass();
		    while (klass != Object.class) { // need to iterated thought hierarchy in order to retrieve methods from above the current instance
		        // iterate though the list of methods declared in the class represented by klass variable, and add those annotated with the specified annotation
		        final List<Method> allMethods = new ArrayList<>(Arrays.asList(klass.getDeclaredMethods()));
		        for (final Method method : allMethods) {
		            if (method.isAnnotationPresent(OnSentence.class)) {
		            	Class<?>[] params = method.getParameterTypes();
		            	if (params[0].equals(JSONObject.class)) {
		            		listenersJSON.add(method);
		            	} else if (params[0].equals(Sentence.class) && params[1].equals(String.class)) {
		            		listeners.add(method);
		            	}
		            }
		        }
		        // move to the upper class in the hierarchy in search for more methods
		        klass = klass.getSuperclass();
		    }
		}
		
		private ListenerWrapper(Object l) {
			o = l;
		    listeners = new ArrayList<>();
		    listenersJSON = new ArrayList<>();
			fillMethodsAnnotatedWith();
		}

		private void onSentence(Sentence s, String src) {
			for (Method m: listeners) {
				Object[] p = new Object[] {s, src};
				try {
					m.invoke(o, p);
				} catch (Exception e) {
					ServerLog.getLogger().error("Error pushing message", e);
				}
			}
		}

		boolean isJSON() {
			return !listenersJSON.isEmpty();
		}

		boolean isNMEA() {
			return !listeners.isEmpty();
		}

		private void onSentence(JSONObject s) {
			Object[] p = new Object[] {s};
			for (Method m: listenersJSON) {
				try {
					m.invoke(o, p);
				} catch (Exception e) {
					ServerLog.getLogger().error("Error pushing message", e);
				}
			}	
		}
	}
}
