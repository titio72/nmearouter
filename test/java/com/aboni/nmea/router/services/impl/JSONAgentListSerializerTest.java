package com.aboni.nmea.router.services.impl;

import com.aboni.nmea.router.agent.AgentActivationMode;
import com.aboni.nmea.router.agent.AgentPersistentStatus;
import com.aboni.nmea.router.agent.AgentPersistentStatusManager;
import com.aboni.nmea.router.agent.NMEAAgent;
import com.aboni.nmea.router.agent.impl.AgentPersistentStatusImpl;
import com.aboni.nmea.router.agent.impl.NMEAAgentImpl;
import com.aboni.nmea.router.conf.QOS;
import com.aboni.utils.DefaultTimestampProvider;
import com.aboni.log.NullLog;
import com.aboni.utils.TimestampProvider;
import com.aboni.nmea.router.filters.NMEAFilter;
import com.aboni.nmea.router.filters.DummyFilter;
import com.aboni.log.Log;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class JSONAgentListSerializerTest {

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    static class MyAgent extends NMEAAgentImpl {

        public MyAgent(String name, Log log, TimestampProvider timestampProvider, boolean source, boolean target) {
            super(log, timestampProvider, source, target);
            setup(name, new QOS());
        }
    }

    private static class MyAgentPersistentStatusManager implements AgentPersistentStatusManager {

        @Override
        public AgentPersistentStatus getPersistentStatus(String agent) {
            switch (agent) {
                case "AG1": return new AgentPersistentStatusImpl(AgentActivationMode.MANUAL, new DummyFilter("out1"), new DummyFilter("in1"));
                case "AG2": return new AgentPersistentStatusImpl(AgentActivationMode.AUTO, new DummyFilter("out2"), new DummyFilter("in2"));
                case "AG3": return new AgentPersistentStatusImpl(AgentActivationMode.MANUAL, new DummyFilter("out3"), new DummyFilter("in3"));
                default: return null;
            }
        }

        @Override
        public void setStartMode(String agent, AgentActivationMode status) {

        }

        @Override
        public void setTargetFilter(String agent, NMEAFilter filter) {

        }

        @Override
        public void setSourceFilter(String agent, NMEAFilter filter) {

        }
    }

    private static final TimestampProvider tp = new DefaultTimestampProvider();
    private static final Log nullLog = new NullLog();
    @Test
    public void test() {
        JSONAgentListSerializer s = new JSONAgentListSerializer(new MyAgentPersistentStatusManager());
        List<NMEAAgent> agents = new ArrayList<>();
        MyAgent a1 = new MyAgent("AG1", nullLog, tp, true, true);
        a1.getSource().setFilter(new DummyFilter("out1"));
        a1.getTarget().setFilter(new DummyFilter("in1"));
        MyAgent a2 = new MyAgent("AG2", nullLog, tp, false, true);
        a2.getTarget().setFilter(new DummyFilter("in2"));
        MyAgent a3 = new MyAgent("AG3", nullLog, tp, true, false);
        a3.getSource().setFilter(new DummyFilter("out3"));
        agents.add(a1);
        agents.add(a2);
        agents.add(a3);
        System.out.println(s.getJSON(agents, "parapapapapa").toString(2));
    }
}