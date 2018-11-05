package com.aboni.nmea.router;

import java.util.Collection;

import com.aboni.nmea.router.agent.NMEAAgent;

public interface NMEARouter extends Startable {

    void addAgent(NMEAAgent source);
    NMEAAgent getAgent(String name);
    Collection<String> getAgents();

}