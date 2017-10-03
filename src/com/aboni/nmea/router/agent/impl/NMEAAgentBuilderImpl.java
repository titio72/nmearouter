package com.aboni.nmea.router.agent.impl;

import java.lang.reflect.Method;
import java.util.StringTokenizer;

import javax.inject.Inject;

import com.aboni.nmea.router.NMEACache;
import com.aboni.nmea.router.NMEAFilterable;
import com.aboni.nmea.router.NMEARouter;
import com.aboni.nmea.router.NMEAStream;
import com.aboni.nmea.router.agent.NMEA2JSONSocketTarget;
import com.aboni.nmea.router.agent.NMEAAgent;
import com.aboni.nmea.router.agent.NMEAAgentBuilder;
import com.aboni.nmea.router.agent.NMEAConsoleTarget;
import com.aboni.nmea.router.agent.NMEAGPXPlayerAgent;
import com.aboni.nmea.router.agent.NMEAMWDSentenceCalculator;
import com.aboni.nmea.router.agent.NMEAMeteoTarget;
import com.aboni.nmea.router.agent.NMEASerial;
import com.aboni.nmea.router.agent.NMEASimulatorSource;
import com.aboni.nmea.router.agent.NMEASocketSource;
import com.aboni.nmea.router.agent.NMEASocketTarget;
import com.aboni.nmea.router.agent.NMEASourceSensor;
import com.aboni.nmea.router.agent.NMEATrackAgent;
import com.aboni.nmea.router.agent.NMEAUDPTarget;
import com.aboni.nmea.router.agent.QOS;
import com.aboni.nmea.router.conf.AgentBase;
import com.aboni.nmea.router.conf.ConsoleAgent;
import com.aboni.nmea.router.conf.Filter;
import com.aboni.nmea.router.conf.FilterSet;
import com.aboni.nmea.router.conf.InOut;
import com.aboni.nmea.router.conf.JSONAgent;
import com.aboni.nmea.router.conf.MWDAgent;
import com.aboni.nmea.router.conf.MeteoAgent;
import com.aboni.nmea.router.conf.SensorAgent;
import com.aboni.nmea.router.conf.SerialAgent;
import com.aboni.nmea.router.conf.SimulatorAgent;
import com.aboni.nmea.router.conf.TcpAgent;
import com.aboni.nmea.router.conf.TrackAgent;
import com.aboni.nmea.router.conf.UdpAgent;
import com.aboni.nmea.router.filters.NMEABasicSentenceFilter;
import com.aboni.nmea.router.filters.NMEAFilterSet;
import com.aboni.nmea.router.filters.NMEAFilterSet.TYPE;
import com.aboni.nmea.router.filters.NMEASentenceFilterSet;

import net.sf.marineapi.nmea.sentence.TalkerId;

public class NMEAAgentBuilderImpl implements NMEAAgentBuilder {
    
