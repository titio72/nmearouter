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

package com.aboni.nmea.router;

import com.aboni.nmea.router.n2k.N2KMessage;
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
    private final List<Method> listenersN2K;
    private final List<Method> listenersMsg;
    private final Object listenerObject;

    public ListenerWrapper(Object listener) {
        listeners = new ArrayList<>();
        listenersJSON = new ArrayList<>();
        listenersN2K = new ArrayList<>();
        listenersMsg = new ArrayList<>();
        fillMethodsAnnotatedWith(listener);
        listenerObject = listener;
    }

    private void fillMethodsAnnotatedWith(Object listener) {
        Class<?> aClass = listener.getClass();
        while (aClass != Object.class) { // need to iterated thought hierarchy in order to retrieve methods from above the current instance
            // iterate though the list of methods declared in the class represented by aClass variable, and add those annotated with the specified annotation
            final List<Method> allMethods = new ArrayList<>(Arrays.asList(aClass.getDeclaredMethods()));
            for (final Method method : allMethods) {
                if (method.isAnnotationPresent(OnSentence.class)) {
                    scanMethod(method, Sentence.class, listeners);
                } else if (method.isAnnotationPresent(OnN2KMessage.class)) {
                    scanMethod(method, N2KMessage.class, listenersN2K);
                } else if (method.isAnnotationPresent(OnJSONMessage.class)) {
                    scanMethod(method, JSONObject.class, listenersJSON);
                } else if (method.isAnnotationPresent(OnRouterMessage.class)) {
                    scanMethod(method, RouterMessage.class, listenersMsg);
                }
            }
            // move to the upper class in the hierarchy in search for more methods
            aClass = aClass.getSuperclass();
        }
    }

    private void scanMethod(Method method, Class<?> c, List<Method> listenerMethods) {
        Class<?>[] params = method.getParameterTypes();
        if ((params.length == 1 && params[0].equals(c))
                || (params.length == 2 && params[0].equals(c) && params[1].equals(String.class))) {
            listenerMethods.add(method);
        }
    }

    public void onSentence(RouterMessage m) {
        dispatch(m, null, listenersMsg);
    }

    public void onSentence(Sentence s, String src) {
        dispatch(s, src, listeners);
    }

    public void onSentence(N2KMessage s, String src) {
        dispatch(s, src, listenersN2K);
    }

    public void onSentence(JSONObject s, String src) {
        dispatch(s, src, listenersJSON);
    }

    private <T> void dispatch(T payload, String src, List<Method> listenerMethods) {
        if (payload!=null) {
            for (Method m : listenerMethods) {
                try {
                    if (m.getParameterCount() == 1)
                        m.invoke(listenerObject, payload);
                    else if (m.getParameterCount() == 2)
                        m.invoke(listenerObject, payload, src);
                } catch (Exception e) {
                    ServerLog.getLogger().error("Error pushing message", e);
                }
            }
        }
    }

    public boolean isRouterMessage() {
        return !listenersMsg.isEmpty();
    }

    public boolean isJSON() {
        return !listenersJSON.isEmpty();
    }

    public boolean isNMEA() {
        return !listeners.isEmpty();
    }

    public boolean isN2K() {
        return !listenersN2K.isEmpty();
    }
}
