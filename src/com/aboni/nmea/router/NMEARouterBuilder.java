package com.aboni.nmea.router;

import java.lang.reflect.Method;
import java.util.StringTokenizer;

import com.aboni.nmea.router.agent.DepthStatsAgent;
import com.aboni.nmea.router.agent.NMEA2JSONSocketTarget;
import com.aboni.nmea.router.agent.NMEAAgent;
import com.aboni.nmea.router.agent.NMEAConsoleTarget;
import com.aboni.nmea.router.agent.NMEAMWDSentenceCalculator;
import com.aboni.nmea.router.agent.NMEAMeteoTarget;
import com.aboni.nmea.router.agent.NMEAPlayer;
import com.aboni.nmea.router.agent.NMEASerialSourceJSSC;
import com.aboni.nmea.router.agent.NMEASerialTargetJSSC;
import com.aboni.nmea.router.agent.NMEASimulatorSource;
import com.aboni.nmea.router.agent.NMEASocketSource;
import com.aboni.nmea.router.agent.NMEASocketTarget;
import com.aboni.nmea.router.agent.NMEASourceSensor;
import com.aboni.nmea.router.agent.NMEASystemTimeGPS;
import com.aboni.nmea.router.agent.NMEATrackTargetDB;
import com.aboni.nmea.router.agent.NMEAUDPTarget;
import com.aboni.nmea.router.agent.PowerTarget;
import com.aboni.nmea.router.agent.QOS;
import com.aboni.nmea.router.conf.AgentBase;
import com.aboni.nmea.router.conf.ConfParser;
import com.aboni.nmea.router.conf.ConsoleAgent;
import com.aboni.nmea.router.conf.InOut;
import com.aboni.nmea.router.conf.JSONAgent;
import com.aboni.nmea.router.conf.MWDAgent;
import com.aboni.nmea.router.conf.MalformedConfigurationException;
import com.aboni.nmea.router.conf.MeteoAgent;
import com.aboni.nmea.router.conf.Router;
import com.aboni.nmea.router.conf.SensorAgent;
import com.aboni.nmea.router.conf.SerialAgent;
import com.aboni.nmea.router.conf.SimulatorAgent;
import com.aboni.nmea.router.conf.TcpAgent;
import com.aboni.nmea.router.conf.TrackAgent;
import com.aboni.nmea.router.conf.UdpAgent;
import com.aboni.nmea.router.impl.NMEARouterImpl;

public class NMEARouterBuilder {

    private NMEARouter router;
    private Router conf;
    
    public NMEARouterBuilder() {}
    
    public NMEARouterBuilder init(String file) throws MalformedConfigurationException {
        conf = getConf(file);
        router = buildRouter(conf);
        return this;
    }
    
    private String getType(AgentBase a) {
    	Method m;
		try {
			m = a.getClass().getMethod("getType");
	    	return (String)m.invoke(a);
		} catch (Exception e) {
			return "";
		}
    }
    
    private NMEARouter buildRouter(Router conf) {
        NMEARouterImpl r = new NMEARouterImpl();
        
        for (AgentBase a: conf.getSerialAgentOrTcpAgentOrUdpAgent()) {
        	switch (getType(a)) {
	        	case "Simulator": buildSimulator((SimulatorAgent)a, r); break;
	        	case "Sensor": buildSensor((SensorAgent)a, r); break;
	        	case "Serial": buildSerial((SerialAgent)a, r); break;
	        	case "TCP": buildSocket((TcpAgent)a, r); break;
	        	case "UDP": buildUDP((UdpAgent)a, r); break;
	        	case "Console": buildConsoleTarget((ConsoleAgent)a, r); break;
                case "Track": buildTrackTarget((TrackAgent)a, r); break;
                case "Meteo": buildMeteoTarget((MeteoAgent)a, r); break;
                case "JSON": buildJSONTarget((JSONAgent)a, r); break;
                case "MWDSynthetizer": buildMWDSynt((MWDAgent)a, r); break;
        		default: break;
        	}
        }
        
        buildGPSTimeTarget(conf, r);
        buildPowerLedTarget(conf, r);
        buildDPTStats(conf, r);
        return r;
    }

    private void buildUDP(UdpAgent conf, NMEARouterImpl r) {
        NMEAUDPTarget a = new NMEAUDPTarget(conf.getName(), 1111);
        for (String s: conf.getTo()) {
        	a.addTarget(s);
        }
        r.addAgent(a);
        a.start();
	}

	private void buildDPTStats(Router conf2, NMEARouterImpl r) {
        QOS q = new QOS();
        q.addProp("builtin");
        DepthStatsAgent a = new DepthStatsAgent("DEPTH", q);
        r.addAgent(a);
        a.start();
    }

