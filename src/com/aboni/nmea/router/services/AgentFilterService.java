package com.aboni.nmea.router.services;

import com.aboni.nmea.router.NMEARouter;
import com.aboni.nmea.router.agent.NMEAAgent;
import com.aboni.nmea.router.conf.db.AgentStatusProvider;
import com.aboni.nmea.router.filters.FilterSetBuilder;
import com.aboni.nmea.router.filters.NMEABasicSentenceFilter;
import com.aboni.nmea.router.filters.NMEAFilterSet;
import com.aboni.nmea.router.filters.NMEAFilterSet.TYPE;

public class AgentFilterService implements WebService {

	private NMEARouter router;
	private boolean isOut;
	
	public AgentFilterService(NMEARouter router, String inOut) {
		this.router = router;
		isOut = "out".equals(inOut);
	}
	
	private class FltSentence {
		String sentence = "";
		String source = "";
		
		public FltSentence(String s) {
			String[] _s = s.split("@");
			sentence = _s[0];
			if (_s.length==2) source = _s[1];
		}
		
		NMEABasicSentenceFilter getFilter() {
			return new NMEABasicSentenceFilter(sentence, source);
		}
	}
	
    @Override
    public void doIt(ServiceConfig config, ServiceOutput response) {
        response.setContentType("application/json");
        try {
        	String agentname = config.getParameter("agent"); 
        	String[] sentences = config.getParameter("sentences").split(",");
            String type = config.getParameter("type");
            NMEAAgent a = router.getAgent(agentname);
            String msg = "";
            if (a!=null) {
	            NMEAFilterSet fs = null;
	            if (sentences.length!=0) {
		            fs = new NMEAFilterSet("whitelist".equals(type)?TYPE.WHITELIST:TYPE.BLACKLIST);
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
            AgentListSerializer serializer = new AgentListSerializer(router);            
            serializer.dump(response.getWriter(), msg);
            response.ok();
        } catch (Exception e) {
            try { e.printStackTrace(response.getWriter()); } catch (Exception ee) {}
            response.ok();
        }
    }
}
