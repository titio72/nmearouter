package com.aboni.nmea.router.agent.impl.system;

import com.aboni.nmea.router.NMEACache;
import com.aboni.nmea.router.agent.impl.NMEAAgentImpl;
import com.aboni.nmea.router.agent.impl.track.EngineStatus;
import com.aboni.sensors.hw.PinDetector;
import com.pi4j.io.gpio.RaspiPin;

public class EngineDetectionAgent extends NMEAAgentImpl {

    private PinDetector detector;
    private EngineStatus engineRunning;
    private final NMEACache cache;

    public EngineDetectionAgent(NMEACache cache, String name) {
        super(cache, name);
        this.cache = cache;
        engineRunning = EngineStatus.UNKNOWN;
    }

    @Override
    protected boolean onActivate() {
        detector = new PinDetector(RaspiPin.GPIO_27);
        return true;
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
