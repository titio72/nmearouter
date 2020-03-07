package com.aboni.nmea.router.services;

import com.aboni.nmea.router.NMEARouter;
import com.aboni.nmea.router.services.impl.AgentFilterSetter;
import com.aboni.nmea.router.services.impl.AgentListSerializer;

public class AgentFilterService extends JSONWebService {

	private final NMEARouter router;
	private final boolean isOut;

	public AgentFilterService(NMEARouter router, String inOut) {
		super();
		this.router = router;
		this.isOut = "out".equals(inOut);
		setLoader((ServiceConfig config) -> {
			try {
				String agentName = config.getParameter("agent");
				String[] sentences = config.getParameter("sentences").split(",");
				String type = config.getParameter("type");
				String msg = new AgentFilterSetter(router).setFilter(agentName, isOut ? AgentFilterSetter.IN_OUT.OUT : AgentFilterSetter.IN_OUT.IN, sentences, type);
				AgentListSerializer serializer = new AgentListSerializer(router);
				return serializer.getJSON(msg);
			} catch (Exception e) {
				throw new JSONGenerationException(e);
			}
		});
	}
}
