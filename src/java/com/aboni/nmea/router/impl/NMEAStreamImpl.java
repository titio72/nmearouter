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
import com.aboni.utils.Log;
import com.aboni.utils.LogStringBuilder;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;

public class NMEAStreamImpl implements NMEAStream {

    private final Map<Object, ListenerWrapper> annotatedListeners;
    private final Log log;

    @Inject
    public NMEAStreamImpl(@NotNull Log log) {
        this.annotatedListeners = new HashMap<>();
        this.log = log;
    }

    @Override
    public void pushSentence(RouterMessage message) {
        if (message!=null) {
            synchronized (annotatedListeners) {
                for (ListenerWrapper i : annotatedListeners.values()) {
                    try {
                        i.dispatchAll(message);
                    } catch (Exception e) {
                        log.warning(LogStringBuilder.start("Stream").wO("push message").toString(), e);
                    }
                }
            }
        }
	}

	@Override
	public void subscribe(Object b) {
		synchronized (annotatedListeners) {
            annotatedListeners.put(b, new ListenerWrapper(b, log));
        }
	}

	@Override
	public void unsubscribe(Object b) {
		synchronized (annotatedListeners) {
			annotatedListeners.remove(b);
		}
	}

    @Override
    public int getSubscribersCount() {
        synchronized (annotatedListeners) {
            return annotatedListeners.size();
        }
    }
}
