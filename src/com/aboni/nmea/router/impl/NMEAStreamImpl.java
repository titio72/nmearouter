package com.aboni.nmea.router.impl;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONObject;

import com.aboni.nmea.router.NMEASentenceListener;
import com.aboni.nmea.router.NMEAStream;
import com.aboni.nmea.router.OnSentence;
import com.aboni.nmea.router.agent.NMEAAgent;
import com.aboni.nmea.sentences.NMEA2JSONb;
import com.aboni.utils.ServerLog;

import net.sf.marineapi.nmea.sentence.Sentence;

public class NMEAStreamImpl implements NMEAStream {

	private Set<NMEASentenceListener> listeners;
	private Map<Object, ListenerWrapper> annotatedListeners;
	private NMEA2JSONb jsonConv;
	
	public NMEAStreamImpl() {
		listeners = new HashSet<>();
		annotatedListeners = new HashMap<>();
		jsonConv = new NMEA2JSONb();
	}

	@Override
	public void addSentenceListener(NMEASentenceListener listener) {
		synchronized (listeners) {
			listeners.add(listener);
		}
	}

	@Override
	public void dropSentenceListener(NMEASentenceListener listener) {
		synchronized (listeners) {
			listeners.remove(listener);
		}
	}

	@Override
	public void pushSentence(Sentence s, NMEAAgent src) {
		pushRegular(s, src);
		pushJSON(s, src);
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

	private void pushRegular(Sentence s, NMEAAgent src) {
		synchronized (listeners) {
			for (NMEASentenceListener i: listeners) {
				try {
					i.onSentence(s, src);
				} catch (Exception e) {
					ServerLog.getLogger().Error("Error dispatchinc event to listener!", e);
				}
			}
		}
	}

	private void pushJSON(Sentence s, NMEAAgent src) {
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
					ServerLog.getLogger().Error("Error dispatchinc event to listener!", e);
				}
			}
		}
	}
	
	private class ListenerWrapper {
		
		List<Method> listeners;
		List<Method> listenersJSON;
		Object o;
		
		void fillMethodsAnnotatedWith() {
		    Class<?> klass = o.getClass();
		    while (klass != Object.class) { // need to iterated thought hierarchy in order to retrieve methods from above the current instance
		        // iterate though the list of methods declared in the class represented by klass variable, and add those annotated with the specified annotation
		        final List<Method> allMethods = new ArrayList<Method>(Arrays.asList(klass.getDeclaredMethods()));       
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
		
		ListenerWrapper(Object l) {
			o = l;
		    listeners = new ArrayList<Method>();
		    listenersJSON = new ArrayList<Method>();
			fillMethodsAnnotatedWith();
		}

		void onSentence(Sentence s, NMEAAgent src) {
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
		
		void onSentence(JSONObject s) {
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
