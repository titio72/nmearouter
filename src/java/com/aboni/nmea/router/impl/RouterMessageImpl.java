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

import com.aboni.nmea.message.Message;
import com.aboni.nmea.router.RouterMessage;

public class RouterMessageImpl implements RouterMessage {

    private final long timestamp;
    private final Message theMessage;
    private final String agentSource;

    public RouterMessageImpl(Message msg, String agentSource, long timestamp) {
        this.timestamp = timestamp;
        this.theMessage = msg;
        this.agentSource = agentSource;
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public Message getPayload() {
        return theMessage;
    }

    @Override
    public String getAgentSource() {
        return agentSource;
    }
}
