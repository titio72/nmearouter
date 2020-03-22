package com.aboni.nmea.router.agent.impl;

import com.aboni.nmea.router.NMEACache;
import com.aboni.nmea.router.NMEAFilterable;
import com.aboni.nmea.router.agent.NMEAAgent;
import com.aboni.nmea.router.agent.NMEAAgentBuilder;
import com.aboni.nmea.router.agent.QOS;
import com.aboni.nmea.router.agent.impl.simulator.NMEASimulatorSource;
import com.aboni.nmea.router.conf.*;
import com.aboni.nmea.router.conf.net.NetConf;
import com.aboni.nmea.router.filters.NMEABasicSentenceFilter;
import com.aboni.nmea.router.filters.NMEAFilterSet;
import com.aboni.nmea.router.filters.NMEAFilterSet.TYPE;
import com.aboni.nmea.router.filters.NMEASentenceFilterSet;
import com.aboni.utils.ServerLog;
import com.aboni.utils.ThingsFactory;
import net.sf.marineapi.nmea.sentence.TalkerId;

import javax.inject.Inject;
import java.lang.reflect.Method;
import java.util.StringTokenizer;

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
    
	private final NMEACache cache;
	
	@Inject
	public NMEAAgentBuilderImpl(NMEACache cache) {
		this.cache = cache;
	}
	
	@Override
	public NMEAAgent createAgent(AgentBase a) {
		NMEAAgent agent = null;
		QOS q = getQos(a.getQos());
		switch (getType(a)) {
            case "Simulator":
                agent = buildSimulator((SimulatorAgent) a, q);
                break;
            case "Sensor":
                agent = buildSensor((SensorAgent) a, q);
                break;
            case "Gyro":
                agent = buildGyro((GyroAgent) a, q);
                break;
            case "Serial":
                agent = buildSerial((SerialAgent) a, q);
                break;
            case "TCP":
                agent = buildSocket((TcpAgent) a, q);
                break;
            case "JSON":
                agent = buildSocketJSON((JSONAgent) a, q);
                break;
            case "UDP":
                agent = buildUDP((UdpAgent) a, q);
                break;
            case "Console":
                agent = buildConsoleTarget((ConsoleAgent) a, q);
                break;
            case "Track":
                agent = buildTrackTarget((TrackAgent) a);
                break;
            case "Meteo":
                agent = buildMeteoTarget((MeteoAgent) a, q);
                break;
            case "MWDSynthetizer":
                agent = buildMWDSyntesizer(q);
                break;
            case "GPXPlayer":
                agent = buildGPXPlayer((com.aboni.nmea.router.conf.GPXPlayerAgent) a, q);
                break;
            default:
                break;
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
                NMEAFilterSet ff = new NMEAFilterSet(filterConf.isWhitelist() ? TYPE.WHITELIST : TYPE.BLACKLIST);
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
            gpx = ThingsFactory.getInstance(NMEAGPXPlayerAgent.class);
            gpx.setup(g.getName(), q);
            gpx.setFile(file);
        } catch (Exception e) {
			ServerLog.getLogger().error("Cannot create GPX reader", e);
		}
    	return gpx;
	}
    
	private NMEAAgent buildConsoleTarget(ConsoleAgent c, QOS q) {
        NMEAAgent a = ThingsFactory.getInstance(NMEAConsoleTarget.class);
        a.setup(c.getName(), q);
        return a;
    }

	private NMEAAgent buildSerial(SerialAgent s, QOS q) {
		String name = s.getName();
		String portName = s.getDevice();
		int speed = s.getBps();
		boolean t;
		boolean r;
		switch (s.getInout()) {
		case IN: r = true; t = false; break;
		case OUT: r = false; t = true; break;
		case INOUT: r = true; t = true; break;
		default: r = false; t = false; break;
		}
		
        return new NMEASerial(cache, name, portName, speed, r, t, q);
	}
	
	private NMEAAgent buildUDP(UdpAgent conf, QOS q) {
		if (conf.getInout()==InOut.OUT) {
            NMEAUDPSender a = ThingsFactory.getInstance(NMEAUDPSender.class);
            a.setup(conf.getName(), q, conf.getPort());
            for (String s : conf.getTo()) {
                a.addTarget(s);
            }
            return a;
        } else {
            NMEAUDPReceiver a = ThingsFactory.getInstance(NMEAUDPReceiver.class);
            a.setup(conf.getName(), q, conf.getPort());
            return a;
        }
    }

    private NMEAAgent buildMWDSyntesizer(QOS q) {
        NMEAAgent mwd = ThingsFactory.getInstance(NMEAMWDSentenceCalculator.class);
        mwd.setup("MWD", q);
        return mwd;
    }

    private NMEAAgent buildMeteoTarget(MeteoAgent a, QOS q) {
        NMEAAgent meteo = ThingsFactory.getInstance(NMEAMeteoTarget.class);
        meteo.setup(a.getName(), q);
        return meteo;
    }

	private NMEAAgent buildSocketJSON(JSONAgent s, QOS q) {
        String name = s.getName();
        int port = s.getPort();
        NMEASocketServerJSON c = ThingsFactory.getInstance(NMEASocketServerJSON.class);
        c.setup(name, q, new NetConf(null, port, false, true));
        return c;
    }

	private NMEAAgent buildSocket(TcpAgent s, QOS q) {
	    if (s.getHost()==null || s.getHost().isEmpty()) {
	        return buildServerSocket(s, q);
	    } else {
	        return buildClientSocket(s, q);
	    }
	}
	
    private NMEAAgent buildClientSocket(TcpAgent s, QOS q) {
        String name = s.getName();
        String server = s.getHost();
        int port = s.getPort();
        boolean t;
        boolean r;
        switch (s.getInout()) {
            case IN:
                r = true;
                t = false;
                break;
            case OUT:
                r = false;
                t = true;
                break;
            case INOUT:
                r = true;
                t = true;
                break;
            default:
                r = false;
                t = false;
                break;
        }
        NMEASocketClient c = ThingsFactory.getInstance(NMEASocketClient.class);
        c.setup(name, q, new NetConf(server, port, r, t));
        return c;
    }

    private NMEAAgent buildServerSocket(TcpAgent s, QOS q) {
        String name = s.getName();
        int port = s.getPort();
        boolean t;
        boolean r;
        switch (s.getInout()) {
            case IN:
                r = true;
                t = false;
                break;
            case OUT:
                r = false;
                t = true;
                break;
            case INOUT:
                r = true;
                t = true;
                break;
            default:
                r = false;
                t = false;
                break;
        }
        NMEASocketServer c = ThingsFactory.getInstance(NMEASocketServer.class);
        c.setup(name, q, new NetConf(null, port, r, t));
        return c;
    }

	private NMEAAgent buildTrackTarget(TrackAgent c) {
        NMEATrackAgent track = ThingsFactory.getInstance(NMEATrackAgent.class);
        track.setup(c.getName(), null);
        track.setPeriod(c.getInterval() * 1000L);
        track.setStaticPeriod(c.getIntervalStatic() * 1000L);
        return track;
    }
    
	private NMEAAgent buildSimulator(SimulatorAgent s, QOS q) {
        NMEAAgent a = ThingsFactory.getInstance(NMEASimulatorSource.class);
        a.setup(s.getName(), q);
        return a;
    }

    private NMEAAgent buildSensor(SensorAgent s, QOS q) {
        NMEAAgent a = ThingsFactory.getInstance(NMEASourceSensor.class);
        a.setup(s.getName(), q);
        return a;
    }

    private NMEAAgent buildGyro(GyroAgent s, QOS q) {
        NMEAAgent a = ThingsFactory.getInstance(NMEASourceGyro.class);
        a.setup(s.getName(), q);
        return a;
    }

}