    private static String getType(AgentBase a) {
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
    
	private NMEAStream stream;
	private NMEACache cache;
	
	@Inject
	public NMEAAgentBuilderImpl(NMEACache cache, NMEAStream stream) {
		this.cache = cache;
		this.stream = stream;
	}
	
	/* (non-Javadoc)
	 * @see com.aboni.nmea.router.agent.NMEAAgentBuilder#createAgent(com.aboni.nmea.router.conf.AgentBase, com.aboni.nmea.router.NMEARouter)
	 */
	@Override
	public NMEAAgent createAgent(AgentBase a, NMEARouter r) {
		NMEAAgent agent = null;
		QOS q = getQos(a.getQos());
		switch (getType(a)) {
			case "Simulator": agent = buildSimulator((SimulatorAgent)a, q); break;
			case "Sensor": agent = buildSensor((SensorAgent)a, q); break;
			case "Serial": agent = buildSerial((SerialAgent)a, q); break;
			case "TCP": agent = buildSocket((TcpAgent)a, q); break;
			case "JSON": agent = buildSocketJSON((JSONAgent)a, q); break;
			case "UDP": agent = buildUDP((UdpAgent)a, q); break;
			case "Console": agent = buildConsoleTarget((ConsoleAgent)a, q); break;
		    case "Track": agent = buildTrackTarget((TrackAgent)a, q); break;
		    case "Meteo": agent = buildMeteoTarget((MeteoAgent)a, q); break;
		    case "MWDSynthetizer": agent = buildMWDSynt((MWDAgent)a, q); break;
		    case "GPXPlayer": agent = buildGPXPlayer((com.aboni.nmea.router.conf.GPXPlayerAgent)a, q); break;
			default: break;
		}
        if (agent!=null) {
        	NMEAFilterable src = agent.getSource();
        	FilterSet srcFilterConf = a.getFilterSource();
        	loadFilters(src, srcFilterConf);

        	NMEAFilterable tgt = agent.getTarget();
        	FilterSet tgtFilterConf = a.getFilterTarget();
        	loadFilters(tgt, tgtFilterConf);
        }
		return agent;
	}

	private void loadFilters(NMEAFilterable agentFilterable, FilterSet filterConf) {
    	if (agentFilterable!=null && filterConf!=null) {
    		if (agentFilterable.getFilter()==null) {
    			NMEAFilterSet ff = new NMEAFilterSet();
    			ff.setType(filterConf.isWhitelist()?TYPE.WHITELIST:TYPE.BLACKLIST);
    			agentFilterable.setFilter(ff);
    		}
    		setFilter(filterConf, agentFilterable.getFilter());
    	}
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
    	NMEAGPXPlayerAgent gpx = null;
		try {
			gpx = new NMEAGPXPlayerAgent(cache, stream, g.getName(), file, q);
		} catch (Exception e) {
			e.printStackTrace();
		}
    	return gpx;
	}
    
	private NMEAAgent buildConsoleTarget(ConsoleAgent c, QOS q) {
    	NMEAAgent console = new NMEAConsoleTarget(cache, stream, c.getName(), q);
    	return console;
	}

	private NMEAAgent buildSerial(SerialAgent s, QOS q) {
		String name = s.getName();
		String portName = s.getDevice();
		int speed = s.getBps();
		boolean t, r;
		switch (s.getInout()) {
		case IN: r = true; t = false; break;
		case OUT: r = false; t = true; break;
		case INOUT: r = true; t = true; break;
		default: r = false; t = false; break;
		}
		
		return new NMEASerial(cache, stream, name, portName, speed, r, t, q);
	}
	
	private NMEAAgent buildUDP(UdpAgent conf, QOS q) {
        NMEAUDPTarget a = new NMEAUDPTarget(cache, stream, conf.getName(), q, 1111);
        for (String s: conf.getTo()) {
        	a.addTarget(s);
        }
        return a;
	}
	
	private NMEAAgent buildMWDSynt(MWDAgent a, QOS q) {
    	NMEAMWDSentenceCalculator c = new NMEAMWDSentenceCalculator(cache, stream, "MWD", q);
    	return c;
	}

	private NMEAAgent buildMeteoTarget(MeteoAgent a, QOS q) {
        NMEAMeteoTarget meteo = new NMEAMeteoTarget(cache, stream, a.getName(), q, new DBMeteoWriter());
        return meteo;
    }

	private NMEAAgent buildSocketJSON(JSONAgent s, QOS q) {
        String name = s.getName();
        int port = s.getPort();
		NMEAAgent sock = null;
		sock = new NMEA2JSONSocketTarget(cache, stream, name, port, q);
        return sock;
	}

	private NMEAAgent buildSocket(TcpAgent s, QOS q) {
        String name = s.getName();
        String server = s.getHost();
        int port = s.getPort();
		NMEAAgent sock = null;
        if (s.getInout().equals(InOut.IN))
			sock = new NMEASocketSource(cache, stream, name, server, port, q);
        else
			sock = new NMEASocketTarget(cache, stream, name, port, q);
        
        return sock;
    }

	private NMEAAgent buildTrackTarget(TrackAgent c, QOS q) {
		NMEATrackAgent track = new NMEATrackAgent(cache, stream, c.getName());
		track.setFile(c.getFile());
	    track.setPeriod(c.getInterval() * 1000);
	    track.setStaticPeriod(c.getIntervalStatic() * 1000);
		track.setListenSentence(c.getSentence());
		return track; 
	}
    
	private NMEAAgent buildSimulator(SimulatorAgent s, QOS q) {
    	NMEAAgent sim = new NMEASimulatorSource(cache, stream, s.getName(), q);
    	return sim;
    }

	private NMEAAgent buildSensor(SensorAgent s, QOS q) {
        NMEAAgent se = new NMEASourceSensor(cache, stream, s.getName(), q);
        return se;
    }
}
