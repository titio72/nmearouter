package com.aboni.nmea.router.agent.impl;

import com.aboni.nmea.router.agent.AgentActivationMode;
import com.aboni.nmea.router.agent.AgentPersistentStatus;
import com.aboni.nmea.router.filters.NMEAFilter;

public class AgentPersistentStatusImpl implements AgentPersistentStatus {

    @Override
    public AgentActivationMode getStatus() {
        return status;
    }

    @Override
    public NMEAFilter getSourceFilter() {
        return filterSource;
    }

    @Override
    public NMEAFilter getTargetFilter() {
        return filterTarget;
    }

    private final AgentActivationMode status;
    private final NMEAFilter filterTarget;
    private final NMEAFilter filterSource;

    public AgentPersistentStatusImpl(AgentActivationMode agSt, NMEAFilter fTarget, NMEAFilter fSource) {
        this.status = agSt;
        this.filterTarget = fTarget;
        this.filterSource = fSource;
    }
}
