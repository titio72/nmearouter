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

import com.aboni.nmea.router.OnRouterMessage;
import com.aboni.nmea.router.RouterMessage;
import com.aboni.nmea.router.TimestampProvider;
import com.aboni.nmea.router.agent.impl.NMEAAgentImpl;
import com.aboni.nmea.router.message.Message;
import com.aboni.nmea.router.message.MsgSystemTime;
import com.aboni.nmea.router.utils.Log;
import org.json.JSONObject;

import javax.inject.Inject;

public class NMEASystemTimeGPS extends NMEAAgentImpl {

    private final SystemTimeChecker systemTimeCHecker;

    @Inject
    public NMEASystemTimeGPS(Log log, TimestampProvider tp, SystemTimeChecker checker) {
        super(log, tp, true, true);
        if (checker==null) throw new IllegalArgumentException("SystemTimeChecker is null");
        this.systemTimeCHecker = checker;
    }

    @Override
    public String getType() {
        return "GPSTime";
    }

    @Override
    public String getDescription() {
        return "Sync up system time with GPS UTC time feed [" + (systemTimeCHecker.isSynced() ? "Sync " + systemTimeCHecker.getTimeSkew() : "Not Sync") + "]";
    }

    @OnRouterMessage
    public void onSentence(RouterMessage msg) {
        Message m = msg.getMessage();
        if (m instanceof MsgSystemTime) {
            systemTimeCHecker.checkAndSetTime(((MsgSystemTime) m).getTime());
            postMsg();
        }
    }

    private void postMsg() {
        long skew = systemTimeCHecker.getTimeSkew();
        boolean sync = systemTimeCHecker.isSynced();
        JSONObject msg = new JSONObject();
        msg.put("topic", "time");
        msg.put("synced", sync);
        msg.put("skew", skew);
        postMessage(msg);

    }

    @Override
    protected boolean onActivate() {
        return true;
    }
}
