package com.aboni.nmea.router.agent.impl;

import com.aboni.nmea.router.agent.AgentActivationMode;
import com.aboni.nmea.router.agent.AgentPersistentStatus;
import com.aboni.nmea.router.filters.NMEAFilter;
import org.json.JSONObject;

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

    @Override
    public JSONObject toJSON() {
        JSONObject res = new JSONObject();
        res.put("auto", getStatus() == AgentActivationMode.AUTO);
        if (filterSource != null) res.put("conf_filterSource", filterSource.toJSON());
        if (filterTarget != null) res.put("conf_filterTarget", filterTarget.toJSON());
        return res;
    }
}
