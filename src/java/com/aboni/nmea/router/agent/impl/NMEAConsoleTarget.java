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

import com.aboni.nmea.router.OnRouterMessage;
import com.aboni.nmea.router.RouterMessage;
import com.aboni.nmea.router.TimestampProvider;
import com.aboni.utils.ConsoleLog;
import com.aboni.utils.Log;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.text.DateFormat;
import java.util.Date;

public class NMEAConsoleTarget extends NMEAAgentImpl {

    @Inject
    public NMEAConsoleTarget(@NotNull Log log, @NotNull TimestampProvider tp) {
        super(log, tp, false, true);
    }

    @OnRouterMessage
    public void onSentence(RouterMessage s) {
        ConsoleLog.getLogger().console(DateFormat.getTimeInstance(DateFormat.MEDIUM).format(new Date()) +
                " [" + s.getPayload() + "] " + s);
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
