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

import com.aboni.nmea.router.NMEACache;
import com.aboni.nmea.router.NMEARouterStatuses;
import com.aboni.nmea.router.TimestampProvider;
import com.aboni.nmea.router.agent.impl.NMEAAgentImpl;
import com.aboni.sensors.EngineDetector;
import com.aboni.sensors.EngineStatus;
import com.aboni.utils.Log;
import org.json.JSONObject;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;

public class EngineDetectionAgent extends NMEAAgentImpl {

    private final NMEACache cache;
    private EngineStatus engineRunning;

    private final Log log;

    @Inject
    public EngineDetectionAgent(@NotNull TimestampProvider tp, @NotNull NMEACache cache, @NotNull Log log) {
        super(log, tp, true, false);
        this.log = log;
        this.cache = cache;
        engineRunning = EngineStatus.UNKNOWN;
    }

    @Override
    public void onTimer() {
        super.onTimer();
        refreshEngine();
    }

    private void refreshEngine() {
        EngineDetector.getInstance().refresh();
        EngineStatus localEngineRunning = EngineDetector.getInstance().isEngineOn() ? EngineStatus.ON : EngineStatus.OFF;
        if (engineRunning != localEngineRunning) {
            log.info(getLogBuilder().wO("status change").wV("status", localEngineRunning).toString());
        }
        engineRunning = localEngineRunning;
        cache.setStatus(NMEARouterStatuses.ENGINE_STATUS, engineRunning);
        notifyEngineStatus();
    }

    private void notifyEngineStatus() {
        JSONObject msg = new JSONObject();
        msg.put("topic", "engine");
        msg.put("status", engineRunning);
        postMessage(msg);
    }

    @Override
    protected boolean onActivate() {
        refreshEngine();
        return true;
    }

    @Override
    protected void onDeactivate() {
        engineRunning = EngineStatus.UNKNOWN;
        cache.setStatus(NMEARouterStatuses.ENGINE_STATUS, engineRunning);
    }

    @Override
    public String getDescription() {
        return "Engine running status [" + engineRunning + "]";
    }
}
