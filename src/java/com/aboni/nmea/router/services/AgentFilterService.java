package com.aboni.nmea.router.services;

import com.aboni.nmea.router.NMEARouter;
import com.aboni.nmea.router.agent.NMEAAgent;
import com.aboni.nmea.router.conf.db.AgentStatusProvider;
import com.aboni.nmea.router.filters.FilterSetBuilder;
import com.aboni.nmea.router.filters.NMEABasicSentenceFilter;
import com.aboni.nmea.router.filters.NMEAFilterSet;
import com.aboni.nmea.router.filters.NMEAFilterSet.TYPE;
import com.aboni.utils.ServerLog;

import java.io.IOException;

public class AgentFilterService implements WebService {

	private final NMEARouter router;
	private final boolean isOut;
	
	public AgentFilterService(NMEARouter router, String inOut) {
		this.router = router;
		isOut = "out".equals(inOut);
	}
	
	private class FltSentence {
		final String sentence;
		final String source;
		
		public FltSentence(String s) {
			String[] _s = s.split("@");
			sentence = _s[0];
			source = (_s.length==2)?_s[1]:"";
		}
		
		NMEABasicSentenceFilter getFilter() {
			return new NMEABasicSentenceFilter(sentence, source);
		}
	}
	
    @Override
    public void doIt(ServiceConfig config, ServiceOutput response) {
        response.setContentType("application/json");
		String agentname = config.getParameter("agent");
		String[] sentences = config.getParameter("sentences").split(",");
		String type = config.getParameter("type");
		String msg = setFilter(agentname, sentences, type);
		sendResponse(response, msg);
	}

	private String setFilter(String agentname, String[] sentences, String type) {
		NMEAAgent a = router.getAgent(agentname);
		String msg;
		if (a!=null) {
			NMEAFilterSet fs = null;
			if (sentences.length!=0) {
				fs = new NMEAFilterSet("whitelist".equals(type)? TYPE.WHITELIST: TYPE.BLACKLIST);
				boolean atLeast1 = false;
				for (String str: sentences) {
					str = str.trim();
					FltSentence fltSentence = new FltSentence(str.trim());
					if (!fltSentence.sentence.isEmpty()) {
						NMEABasicSentenceFilter f = fltSentence.getFilter();
						fs.addFilter(f);
						atLeast1 = true;
					}
				}
				fs = (atLeast1?fs:null);
			}
			String sfs = (fs!=null)?new FilterSetBuilder().exportFilter(fs):null;
			if (isOut && a.getTarget()!=null) {
				a.getTarget().setFilter(fs);
				AgentStatusProvider.getAgentStatus().setFilterOutData(agentname, sfs);
			} else if (!isOut && a.getSource()!=null){
				a.getSource().setFilter(fs);
				AgentStatusProvider.getAgentStatus().setFilterInData(agentname, sfs);
			}
			msg = "Filter set for " + agentname;
		} else {
			msg = "Agent " + agentname + " not found";
		}
		return msg;
	}

	private void sendResponse(ServiceOutput response, String msg) {
		AgentListSerializer serializer = new AgentListSerializer(router);
		try {
            serializer.dump(response.getWriter(), msg);
            response.ok();
        } catch (IOException e) {
			ServerLog.getLogger().Error("Error serving web call", e);
            response.ok();
        }
	}
}