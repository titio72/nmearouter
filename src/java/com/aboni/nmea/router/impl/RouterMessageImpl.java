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
import com.aboni.nmea.router.message.Message;
import org.json.JSONObject;

public class RouterMessageImpl<T> implements RouterMessage {

    private final long timestamp;
    private final T theMessage;
    private final String source;

    public RouterMessageImpl(T msg, String source, long timestamp) {
        this.timestamp = timestamp;
        this.theMessage = msg;
        this.source = source;
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public Object getPayload() {
        return theMessage;
    }

    @Override
    public Message getMessage() {
        if (theMessage instanceof Message) return (Message) theMessage;
        return null;
    }

    @Override
    public JSONObject getJSON() {
        if (theMessage instanceof JSONObject) return (JSONObject) theMessage;
        return null;
    }

    @Override
    public String getSource() {
        return source;
    }
}
