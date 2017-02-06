package com.aboni.nmea.router;

import com.aboni.nmea.router.agent.DepthStatsAgent;
import com.aboni.nmea.router.agent.FanAgent;
import com.aboni.nmea.router.agent.NMEA2FileAgent;
import com.aboni.nmea.router.agent.NMEAAgent;
import com.aboni.nmea.router.agent.NMEAAgentBuilder;
import com.aboni.nmea.router.agent.NMEAPlayer;
import com.aboni.nmea.router.agent.NMEASocketTarget;
import com.aboni.nmea.router.agent.NMEASystemTimeGPS;
import com.aboni.nmea.router.agent.PowerTarget;
import com.aboni.nmea.router.agent.QOS;
import com.aboni.nmea.router.conf.AgentBase;
import com.aboni.nmea.router.conf.ConfParser;
import com.aboni.nmea.router.conf.MalformedConfigurationException;
import com.aboni.nmea.router.conf.Router;
import com.aboni.nmea.router.impl.NMEARouterImpl;

public class NMEARouterDefaultBuilderImpl implements NMEARouterBuilder {

    private NMEARouter router;
    private String confFile;
    
    public NMEARouterDefaultBuilderImpl(String confFile) {
    	this.confFile = confFile;
    }
    
    @Override
    public NMEARouterBuilder init() {
    	Router conf;
		try {
			conf = parseConf(confFile);
	        NMEAAgentBuilder builder = new NMEAAgentBuilder();
	        router = buildRouter(conf, builder);
	        return this;
		} catch (MalformedConfigurationException e) {
			e.printStackTrace();
		}
        return null;
    }

    private NMEARouter buildRouter(Router conf, NMEAAgentBuilder builder) {
        NMEARouterImpl r = new NMEARouterImpl();
        
        for (AgentBase a: conf.getSerialAgentOrTcpAgentOrUdpAgent()) {
        	NMEAAgent agent = builder.createAgent(a, r);
            if (agent!=null) {
            	r.addAgent(agent);
            	if (a.isActive()) agent.start();
            }
        }
        
        buildStreamDump(conf, r);
        buildGPSTimeTarget(conf, r);
        buildPowerLedTarget(conf, r);
        buildFanTarget(conf, r);
        buildDPTStats(conf, r);
        return r;
    }
    
    private void buildStreamDump(Router conf2, NMEARouterImpl r) {
        QOS q = createBuiltInQOS();
        NMEA2FileAgent dumper = new NMEA2FileAgent("nmea", q);
        r.addAgent(dumper);
	}

	private void buildDPTStats(Router conf2, NMEARouterImpl r) {
        QOS q = createBuiltInQOS();
        DepthStatsAgent a = new DepthStatsAgent("DEPTH", q);
        r.addAgent(a);
        a.start();
    }

    private void buildPowerLedTarget(Router conf2, NMEARouterImpl r) {
    	if (System.getProperty("os.arch").startsWith("arm")) {
	        QOS q = createBuiltInQOS();
	        PowerTarget pwrled = new PowerTarget("PWRLED", q);
	        r.addAgent(pwrled);
	        pwrled.start();
    	}
    }

    private void buildFanTarget(Router conf2, NMEARouterImpl r) {
        QOS q = createBuiltInQOS();
        FanAgent fan = new FanAgent("FAN", q);
        r.addAgent(fan);
        fan.start();
    }

    private void buildGPSTimeTarget(Router conf2, NMEARouter r) {
        QOS q = createBuiltInQOS();
    	NMEASystemTimeGPS gpstime = new NMEASystemTimeGPS("gpstime", q);
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
        router = new NMEARouterImpl();
        
        NMEAAgent sock = new NMEASocketTarget("TCP", 1111, null);
        router.addAgent(sock);
        sock.start();
        
        NMEAPlayer play = new NMEAPlayer("PLAYER", null);
        play.setFile(playFile);
        router.addAgent(play);
        play.start();
        
        return this;
	}
}
