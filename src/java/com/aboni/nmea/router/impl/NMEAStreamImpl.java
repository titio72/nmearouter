package com.aboni.nmea.router.impl;

import com.aboni.nmea.router.NMEAStream;
import com.aboni.nmea.router.OnSentence;
import com.aboni.nmea.router.agent.NMEAAgent;
import com.aboni.nmea.sentences.NMEA2JSONb;
import com.aboni.utils.ServerLog;
import net.sf.marineapi.nmea.sentence.Sentence;
import org.json.JSONObject;

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
	public void pushSentence(Sentence s, NMEAAgent src) {
		synchronized (this) {
			push(s, src);
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

	private void push(Sentence s, NMEAAgent src) {
		synchronized (annotatedListeners) {
			JSONObject msg = null;
			for (ListenerWrapper i: annotatedListeners.values()) {
				try {
					i.onSentence(s, src);
					if (i.isJSON()) {
						if (msg==null) {
							msg = jsonConv.convert(s);
						}
						if (msg!=null) {
							i.onSentence(msg);
						}
					}
				} catch (Exception e) {
					ServerLog.getLogger().Warning("Error dispatching event to listener {" + s + "} error {" + e.getMessage() + "}");
				}
			}
		}
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
		            	} else if (params[0].equals(Sentence.class) && params[1].equals(NMEAAgent.class)) {
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

		private void onSentence(Sentence s, NMEAAgent src) {
			for (Method m: listeners) {
				Object[] p = new Object[] {s, src};
				try {
					m.invoke(o, p);
				} catch (Exception e) {
					ServerLog.getLogger().Error("Error pushing message", e);
				}
			}
		}

		boolean isJSON() {
			return !listenersJSON.isEmpty();
		}
		
		private void onSentence(JSONObject s) {
			Object[] p = new Object[] {s};
			for (Method m: listenersJSON) {
				try {
					m.invoke(o, p);
				} catch (Exception e) {
					ServerLog.getLogger().Error("Error pushing message", e);
				}
			}	
		}
	}
}
