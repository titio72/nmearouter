package com.aboni.nmea.router;

import com.aboni.nmea.router.agent.NMEAAgent;
import com.aboni.nmea.router.processors.NMEAPostProcess;

import java.util.Collection;

public interface NMEARouter extends Startable {

    void addProcessor(NMEAPostProcess p);
    void addAgent(NMEAAgent source);
    NMEAAgent getAgent(String name);
    Collection<String> getAgents();

}