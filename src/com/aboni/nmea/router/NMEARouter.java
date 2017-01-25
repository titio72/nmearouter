package com.aboni.nmea.router;

import java.util.Collection;

import com.aboni.nmea.router.agent.NMEAAgent;
import com.aboni.nmea.router.conf.LogLevelType;

public interface NMEARouter extends Startable {

    void addAgent(NMEAAgent source);
    NMEAAgent getAgent(String name);
    Collection<String> getAgents();
    
    NMEACache getCache();
    
    LogLevelType getPreferredLogLevelType();
}