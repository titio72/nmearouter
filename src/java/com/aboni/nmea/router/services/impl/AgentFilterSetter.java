package com.aboni.nmea.router.services.impl;

import com.aboni.nmea.router.NMEARouter;
import com.aboni.nmea.router.agent.NMEAAgent;
import com.aboni.nmea.router.conf.db.AgentStatusProvider;
import com.aboni.nmea.router.filters.FilterSetBuilder;
import com.aboni.nmea.router.filters.NMEABasicSentenceFilter;
import com.aboni.nmea.router.filters.NMEAFilterSet;
import com.aboni.utils.ServerLog;

import javax.validation.constraints.NotNull;

public class AgentFilterSetter {

    private final NMEARouter router;

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

    public AgentFilterSetter(NMEARouter router) {
        this.router = router;
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

    public String setFilter(@NotNull String agentName, @NotNull IN_OUT inOut, @NotNull String[] sentences, @NotNull String type) {
        ServerLog.getLogger().info("Setting filters {" + inOut + "} type {" + type + "} sentences {" + toString(sentences) + "}");
        NMEAAgent a = router.getAgent(agentName);
        String msg;
        if (a != null) {
            NMEAFilterSet fs = getNMEAFilterSet(sentences, type);
            String sfs = (fs != null) ? new FilterSetBuilder().exportFilter(fs) : null;
            if (inOut == IN_OUT.OUT && a.getTarget() != null) {
                a.getTarget().setFilter(fs);
                AgentStatusProvider.getAgentStatus().setFilterOutData(agentName, sfs);
            } else if (inOut == IN_OUT.IN && a.getSource() != null) {
                a.getSource().setFilter(fs);
                AgentStatusProvider.getAgentStatus().setFilterInData(agentName, sfs);
            }
            msg = "Filter set for " + agentName;
        } else {
            msg = "Agent " + agentName + " not found";
        }
        return msg;
    }
}
