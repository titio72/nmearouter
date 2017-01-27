package com.aboni.nmea.router.agent;

import java.lang.reflect.Method;
import java.util.StringTokenizer;

import com.aboni.nmea.router.NMEARouter;
import com.aboni.nmea.router.NMEASentenceFilterSet;
import com.aboni.nmea.router.conf.AgentBase;
import com.aboni.nmea.router.conf.ConsoleAgent;
import com.aboni.nmea.router.conf.Filter;
import com.aboni.nmea.router.conf.FilterSet;
import com.aboni.nmea.router.conf.InOut;
import com.aboni.nmea.router.conf.MWDAgent;
import com.aboni.nmea.router.conf.MeteoAgent;
import com.aboni.nmea.router.conf.SensorAgent;
import com.aboni.nmea.router.conf.SerialAgent;
import com.aboni.nmea.router.conf.SimulatorAgent;
import com.aboni.nmea.router.conf.TcpAgent;
import com.aboni.nmea.router.conf.TrackAgent;
import com.aboni.nmea.router.conf.UdpAgent;
import com.aboni.nmea.router.filters.NMEABasicSentenceFilter;

import net.sf.marineapi.nmea.sentence.TalkerId;

public class NMEAAgentBuilder {
 
    
    private String getType(AgentBase a) {
    	Method m;
		try {
			m = a.getClass().getMethod("getType");
	    	return (String)m.invoke(a);
		} catch (Exception e) {
			return "";
		}
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
    
	public NMEAAgent createAgent(AgentBase a, NMEARouter r) {
		NMEAAgent agent = null;
		QOS q = getQos(a.getQos());
		switch (getType(a)) {
			case "Simulator": agent = buildSimulator((SimulatorAgent)a, q); break;
			case "Sensor": agent = buildSensor((SensorAgent)a, q); break;
			case "Serial": agent = buildSerial((SerialAgent)a, q); break;
			case "TCP": agent = buildSocket((TcpAgent)a, q); break;
			case "UDP": agent = buildUDP((UdpAgent)a, q); break;
			case "Console": agent = buildConsoleTarget((ConsoleAgent)a, q); break;
		    case "Track": agent = buildTrackTarget((TrackAgent)a, q); break;
		    case "Meteo": agent = buildMeteoTarget((MeteoAgent)a, q); break;
		    case "MWDSynthetizer": agent = buildMWDSynt((MWDAgent)a, q); break;
		    case "GPXPlayer": agent = buildGPXPlayer((com.aboni.nmea.router.conf.GPXPlayerAgent)a, q); break;
			default: break;
		}
        if (agent!=null) {
        	if (agent.getSource()!=null) setFilter(a.getFilterSource(), agent.getSource().getSourceFilter());
        	if (agent.getTarget()!=null) setFilter(a.getFilterTarget(), agent.getTarget().getTargetFilter());
        }
		return agent;
	}

	private static void setFilter(FilterSet conf, NMEASentenceFilterSet dest) {
    	if (conf!=null && dest!=null) {
    		for (Filter fConf: conf.getFilter()) {
    			NMEABasicSentenceFilter sF = new NMEABasicSentenceFilter(
    					"*".equals(fConf.getSentence())?"":fConf.getSentence(),
    					"*".equals(fConf.getTalker())?null:TalkerId.parse(fConf.getTalker()),
    					"*".equals(fConf.getSource())?"":fConf.getSource()
    					);
    			dest.addFilter(sF);
    		}
    	}
    }
	
    private NMEAAgent buildGPXPlayer(com.aboni.nmea.router.conf.GPXPlayerAgent g, QOS q) {
    	String file = g.getGpxFile();
    	GPXPlayerAgent gpx = null;
		try {
			gpx = new GPXPlayerAgent(g.getName(), file, q);
		} catch (Exception e) {
			e.printStackTrace();
		}
    	return gpx;
	}
    
	private NMEAAgent buildConsoleTarget(ConsoleAgent c, QOS q) {
    	NMEAAgent console = new NMEAConsoleTarget(c.getName(), q);
    	return console;
	}

	private NMEAAgent buildSerial(SerialAgent s, QOS q) {
		String name = s.getName();
		String portName = s.getDevice();
		int speed = s.getBps();
		NMEAAgent ser = null;
		if (s.getInout().equals(InOut.IN))
			ser = new NMEASerialSourceJSSC(name, portName, speed, q);
		else 
			ser = new NMEASerialTargetJSSC(name, portName, speed, q);

		return ser;
	}
	
	private NMEAAgent buildUDP(UdpAgent conf, QOS q) {
        NMEAUDPTarget a = new NMEAUDPTarget(conf.getName(), q, 1111);
        for (String s: conf.getTo()) {
        	a.addTarget(s);
        }
        return a;
	}
	
	private NMEAAgent buildMWDSynt(MWDAgent a, QOS q) {
    	NMEAMWDSentenceCalculator c = new NMEAMWDSentenceCalculator("MWD", q);
    	return c;
	}

	private NMEAAgent buildMeteoTarget(MeteoAgent a, QOS q) {
        NMEAMeteoTarget meteo = new NMEAMeteoTarget(a.getName(), q);
        return meteo;
    }
	

	private NMEAAgent buildSocket(TcpAgent s, QOS q) {
        String name = s.getName();
        String server = s.getHost();
        int port = s.getPort();
		NMEAAgent sock = null;
        if (s.getInout().equals(InOut.IN))
			sock = new NMEASocketSource(name, server, port, q);
        else
			sock = new NMEASocketTarget(name, port, q);
        
        return sock;
    }

	private NMEAAgent buildTrackTarget(TrackAgent c, QOS q) {
		NMEATrackTargetDB track = new NMEATrackTargetDB(c.getName());
		track.setFile(c.getFile());
	    track.setPeriod(c.getInterval() * 1000);
	    track.setStaticPeriod(c.getIntervalStatic() * 1000);
		track.setListenSentence(c.getSentence());
		return track; 
	}
    
	
	private NMEAAgent buildSimulator(SimulatorAgent s, QOS q) {
    	NMEAAgent sim = new NMEASimulatorSource(s.getName(), q);
    	return sim;
    }

	private NMEAAgent buildSensor(SensorAgent s, QOS q) {
        NMEAAgent se = new NMEASourceSensor(s.getName(), q);
        return se;
    }
}
