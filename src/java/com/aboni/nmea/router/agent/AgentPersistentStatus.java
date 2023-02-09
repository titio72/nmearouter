package com.aboni.nmea.router.agent;

import com.aboni.nmea.router.filters.NMEAFilter;

public interface AgentPersistentStatus {
    AgentActivationMode getStatus();

    NMEAFilter getSourceFilter();

    NMEAFilter getTargetFilter();
}
