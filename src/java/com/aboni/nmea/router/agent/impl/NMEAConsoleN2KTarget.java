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

import com.aboni.nmea.router.NMEACache;
import com.aboni.nmea.router.OnN2KMessage;
import com.aboni.nmea.router.n2k.N2KMessage;
import com.aboni.utils.ConsoleLog;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.text.DateFormat;
import java.util.Date;

public class NMEAConsoleN2KTarget extends NMEAAgentImpl {

    @Inject
    public NMEAConsoleN2KTarget(@NotNull NMEACache cache) {
        super(cache);
        setSourceTarget(false, true);
    }

    @OnN2KMessage
    public void onSentence(N2KMessage s, String src) {
        ConsoleLog.getLogger().console(DateFormat.getTimeInstance(DateFormat.MEDIUM).format(new Date()) +
                " [" + src + "] " + s);
    }

    @Override
    public String getDescription() {
        return "N2K Console monitor";
    }

    @Override
    public String getType() {
        return "ConsoleN2K";
    }

    @Override
    public String toString() {
        return getType();
    }
}
