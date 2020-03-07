package com.aboni.nmea.router.impl;

import com.aboni.nmea.router.NMEACache;
import com.aboni.nmea.router.NMEAFilterable;
import com.aboni.nmea.router.NMEARouter;
import com.aboni.nmea.router.NMEARouterBuilder;
import com.aboni.nmea.router.agent.NMEAAgent;
import com.aboni.nmea.router.agent.NMEAAgentBuilder;
import com.aboni.nmea.router.agent.QOS;
import com.aboni.nmea.router.agent.impl.DepthStatsAgent;
import com.aboni.nmea.router.agent.impl.NMEA2FileAgent;
import com.aboni.nmea.router.agent.impl.NMEAAutoPilotAgent;
import com.aboni.nmea.router.agent.impl.system.EngineDetectionAgent;
import com.aboni.nmea.router.agent.impl.system.FanAgent;
import com.aboni.nmea.router.agent.impl.system.NMEASystemTimeGPS;
import com.aboni.nmea.router.agent.impl.system.PowerLedAgent;
import com.aboni.nmea.router.conf.AgentBase;
import com.aboni.nmea.router.conf.ConfParser;
import com.aboni.nmea.router.conf.MalformedConfigurationException;
import com.aboni.nmea.router.conf.Router;
import com.aboni.nmea.router.conf.db.AgentStatus;
import com.aboni.nmea.router.conf.db.AgentStatus.STATUS;
import com.aboni.nmea.router.conf.db.AgentStatusProvider;
import com.aboni.nmea.router.filters.FilterSetBuilder;
import com.aboni.nmea.router.processors.NMEASourcePriorityProcessor;
import com.aboni.utils.ServerLog;
import com.aboni.utils.ThingsFactory;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NMEARouterDefaultBuilderImpl implements NMEARouterBuilder {

    private NMEARouter router;
    private final String confFile;
	private static final boolean ENABLE_GPS_TIME = true;
	private static final boolean ENABLE_AP = false;

    public NMEARouterDefaultBuilderImpl(String confFile) {
        this.confFile = confFile;
    }
    
    @Override
    public NMEARouterBuilder init() {
    	Router conf;
		try {
            conf = parseConf(confFile);
            NMEAAgentBuilder builder = ThingsFactory.getInstance(NMEAAgentBuilder.class);
            router = buildRouter(conf, builder);
            return this;
        } catch (MalformedConfigurationException e) {
			Logger.getGlobal().log(Level.SEVERE, "Error", e);
		}
        return null;
    }

    private NMEARouter buildRouter(Router conf, NMEAAgentBuilder builder) {
        NMEARouter r = ThingsFactory.getInstance(NMEARouter.class);

		configureGPSPriority(conf, r);

		switch (conf.getLog().getLevel()) {
			case DEBUG: 
				ServerLog.getLoggerAdmin().setDebug(); break;
			case WARNING: 
	        	ServerLog.getLoggerAdmin().setWarning(); break;
			case ERROR: 
	        	ServerLog.getLoggerAdmin().setError(); break;
			case NONE: 
	        	ServerLog.getLoggerAdmin().setNone(); break;
	    	default:
	        	ServerLog.getLoggerAdmin().setInfo(); break;
		}
        
        for (AgentBase a: conf.getSerialAgentOrTcpAgentOrUdpAgent()) {
        	NMEAAgent agent = builder.createAgent(a);
            if (agent!=null) {
            	r.addAgent(agent);
            	handlePersistentState(agent, a);
            }
        }
        
        if (ENABLE_GPS_TIME) buildGPSTimeTarget(r);
        buildStreamDump(r);
        buildPowerLedTarget(r);
        buildFanTarget(r);
        buildDPTStats(r);
        buildEngineDetector(r);
        if (ENABLE_AP) buildAutoPilot(r);
        return r;
    }

	private void configureGPSPriority(Router conf, NMEARouter r) {
		com.aboni.nmea.router.conf.List gpsPriorityConf = conf.getGPSPriority();
		if (gpsPriorityConf!=null) {
            List<String> gpsPriority = gpsPriorityConf.getGPSSource();
            NMEASourcePriorityProcessor proc = new NMEASourcePriorityProcessor(ThingsFactory.getInstance(NMEACache.class));
            proc.addAllGPS();
            for (int i = 0; i < gpsPriority.size(); i++) {
                proc.setPriority(gpsPriority.get(i), gpsPriority.size() - i /* first has the highest priority */);
            }
            r.addProcessor(proc);
        }
	}

	private void handlePersistentState(NMEAAgent agent, AgentBase a) {
    	boolean activate = handleActivation(agent, a);
    	handleFilter(agent);
		if (activate) agent.start();
	}

	private void handleFilter(NMEAAgent agent) {
    	AgentStatus s = AgentStatusProvider.getAgentStatus();
    	if (s!=null) {
    		NMEAFilterable tgt = agent.getTarget();
    		if (tgt!=null) {
	    		String data = s.getFilterOutData(agent.getName());  
	    		if (data==null)  {
	    			s.setFilterOutData(agent.getName(), new FilterSetBuilder().exportFilter(tgt.getFilter()));
	    		} else {
	    			tgt.setFilter(new FilterSetBuilder().importFilter(data));
	    		}
    		}
    		NMEAFilterable src = agent.getSource();
    		if (src!=null) {
	    		String data = s.getFilterInData(agent.getName());  
	    		if (data==null)  {
	    			s.setFilterInData(agent.getName(), new FilterSetBuilder().exportFilter(src.getFilter()));
	    		} else {
	    			src.setFilter(new FilterSetBuilder().importFilter(data));
	    		}
    		}
    	}
	}

	private boolean handleActivation(NMEAAgent agent, AgentBase a) {
        AgentStatus s = AgentStatusProvider.getAgentStatus();
        boolean active = (a != null) && a.isActive();
        if (s != null) {
            AgentStatus.STATUS requestedStatus = s.getStartMode(agent.getName());
            if (requestedStatus == STATUS.UNKNOWN) {
                s.setStartMode(agent.getName(), active ? STATUS.AUTO : STATUS.MANUAL);
            } else {
                active = (requestedStatus == STATUS.AUTO);
            }
        }
        return active;
    }
    
    private void buildStreamDump(NMEARouter r) {
        QOS q = createBuiltInQOS();
        NMEA2FileAgent dumper = new NMEA2FileAgent(
                ThingsFactory.getInstance(NMEACache.class),
                "Log", q);
        r.addAgent(dumper);
        handlePersistentState(dumper, null);
        handleFilter(dumper);
    }

	private void buildDPTStats(NMEARouter r) {
        QOS q = createBuiltInQOS();
        DepthStatsAgent a = new DepthStatsAgent(
                ThingsFactory.getInstance(NMEACache.class),
                "Depth", q);
        r.addAgent(a);
        a.start();
    }

    private void buildPowerLedTarget(NMEARouter r) {
    	if (System.getProperty("os.arch").startsWith("arm")) {
            QOS q = createBuiltInQOS();
            PowerLedAgent pwrLed = new PowerLedAgent(
                    ThingsFactory.getInstance(NMEACache.class),
                    "PowerLed", q);
            r.addAgent(pwrLed);
            pwrLed.start();
        }
    }

    private void buildAutoPilot(NMEARouter r) {
        QOS q = createBuiltInQOS();
        NMEAAutoPilotAgent ap = new NMEAAutoPilotAgent(
                ThingsFactory.getInstance(NMEACache.class),
                "SmartPilot", q);
        r.addAgent(ap);
        ap.start();
    }

    private void buildFanTarget(NMEARouter r) {
        QOS q = createBuiltInQOS();
        FanAgent fan = new FanAgent(
                ThingsFactory.getInstance(NMEACache.class),
                "FanManager", q);
        r.addAgent(fan);
        fan.start();
    }

    private void buildEngineDetector(NMEARouter r) {
        QOS q = createBuiltInQOS();
        EngineDetectionAgent eng = new EngineDetectionAgent(
                ThingsFactory.getInstance(NMEACache.class),
                "EngineManager", q);
        r.addAgent(eng);
        eng.start();
    }

    private void buildGPSTimeTarget(NMEARouter r) {
        QOS q = createBuiltInQOS();
        NMEASystemTimeGPS gpsTime = new NMEASystemTimeGPS(
                ThingsFactory.getInstance(NMEACache.class),
                "GPSTime", q);
        r.addAgent(gpsTime);
        handleFilter(gpsTime);
        gpsTime.start();
    }

    private Router parseConf(String file) throws MalformedConfigurationException {
        ConfParser parser = new ConfParser();
		return parser.init(file).getConf();
    }

	private QOS createBuiltInQOS() {
		QOS q = new QOS();
        q.addProp("builtin");
		return q;
	}
	
	@Override
	public NMEARouter getRouter() {
		return router;
	}
}
