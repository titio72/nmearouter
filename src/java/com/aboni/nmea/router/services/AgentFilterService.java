/*
(C) 2020, Andrea Boni
This file is part of NMEARouter.
NMEARouter is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.
NMEARouter is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.
You should have received a copy of the GNU General Public License
along with NMEARouter.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.aboni.nmea.router.services;

import com.aboni.log.Log;
import com.aboni.nmea.router.NMEARouter;
import com.aboni.nmea.router.agent.AgentPersistentStatusManager;
import com.aboni.nmea.router.agent.NMEAAgent;
import com.aboni.nmea.router.filters.DummyFilter;
import com.aboni.nmea.router.filters.FilterFactory;
import com.aboni.nmea.router.filters.NMEAFilter;
import com.aboni.nmea.router.filters.NMEAFilterSet;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

public class AgentFilterService extends JSONWebService {

    public enum SOURCE_TARGET {
        SOURCE,
        TARGET
    }

    private static class FltSentence {
        final String sentence;
        final String source;

        FltSentence(String s) {
            String[] tokens = s.split("@");
            sentence = tokens[0];
            source = (tokens.length == 2) ? tokens[1] : "";
        }

        NMEAFilter getFilter(FilterFactory factory) {
            return factory.getNMEA0183Filter(sentence, source);
        }
    }

    private final FilterFactory filterFactory;

    @Inject
    public AgentFilterService(Log log, NMEARouter router,
                              AgentPersistentStatusManager agentStatusManager,
                              FilterFactory filterFactory) {
        super(log);
        if (filterFactory == null) throw new IllegalArgumentException("FilterFactory is null");
        this.filterFactory = filterFactory;
        setLoader(new AgentServiceHelper(router, agentStatusManager) {
            @Override
            protected String execute(ServiceConfig config) throws ServiceException {
                return executeFilter(getRouter(), getAgentStatusManager(), config);
            }
        });
    }

    protected String executeFilter(NMEARouter router, AgentPersistentStatusManager agentPersistentStatusManager, ServiceConfig config) throws ServiceException {
        try {
            String agentName = config.getParameter("agent");
            String[] sentences = cleanSentences(config.getParameter("sentences", "").split(","));
            String type = config.getParameter("type");
            SOURCE_TARGET sourceTarget =
                    "source".equals(config.getParameter("direction", "source")) ? SOURCE_TARGET.SOURCE : SOURCE_TARGET.TARGET;
            NMEAFilter fs = getNMEAFilterSet(sentences, type);
            return setFilter(router, agentPersistentStatusManager, agentName, fs, sourceTarget);
        } catch (Exception e) {
            throw new ServiceException(e);
        }
    }


    private static String[] cleanSentences(String[] sentences) {
        List<String> listSentences = new ArrayList<>();
        for (String s : sentences) if (s != null && !s.trim().isEmpty()) listSentences.add(s.trim());
        return listSentences.toArray(new String[0]);
    }

    private NMEAFilter getNMEAFilterSet(String[] sentences, String type) {
        if (sentences == null) sentences = new String[0];
        if (sentences.length == 0 && "blacklist".equals(type)) {
            return new DummyFilter("");
        } else {
            NMEAFilterSet fs = filterFactory.createFilterSet("whitelist".equals(type) ? NMEAFilterSet.TYPE.WHITELIST : NMEAFilterSet.TYPE.BLACKLIST);
            for (String str : sentences) {
                str = str.trim();
                FltSentence fltSentence = new FltSentence(str.trim());
                if (!fltSentence.sentence.isEmpty()) {
                    NMEAFilter f = fltSentence.getFilter(filterFactory);
                    fs.addFilter(f);
                }
            }
            return fs;
        }
    }

    private String setFilter(NMEARouter router, AgentPersistentStatusManager agentStatusManager,
                             String agentName, NMEAFilter fs, SOURCE_TARGET inOut) {
        if (router==null) throw new IllegalArgumentException("Router is null");
        if (agentName==null || agentName.isEmpty()) throw new IllegalArgumentException("Agent name " + agentName + " is invalid or null");
        if (fs==null) throw new IllegalArgumentException("Filter set implementation is null");
        NMEAAgent a = router.getAgent(agentName);
        String msg;
        if (a != null) {
            saveFilter(agentStatusManager, agentName, fs, inOut, a);
            msg = "Filter set for " + agentName;
        } else {
            msg = "Agent " + agentName + " not found";
        }
        return msg;
    }

    private static void saveFilter(AgentPersistentStatusManager agentStatusManager, String agentName, NMEAFilter fs, SOURCE_TARGET inOut, NMEAAgent a) {
        switch (inOut) {
            case TARGET:
                if (a.getTarget() != null) {
                    a.getTarget().setFilter(fs);
                    agentStatusManager.setTargetFilter(agentName, fs);
                }
                break;
            case SOURCE:
                if (a.getSource() != null) {
                    a.getSource().setFilter(fs);
                    agentStatusManager.setSourceFilter(agentName, fs);
                }
                break;
            default:
        }
    }
}
