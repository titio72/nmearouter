package com.aboni.nmea.router.impl;

import com.aboni.nmea.router.NMEACache;
import com.aboni.nmea.router.NMEARouter;
import com.aboni.nmea.router.NMEARouterBuilder;
import com.aboni.nmea.router.NMEAStream;
import com.aboni.nmea.router.agent.DepthStatsAgent;
import com.aboni.nmea.router.agent.FanAgent;
import com.aboni.nmea.router.agent.NMEA2FileAgent;
import com.aboni.nmea.router.agent.NMEAAgent;
import com.aboni.nmea.router.agent.NMEAAgentBuilder;
import com.aboni.nmea.router.agent.NMEAPlayer;
import com.aboni.nmea.router.agent.NMEASocketTarget;
import com.aboni.nmea.router.agent.NMEASystemTimeGPS;
import com.aboni.nmea.router.agent.PowerLedAgent;
import com.aboni.nmea.router.agent.QOS;
import com.aboni.nmea.router.conf.AgentBase;
import com.aboni.nmea.router.conf.ConfParser;
import com.aboni.nmea.router.conf.MalformedConfigurationException;
import com.aboni.nmea.router.conf.Router;
import com.aboni.nmea.router.conf.db.AgentStatus;
import com.aboni.nmea.router.conf.db.AgentStatus.STATUS;
import com.aboni.nmea.router.conf.db.AgentStatusProvider;
import com.google.inject.Injector;

public class NMEARouterDefaultBuilderImpl implements NMEARouterBuilder {

    private NMEARouter router;
    private String confFile;
    private Injector injector;
    
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
        
        for (AgentBase a: conf.getSerialAgentOrJSONAgentOrTcpAgent()) {
        	NMEAAgent agent = builder.createAgent(a, r);
            if (agent!=null) {
            	r.addAgent(agent);
            	handleActivation(agent, a);
            }
        }
        
        buildStreamDump(conf, r);
        buildGPSTimeTarget(conf, r);
        buildPowerLedTarget(conf, r);
        buildFanTarget(conf, r);
        buildDPTStats(conf, r);
        return r;
    }
    
    private void handleActivation(NMEAAgent agent, AgentBase a) {
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
    	if (active) agent.start();
    }
    
    private void buildStreamDump(Router conf2, NMEARouter r) {
        QOS q = createBuiltInQOS();
        NMEA2FileAgent dumper = new NMEA2FileAgent(
        		injector.getInstance(NMEACache.class), 
        		injector.getInstance(NMEAStream.class), 
        		"nmea", q);
        r.addAgent(dumper);
	}

	private void buildDPTStats(Router conf2, NMEARouter r) {
        QOS q = createBuiltInQOS();
        DepthStatsAgent a = new DepthStatsAgent(
        		injector.getInstance(NMEACache.class), 
        		injector.getInstance(NMEAStream.class), 
        		"DEPTH", q);
        r.addAgent(a);
        a.start();
    }

    private void buildPowerLedTarget(Router conf2, NMEARouter r) {
    	if (System.getProperty("os.arch").startsWith("arm")) {
	        QOS q = createBuiltInQOS();
	        PowerLedAgent pwrled = new PowerLedAgent(
	        		injector.getInstance(NMEACache.class), 
	        		injector.getInstance(NMEAStream.class), 
	        		"PWRLED", q);
	        r.addAgent(pwrled);
	        pwrled.start();
    	}
    }

    private void buildFanTarget(Router conf2, NMEARouter r) {
        QOS q = createBuiltInQOS();
        FanAgent fan = new FanAgent(
        		injector.getInstance(NMEACache.class), 
        		injector.getInstance(NMEAStream.class), 
        		"FAN", q);
        r.addAgent(fan);
        fan.start();
    }

    private void buildGPSTimeTarget(Router conf2, NMEARouter r) {
        QOS q = createBuiltInQOS();
    	NMEASystemTimeGPS gpstime = new NMEASystemTimeGPS(
        		injector.getInstance(NMEACache.class), 
        		injector.getInstance(NMEAStream.class), 
        		"gpstime", q);
    	r.addAgent(gpstime);
    	gpstime.start();
	}

    private Router parseConf(String file) throws MalformedConfigurationException {
        ConfParser parser = new ConfParser();
        Router conf = parser.init(file).getConf();
        return conf;
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
        
        NMEAAgent sock = new NMEASocketTarget(
        		injector.getInstance(NMEACache.class), 
        		injector.getInstance(NMEAStream.class), 
        		"TCP", 1111, null);
        router.addAgent(sock);
        sock.start();
        
        NMEAPlayer play = new NMEAPlayer(
        		injector.getInstance(NMEACache.class), 
        		injector.getInstance(NMEAStream.class), 
        		"PLAYER", null);
        play.setFile(playFile);
        router.addAgent(play);
        play.start();
        
        return this;
	}
}
