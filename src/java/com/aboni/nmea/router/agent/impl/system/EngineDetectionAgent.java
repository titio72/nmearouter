package com.aboni.nmea.router.agent.impl.system;

import com.aboni.nmea.router.NMEACache;
import com.aboni.nmea.router.agent.QOS;
import com.aboni.nmea.router.agent.impl.NMEAAgentImpl;
import com.aboni.nmea.router.agent.impl.track.EngineStatus;
import com.aboni.sensors.EngineDetector;
import org.json.JSONObject;

public class EngineDetectionAgent extends NMEAAgentImpl {

    private EngineStatus engineRunning;
    private final NMEACache cache;

    public EngineDetectionAgent(NMEACache cache, String name, QOS q) {
        super(cache, name, q);
        setSourceTarget(true, false);
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
        engineRunning = EngineDetector.getInstance().isEngineOn() ? EngineStatus.ON : EngineStatus.OFF;
        cache.setStatus("Engine", engineRunning);
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
        cache.setStatus("Engine", engineRunning);
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
