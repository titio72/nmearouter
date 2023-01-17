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

import com.aboni.nmea.router.utils.Log;
import com.aboni.nmea.router.utils.SafeLog;
import com.aboni.utils.LogStringBuilder;
import org.json.JSONObject;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ListenerWrapper {

    private final List<Method> listenersJSON;
    private final List<Method> listenersMsg;
    private final Object listenerObject;
    private final Log log;

    public ListenerWrapper(Object listener, Log log) {
        this.log = SafeLog.getSafeLog(log);
        listenersJSON = new ArrayList<>();
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
                if (method.isAnnotationPresent(OnJSONMessage.class)) {
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

    public void dispatchAll(RouterMessage message) {
        // it is important to check if the listeners set is empty because the getXXX may lazy load stuff, in this way
        // we avoid executing code that is not needed (because no one would listen)
        if (!listenersMsg.isEmpty()) dispatch(message, message.getSource(), listenersMsg);
        if (message.getJSON()!=null && !listenersJSON.isEmpty()) dispatch(message.getJSON(), message.getSource(), listenersJSON);
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
                    log.errorForceStacktrace(LogStringBuilder.start("MessageDispatcher").wO("push message").toString(), e);
                }
            }
        }
    }

    public Object getListenerObject() {
        return listenerObject;
    }
}
