package com.aboni.nmea.router.services;

import com.aboni.nmea.router.NMEARouter;
import com.aboni.nmea.router.services.impl.AgentFilterSetter;
import com.aboni.nmea.router.services.impl.AgentListSerializer;

import javax.inject.Inject;

public class AgentFilterService extends JSONWebService {

    @Inject
    public AgentFilterService(NMEARouter router) {
        super();
        setLoader((ServiceConfig config) -> {
            try {
                String agentName = config.getParameter("agent");
                String[] sentences = config.getParameter("sentences").split(",");
                String type = config.getParameter("type");
                boolean isOut = "out".equals(config.getParameter("direction", "in"));
                String msg = new AgentFilterSetter(router).setFilter(agentName, isOut ? AgentFilterSetter.IN_OUT.OUT : AgentFilterSetter.IN_OUT.IN, sentences, type);
                AgentListSerializer serializer = new AgentListSerializer(router);
                return serializer.getJSON(msg);
            } catch (Exception e) {
				throw new JSONGenerationException(e);
			}
		});
	}
}
