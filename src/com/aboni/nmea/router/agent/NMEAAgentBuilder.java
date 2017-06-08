package com.aboni.nmea.router.agent;

import com.aboni.nmea.router.NMEARouter;
import com.aboni.nmea.router.conf.AgentBase;

public interface NMEAAgentBuilder {

	NMEAAgent createAgent(AgentBase a, NMEARouter r);

}