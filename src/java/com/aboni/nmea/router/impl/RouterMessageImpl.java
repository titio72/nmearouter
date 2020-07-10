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

import com.aboni.nmea.router.RouterMessage;
import com.aboni.nmea.router.n2k.N2KMessage;
import net.sf.marineapi.nmea.sentence.Sentence;
import org.json.JSONObject;

public class RouterMessageImpl<T> implements RouterMessage {

    private final long timestamp;
    private final T message;
    private final String source;

    public static RouterMessage createMessage(Sentence obj, String source, long timestamp) {
        return new RouterMessageImpl<>(obj, source, timestamp);
    }

    public static RouterMessage createMessage(JSONObject obj, String source, long timestamp) {
        return new RouterMessageImpl<>(obj, source, timestamp);
    }

    public static RouterMessage createMessage(N2KMessage obj, String source, long timestamp) {
        return new RouterMessageImpl<>(obj, source, timestamp);
    }

    public static RouterMessage clone(RouterMessage m) {
        return new RouterMessageImpl<>(m.getPayload(), m.getSource(), m.getTimestamp());
    }

    private RouterMessageImpl(T msg, String source, long timestamp) {
        this.timestamp = timestamp;
        this.message = msg;
        this.source = source;
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public Object getPayload() {
        return getMessage();
    }

    @Override
    public String getSource() {
        return source;
    }

    public T getMessage() {
        return message;
    }
}
