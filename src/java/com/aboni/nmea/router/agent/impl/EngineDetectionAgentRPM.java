package com.aboni.nmea.router.agent.impl;

import com.aboni.log.Log;
import com.aboni.nmea.message.MsgEngine;
import com.aboni.nmea.router.*;
import com.aboni.sensors.EngineStatus;
import com.aboni.utils.TimestampProvider;

import javax.inject.Inject;

public class EngineDetectionAgentRPM extends NMEAAgentImpl {

    private final NMEACache cache;
    private EngineStatus engineRunning;

    @Inject
    public EngineDetectionAgentRPM(TimestampProvider tp, NMEACache cache, Log log, RouterMessageFactory messageFactory) {
        super(log, tp, messageFactory, true, true);
        if (cache==null) throw new IllegalArgumentException("Cache is null");
        this.cache = cache;
        engineRunning = EngineStatus.UNKNOWN;
    }

    private void refreshEngine(EngineStatus newStatus) {
        if (engineRunning != newStatus) {
            getLog().info(getLogBuilder().wO("status change").wV("status", newStatus).toString());
        }
        engineRunning = newStatus;
        cache.setStatus(NMEARouterStatuses.ENGINE_STATUS, engineRunning);
    }

    @Override
    protected boolean onActivate() {
        engineRunning = EngineStatus.UNKNOWN;
        cache.setStatus(NMEARouterStatuses.ENGINE_STATUS, engineRunning);
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

    @OnRouterMessage
    public void onMessage(RouterMessage msg) {
        if (msg.getPayload() instanceof MsgEngine) {
           EngineStatus status = ((MsgEngine) msg.getPayload()).getRPM()>50 ? EngineStatus.ON : EngineStatus.OFF;
           refreshEngine(status);
        }
    }
}
