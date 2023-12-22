package com.aboni.nmea.router.agent;

import com.aboni.nmea.router.JSONable;
import com.aboni.nmea.router.filters.NMEAFilter;

public interface AgentPersistentStatus extends JSONable {
    AgentActivationMode getStatus();

    NMEAFilter getSourceFilter();

    NMEAFilter getTargetFilter();
}
