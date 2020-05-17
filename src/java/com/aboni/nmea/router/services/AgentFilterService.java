package com.aboni.nmea.router.services;

import com.aboni.nmea.router.NMEARouter;
import com.aboni.nmea.router.agent.AgentStatusManager;
import com.aboni.nmea.router.agent.NMEAAgent;
import com.aboni.nmea.router.filters.impl.JSONFilterSetSerializer;
import com.aboni.nmea.router.filters.impl.NMEAFilterSet;
import com.aboni.nmea.router.services.impl.AgentListSerializer;
import com.aboni.nmea.sentences.NMEABasicSentenceFilter;
import com.aboni.utils.ServerLog;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;

public class AgentFilterService extends JSONWebService {

    public enum IN_OUT {
        IN,
        OUT
    }

    private static class FltSentence {
        final String sentence;
        final String source;

        FltSentence(String s) {
            String[] tokens = s.split("@");
            sentence = tokens[0];
            source = (tokens.length == 2) ? tokens[1] : "";
        }

        NMEABasicSentenceFilter getFilter() {
            return new NMEABasicSentenceFilter(sentence, source);
        }
    }

    @Inject
    public AgentFilterService(@NotNull NMEARouter router, @NotNull AgentListSerializer serializer,
                              @NotNull AgentStatusManager agentStatusManager) {
        super();
        setLoader((ServiceConfig config) -> {
            try {
                String agentName = config.getParameter("agent");
                String[] sentences = config.getParameter("sentences").split(",");
                String type = config.getParameter("type");
                boolean inOut = "out".equals(config.getParameter("direction", "in"));
                ServerLog.getLogger().info("Setting filters {" + inOut + "} type {" + type + "} sentences {" + toString(sentences) + "}");
                NMEAFilterSet fs = getNMEAFilterSet(sentences, type);
                String msg = setFilter(router, agentStatusManager, agentName, fs, inOut ? IN_OUT.OUT : IN_OUT.IN);
                return serializer.getJSON(router, msg);
            } catch (Exception e) {
                throw new JSONGenerationException(e);
            }
        });
    }

    private static NMEAFilterSet getNMEAFilterSet(String[] sentences, String type) {
        NMEAFilterSet fs;
        fs = new NMEAFilterSet("whitelist".equals(type) ? NMEAFilterSet.TYPE.WHITELIST : NMEAFilterSet.TYPE.BLACKLIST);
        boolean atLeast1 = false;
        for (String str : sentences) {
            str = str.trim();
            FltSentence fltSentence = new FltSentence(str.trim());
            if (!fltSentence.sentence.isEmpty()) {
                NMEABasicSentenceFilter f = fltSentence.getFilter();
                fs.addFilter(f);
                atLeast1 = true;
            }
        }
        fs = (atLeast1 ? fs : null);
        return fs;
    }

    private static String toString(String[] ss) {
        StringBuilder r = new StringBuilder();
        for (String s : ss) r.append(" ").append(s);
        return r.toString();
    }

    private String setFilter(@NotNull NMEARouter router, @NotNull AgentStatusManager agentStatusManager,
                             @NotNull String agentName, NMEAFilterSet fs, IN_OUT inOut) {
        NMEAAgent a = router.getAgent(agentName);
        String msg;
        if (a != null) {
            String sfs = (fs != null) ? new JSONFilterSetSerializer().exportFilter(fs) : null;
            switch (inOut) {
                case OUT:
                    if (a.getTarget() != null) {
                        a.getTarget().setFilter(fs);
                        agentStatusManager.setFilterOutData(agentName, sfs);
                    }
                    break;
                case IN:
                    if (a.getSource() != null) {
                        a.getSource().setFilter(fs);
                        agentStatusManager.setFilterInData(agentName, sfs);
                    }
                    break;
                default:
            }
            msg = "Filter set for " + agentName;
        } else {
            msg = "Agent " + agentName + " not found";
        }
        return msg;
    }
}