    private void buildMWDSynt(MWDAgent a, NMEARouterImpl r) {
    	NMEAMWDSentenceCalculator c = new NMEAMWDSentenceCalculator("MWD", getQos(a.getQos()));
    	r.addAgent(c);
    	c.start();
	}

	private void buildJSONTarget(JSONAgent a, NMEARouterImpl r) {
        NMEA2JSONSocketTarget json = new NMEA2JSONSocketTarget(a.getName(), a.getPort());
        r.addAgent(json); 
        if (a.isActive()) json.start();
	}

	private void buildMeteoTarget(MeteoAgent a, NMEARouterImpl r) {
        NMEAMeteoTarget meteo = new NMEAMeteoTarget(a.getName(), null);
        r.addAgent(meteo); 
        if (a.isActive()) meteo.start();
    }

    private void buildPowerLedTarget(Router conf2, NMEARouterImpl r) {
    	if (System.getProperty("os.arch").startsWith("arm")) {
	        QOS q = new QOS();
	        q.addProp("builtin");
	        PowerTarget pwrled = new PowerTarget("PWRLED", q);
	        r.addAgent(pwrled);
	        pwrled.start();
    	}
    }

    private void buildGPSTimeTarget(Router conf2, NMEARouter r) {
        QOS q = new QOS();
        q.addProp("builtin");
    	NMEASystemTimeGPS gpstime = new NMEASystemTimeGPS("gpstime", q);
    	r.addAgent(gpstime);
    	gpstime.start();
	}

	private void buildSocket(TcpAgent s, NMEARouter r) {
        String name = s.getName();
        String server = s.getHost();
        int port = s.getPort();
		NMEAAgent sock = null;
        if (s.getInout().equals(InOut.IN))
			sock = new NMEASocketSource(name, server, port, getQos(s.getQos()));
        else
			sock = new NMEASocketTarget(name, port);
        r.addAgent(sock);
        if (s.isActive()) sock.start();
    }

    private void buildTrackTarget(TrackAgent c, NMEARouter r) {
		NMEATrackTargetDB track = new NMEATrackTargetDB(c.getName());
		track.setFile(c.getFile());
	    track.setPeriod(c.getInterval() * 1000);
	    track.setStaticPeriod(c.getIntervalStatic() * 1000);
		track.setListenSentence(c.getSentence());
		r.addAgent(track); 
		if (c.isActive()) track.start();
	}

	public Router getConfiguration() {
    	return conf;
    }
    
	private void buildConsoleTarget(ConsoleAgent c, NMEARouter r) {
    	NMEAAgent console = new NMEAConsoleTarget(c.getName());
    	r.addAgent(console);
    	if (c.isActive()) console.start();
	}

	private void buildSerial(SerialAgent s, NMEARouter r) {
		String name = s.getName();
		String portName = s.getDevice();
		int speed = s.getBps();
		NMEAAgent ser = null;
		if (s.getInout().equals(InOut.IN))
			ser = new NMEASerialSourceJSSC(name, portName, speed, getQos(s.getQos()));
		else 
			ser = new NMEASerialTargetJSSC(name, portName, speed);
        r.addAgent(ser);
        if (s.isActive()) ser.start();
	}
	
	private static QOS getQos(String par) {
		QOS q = new QOS();
		if (par!=null) {
			StringTokenizer t = new StringTokenizer(par, ";");
			while (t.hasMoreTokens()) {
				StringTokenizer t1 = new StringTokenizer(t.nextToken(), "=");
				if (t1.countTokens()==1) {
					String token = t1.nextToken();
					q.addProp(token);
				} else {
					q.addProp(t1.nextToken(), t1.nextToken());
				}
			}
		}
		return q;
	}
	
    private void buildSimulator(SimulatorAgent s, NMEARouter r) {
    	NMEAAgent sim = new NMEASimulatorSource(s.getName(), getQos(s.getQos()));
        r.addAgent(sim);
        if (s.isActive()) sim.start();
    }

    private void buildSensor(SensorAgent s, NMEARouter r) {
        NMEAAgent se = new NMEASourceSensor(s.getName());
        r.addAgent(se);
        if (s.isActive()) se.start();
    }

    private Router getConf(String file) throws MalformedConfigurationException {
        ConfParser parser = new ConfParser();
        Router conf = parser.init(file).getConf();
        return conf;
    }

	public NMEARouter getRouter() {
		return router;
	}

	public NMEARouterBuilder initPlayFile(String playFile) {
        router = new NMEARouterImpl();
        
        NMEAAgent sock = new NMEASocketTarget("TCP", 1111);
        router.addAgent(sock);
        sock.start();
        
        NMEAPlayer play = new NMEAPlayer("PLAYER", null);
        play.setFile(playFile);
        router.addAgent(play);
        play.start();
        
        
        return this;
	}
}
