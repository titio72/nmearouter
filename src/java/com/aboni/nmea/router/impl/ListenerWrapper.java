package com.aboni.nmea.router.impl;

import com.aboni.nmea.router.OnSentence;
import com.aboni.utils.ServerLog;
import net.sf.marineapi.nmea.sentence.Sentence;
import org.json.JSONObject;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ListenerWrapper {

    private final List<Method> listeners;
    private final List<Method> listenersJSON;
    private final Object o;

    public ListenerWrapper(Object l) {
        o = l;
        listeners = new ArrayList<>();
        listenersJSON = new ArrayList<>();
        fillMethodsAnnotatedWith();
    }

    private void fillMethodsAnnotatedWith() {
        Class<?> aClass = o.getClass();
        while (aClass != Object.class) { // need to iterated thought hierarchy in order to retrieve methods from above the current instance
            // iterate though the list of methods declared in the class represented by aClass variable, and add those annotated with the specified annotation
            final List<Method> allMethods = new ArrayList<>(Arrays.asList(aClass.getDeclaredMethods()));
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
            aClass = aClass.getSuperclass();
        }
    }

    public void onSentence(Sentence s, String src) {
        for (Method m : listeners) {
            Object[] p = new Object[]{s, src};
            try {
                m.invoke(o, p);
            } catch (Exception e) {
                ServerLog.getLogger().error("Error pushing message", e);
            }
        }
    }

    public boolean isJSON() {
        return !listenersJSON.isEmpty();
    }

    public boolean isNMEA() {
        return !listeners.isEmpty();
    }

    public void onSentence(JSONObject s) {
        Object[] p = new Object[]{s};
        for (Method m : listenersJSON) {
            try {
                m.invoke(o, p);
            } catch (Exception e) {
                ServerLog.getLogger().error("Error pushing message", e);
            }
        }
    }
}
