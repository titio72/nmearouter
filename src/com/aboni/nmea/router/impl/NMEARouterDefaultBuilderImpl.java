package com.aboni.nmea.router.impl;

import com.aboni.nmea.router.NMEACache;
import com.aboni.nmea.router.NMEAFilterable;
import com.aboni.nmea.router.NMEARouter;
import com.aboni.nmea.router.NMEARouterBuilder;
import com.aboni.nmea.router.agent.NMEAAgent;
import com.aboni.nmea.router.agent.NMEAAgentBuilder;
import com.aboni.nmea.router.agent.QOS;
import com.aboni.nmea.router.agent.impl.*;
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
import com.aboni.utils.ServerLog;
import com.google.inject.Injector;

public class NMEARouterDefaultBuilderImpl implements NMEARouterBuilder {

    private NMEARouter router;
    private final String confFile;
    private final Injector injector;
    private static final boolean ENABLE_GPS_TIME = false;
    
    public NMEARouterDefaultBuilderImpl(Injector injector, String confFile) {
    	this.confFile = confFile;
    	this.injector = injector;
    }
    
    @Override
    public NMEARouterBuilder init() {
    	Router conf;
		try {
			conf = parseConf(confFile);
	        NMEAAgentBuilder builder = injector.getInstance(NMEAAgentBuilder.class);
	        router = buildRouter(conf, builder);
	        return this;
		} catch (MalformedConfigurationException e) {
			e.printStackTrace();
		}
        return null;
    }

    private NMEARouter buildRouter(Router conf, NMEAAgentBuilder builder) {
        NMEARouter r = injector.getInstance(NMEARouter.class);
        
		switch (conf.getLog().getLevel()) {
			case DEBUG: 
	        	ServerLog.getLogger().setDebug(); break;
			case WARNING: 
	        	ServerLog.getLogger().setWarning(); break;
			case ERROR: 
	        	ServerLog.getLogger().setError(); break;
			case NONE: 
	        	ServerLog.getLogger().setNone(); break;
	    	default:
	        	ServerLog.getLogger().setInfo(); break;
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
        buildAutoPilot(r);
        return r;
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
    	boolean active = a.isActive();
    	if (s!=null) {
    		AgentStatus.STATUS requestedStatus = s.getStartMode(agent.getName());  
    		if (requestedStatus==STATUS.UNKNOWN) {
    			s.setStartMode(agent.getName(), active?STATUS.AUTO:STATUS.MANUAL);
    		} else {
    			active = (requestedStatus==STATUS.AUTO);
    		}
    	}
    	return active;
    }
    
    private void buildStreamDump(NMEARouter r) {
        QOS q = createBuiltInQOS();
        NMEA2FileAgent dumper = new NMEA2FileAgent(
        		injector.getInstance(NMEACache.class), 
        		"Log", q);
        r.addAgent(dumper);
	}

	private void buildDPTStats(NMEARouter r) {
        QOS q = createBuiltInQOS();
        DepthStatsAgent a = new DepthStatsAgent(
        		injector.getInstance(NMEACache.class), 
        		"Depth", q);
        r.addAgent(a);
        a.start();
    }

    private void buildPowerLedTarget(NMEARouter r) {
    	if (System.getProperty("os.arch").startsWith("arm")) {
	        QOS q = createBuiltInQOS();
	        PowerLedAgent pwrled = new PowerLedAgent(
	        		injector.getInstance(NMEACache.class), 
	        		"PowerLed", q);
	        r.addAgent(pwrled);
	        pwrled.start();
    	}
    }

    private void buildAutoPilot(NMEARouter r) {
        QOS q = createBuiltInQOS();
        NMEAAutoPilotAgent ap = new NMEAAutoPilotAgent(
        		injector.getInstance(NMEACache.class), 
        		"SmartPilot", q);
        r.addAgent(ap);
        ap.start();
    }

    private void buildFanTarget(NMEARouter r) {
        QOS q = createBuiltInQOS();
        FanAgent fan = new FanAgent(
        		injector.getInstance(NMEACache.class), 
        		"FanManager", q);
        r.addAgent(fan);
        fan.start();
    }

    private void buildGPSTimeTarget(NMEARouter r) {
        QOS q = createBuiltInQOS();
    	NMEASystemTimeGPS gpstime = new NMEASystemTimeGPS(
        		injector.getInstance(NMEACache.class), 
        		"GPSTime", q);
    	r.addAgent(gpstime);
    	gpstime.start();
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

	public NMEARouterBuilder initPlayFile(String playFile) {
        router = injector.getInstance(NMEARouter.class);
        
        NMEAAgent sock = new NMEASocketServer(
        		injector.getInstance(NMEACache.class), 
        		"TCP", 1111, null);
        router.addAgent(sock);
        sock.start();
        
        NMEAPlayer play = new NMEAPlayer(
        		injector.getInstance(NMEACache.class), 
        		"PLAYER", null);
        play.setFile(playFile);
        router.addAgent(play);
        play.start();
        
        return this;
	}
}
