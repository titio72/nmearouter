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

import com.aboni.log.Log;
import com.aboni.log.LogStringBuilder;
import com.aboni.log.SafeLog;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ListenerWrapper {

    public static class MessageDispatchException extends Exception {
        MessageDispatchException(Exception sourceE) {
            super(sourceE);
        }
    }

    public static class MessageListenerException extends RuntimeException {
        MessageListenerException(String msg) {
            super(msg);
        }
    }

    private final Method listenerMethod;
    private final Object listenerObject;
    private final Log log;

    public ListenerWrapper(Object listener, Log log) {
        this.log = SafeLog.getSafeLog(log);
        listenerObject = listener;
        listenerMethod = scanAnnotatedMethod(listener);
    }

    private Method scanAnnotatedMethod(Object listener) {
        Class<?> aClass = listener.getClass();
        while (aClass != Object.class) { // need to iterated thought hierarchy in order to retrieve methods from above the current instance
            // iterate though the list of methods declared in the class represented by aClass variable, and add those annotated with the specified annotation
            final List<Method> allMethods = new ArrayList<>(Arrays.asList(aClass.getDeclaredMethods()));
            for (final Method method : allMethods) {
                if (method.isAnnotationPresent(OnRouterMessage.class)) {
                    Class<?>[] params = method.getParameterTypes();
                    if (params.length == 1 && params[0].equals(RouterMessage.class)) {
                        return method;
                    }
                }
            }
            // move to the upper class in the hierarchy in search for more methods
            aClass = aClass.getSuperclass();
        }
        throw new MessageListenerException("No annotated method found");
    }

    public void dispatch(RouterMessage message) throws MessageDispatchException {
        // it is important to check if the listeners set is empty because the getXXX may lazy load stuff, in this way
        // we avoid executing code that is not needed (because no one would listen)
        if (message!=null && listenerMethod!=null) {
            try {
                listenerMethod.invoke(listenerObject, message);
            } catch (Exception e) {
                log.errorForceStacktrace(LogStringBuilder.start("MessageDispatcher").wO("push message").toString(), e);
                throw new MessageDispatchException(e);
            }
        }
    }

    public Object getListenerObject() {
        return listenerObject;
    }
}
