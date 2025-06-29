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

package com.aboni.nmea.router.agent.impl;

import com.aboni.log.ConsoleLog;
import com.aboni.log.Log;
import com.aboni.nmea.router.OnRouterMessage;
import com.aboni.nmea.router.RouterMessage;
import com.aboni.nmea.router.RouterMessageFactory;
import com.aboni.utils.TimestampProvider;

import javax.inject.Inject;

public class NMEAConsoleTarget extends NMEAAgentImpl {

    @Inject
    public NMEAConsoleTarget(Log log, TimestampProvider tp, RouterMessageFactory messageFactory) {
        super(log, tp, messageFactory, false, true);
    }

    @OnRouterMessage
    public void onMessage(RouterMessage rm) {
        ConsoleLog.getLogger().info("[" + rm.getAgentSource() + "] " + rm.getPayload());
    }

    @Override
    public String getDescription() {
        return "Console monitor";
    }

    @Override
    public String getType() {
        return "Console";
    }

    @Override
    public String toString() {
        return getType();
    }
}
