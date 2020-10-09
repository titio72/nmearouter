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

package com.aboni.nmea.router.agent.impl.system;

import com.aboni.nmea.router.OnSentence;
import com.aboni.nmea.router.TimestampProvider;
import com.aboni.nmea.router.agent.impl.NMEAAgentImpl;
import com.aboni.utils.Log;
import com.aboni.utils.ThingsFactory;
import net.sf.marineapi.nmea.sentence.Sentence;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;

public class NMEASystemTimeGPS extends NMEAAgentImpl {

    private final SystemTimeChecker systemTimeCHecker;

    @Inject
    public NMEASystemTimeGPS(@NotNull Log log, @NotNull TimestampProvider tp) {
        super(log, tp, false, true);
        systemTimeCHecker = ThingsFactory.getInstance(SystemTimeChecker.class);
    }

    @Override
    public String getType() {
        return "GPSTime";
    }

    @Override
    public String getDescription() {
        return "Sync up system time with GPS UTC time feed [" + (systemTimeCHecker.isSynced() ? "Sync " + systemTimeCHecker.getTimeSkew() : "Not Sync") + "]";
    }

    @OnSentence
    public void onSentence(Sentence s, String src) {
        systemTimeCHecker.checkAndSetTime(s);
    }

    @Override
    protected boolean onActivate() {
        return true;
    }
}
