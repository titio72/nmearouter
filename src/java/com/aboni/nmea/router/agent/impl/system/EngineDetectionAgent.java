package com.aboni.nmea.router.agent.impl.system;

import com.aboni.nmea.router.NMEACache;
import com.aboni.nmea.router.agent.QOS;
import com.aboni.nmea.router.agent.impl.NMEAAgentImpl;
import com.aboni.sensors.EngineDetector;
import com.aboni.sensors.EngineStatus;
import com.aboni.utils.ServerLog;
import org.json.JSONObject;

public class EngineDetectionAgent extends NMEAAgentImpl {

    private EngineStatus engineRunning;

    public EngineDetectionAgent(NMEACache cache, String name, QOS q) {
        super(cache, name, q);
        setSourceTarget(true, false);
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
            ServerLog.getLogger().info("Engine status change {" + localEngineRunning + "}");
        }
        engineRunning = localEngineRunning;
        getCache().setStatus("Engine", engineRunning);
        notifyEngineStatus();
    }

    private void notifyEngineStatus() {
        JSONObject msg = new JSONObject();
        msg.put("topic", "engine");
        msg.put("status", engineRunning);
        notify(msg);
    }

    @Override
    protected boolean onActivate() {
        refreshEngine();
        return true;
    }

    @Override
    protected void onDeactivate() {
        engineRunning = EngineStatus.UNKNOWN;
        getCache().setStatus("Engine", engineRunning);
    }

    @Override
    public boolean isUserCanStartAndStop() {
        return false;
    }

    @Override
    public String getDescription() {
        return "Engine running status [" + engineRunning + "]";
    }
}
