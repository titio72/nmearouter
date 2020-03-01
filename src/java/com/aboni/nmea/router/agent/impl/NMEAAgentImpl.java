package com.aboni.nmea.router.agent.impl;

import com.aboni.nmea.router.NMEACache;
import com.aboni.nmea.router.NMEASentenceListener;
import com.aboni.nmea.router.agent.*;
import com.aboni.nmea.router.filters.NMEASentenceFilterSet;
import com.aboni.nmea.router.filters.NMEASpeedFilter;
import com.aboni.nmea.router.impl.RouterMessageImpl;
import com.aboni.nmea.router.processors.*;
import com.aboni.utils.Log;
import com.aboni.utils.ServerLog;
import net.sf.marineapi.nmea.sentence.Sentence;
import org.json.JSONObject;

import java.util.List;

public abstract class NMEAAgentImpl implements NMEAAgent {

	private class InternalSource implements NMEASource {

		@Override
		public NMEASentenceFilterSet getFilter() {
		    return fsetOutput;
		}

		@Override
		public void setFilter(NMEASentenceFilterSet s) {
            fsetOutput = s;
        }

		@Override
		public void setSentenceListener(NMEASentenceListener listener) {
            NMEAAgentImpl.this.listener = listener;
		}

	}
	
	private class InternalTarget implements NMEATarget {

		@Override
		public NMEASentenceFilterSet getFilter() {
		    return fsetInput;
		}

		@Override
		public void setFilter(NMEASentenceFilterSet s) {
            fsetInput = s;
		}

		@Override
		public void pushSentence(Sentence e, String src) {
			try {
				if (isStarted() &&
						(getFilter()==null || getFilter().match(e,  src))) {
					doWithSentence(e, src);
				}
			} catch (Exception t) {
				getLogger().warning("Error delivering sentence to agent {" + e + "} error {" + t.getMessage() + "}");
			}
		}
	}
	
	private final String name;
	private NMEAAgentStatusListener sl;
    private NMEASentenceFilterSet fsetInput;
    private NMEASentenceFilterSet fsetOutput;
    private boolean active;
    private NMEASentenceListener listener;
    private boolean builtin;
    private boolean target;
    private boolean source;
    private final InternalTarget targetIf;
    private final InternalSource sourceIf;
    private final NMEAProcessorSet procs;
    private final NMEACache cache;

    public NMEAAgentImpl(NMEACache cache, String name, QOS qos) {
        this.cache = cache;
        targetIf = new InternalTarget();
        sourceIf = new InternalSource();
        this.name = name;
        fsetInput = null;
        fsetOutput = null;
        active = false;
        procs = new NMEAProcessorSet();
        handleQos(cache, name, qos);
        target = true;
        source = true;
    }

    private void handleQos(NMEACache cache, String name, QOS qos) {
        if (qos!=null) {
        	for (String q: qos.getKeys()) {
				switch (q) {
					case "speed_filter":
						getLogger().info("QoS {SPEED_FILTER} Agent {" + name + "}");
						addProc(new NMEAGenericFilterProc(new NMEASpeedFilter(cache)));
						break;
                    case "dpt":
                        getLogger().info("QoS {DPT} Agent {" + name + "}");
                        addProc(new NMEADepthEnricher());
                        break;
                    case "rmc2vtg":
                        getLogger().info("QoS {RMC2VTG} Agent {" + name + "}");
                        addProc(new NMEARMC2VTGProcessor());
                        break;
                    case "truewind_sog":
                        getLogger().info("QoS {TRUEWIND_SOG} Agent {" + name + "}");
                        addProc(new NMEAMWVTrue(cache, true));
                        break;
                    case "truewind":
                        getLogger().info("QoS {TRUEWIND} Agent {" + name + "}");
                        addProc(new NMEAMWVTrue(cache, false));
                        break;
                    case "enrich_hdg":
                        getLogger().info("QoS {ENRICH_HDG} Agent {" + name + "}");
                        addProc(new NMEAHDGEnricher(cache));
                        break;
                    case "enrich_hdm":
                        getLogger().info("QoS {ENRICH_HDM} Agent {" + name + "}");
                        addProc(new NMEAHDMEnricher(cache));
                        break;
                    case "rmc_filter":
						getLogger().info("QoS {RMC filter} Agent {" + name + "}");
						addProc(new NMEARMCFilter());
						break;
					case "builtin":
						getLogger().info("QoS {BuiltIn} Agent {" + name + "}");
						builtin = true;
						break;
					default:
						break;
				}
			}

        }
    }
    
	public NMEAAgentImpl(NMEACache cache, String name) {
		this(cache, name, null);
	}

	protected void setSourceTarget(boolean isSource, boolean isTarget) {
	    target = isTarget;
	    source = isSource;
	}
	
	protected class PrivateLog implements Log {

	    private final Log log;
	    
	    private PrivateLog() {
	        log = ServerLog.getLogger();
	    }

	    private String getMsg(NMEAAgentImpl a, String msg) {
	        return String.format("Agent {%s} Name {%s} %s", a.toString(), a.getName(), msg);
        }

        @Override
        public void error(String msg) {
            log.error(getMsg(NMEAAgentImpl.this, msg));
        }

		@Override
		public void error(String msg, Throwable t) {
			log.error(getMsg(NMEAAgentImpl.this, msg), t);
		}

		@Override
		public void errorForceStacktrace(String msg, Throwable t) {
			log.errorForceStacktrace(getMsg(NMEAAgentImpl.this, msg), t);
		}

		@Override
        public void warning(String msg) {
            log.warning(getMsg(NMEAAgentImpl.this, msg));
        }

        @Override
        public void info(String msg) {
            log.info(getMsg(NMEAAgentImpl.this, msg));
        }

        @Override
        public void debug(String msg) {
            log.debug(getMsg(NMEAAgentImpl.this, msg));
        }

        @Override
		public void console(String msg) { throw new UnsupportedOperationException(); }
	}
	
	private Log internalLog;
	
	protected Log getLogger() {
	    if (internalLog ==null) internalLog = new PrivateLog();
		return internalLog;
	}

	@Override
	public boolean isBuiltIn() {
	    return builtin;
	}
	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public boolean isStarted() {
		synchronized (this) {
			return active;
		}
	}

	@Override
	public void start() {
		synchronized (this) {
			if (!active) {
				getLogger().info("Activating agent {" + getName() + "}");
				if (onActivate()) {
					active = true;
					notifyStatus();
				} else {
					getLogger().error("Cannot activate agent {" + getName() + "}");
				}
			}
		}	
	}
	
	private void notifyStatus() {
		if (sl!=null) sl.onStatusChange(this);
	}

	@Override
	public void stop() {
		synchronized (this) {
			if (active) {
				getLogger().info("Deactivating {" + getName() + "}");
				onDeactivate();
				active = false;
				notifyStatus();
			}
		}
	}

    @Override
    public void setStatusListener(NMEAAgentStatusListener listener) {
        sl = listener;
    }

	@Override
	public String toString() {
		return "{" + getType() + "}";
	}
	
	protected boolean onActivate() {
		return true;
	}
	
	protected void onDeactivate() {
	    
	}

	private boolean checkSourceFilter(Sentence sentence) {
        NMEASource s = getSource();
        if (s!=null && s.getFilter()!=null)
            return s.getFilter().match(sentence, getName());
        return true;
    }

	/**
	 * Used by "sources" to push sentences into the stream
	 * @param sentence The sentemce to be notified to agents
	 */
	protected final void notify(Sentence sentence) {

		if (isStarted() && checkSourceFilter(sentence) && listener!=null) {
            getLogger().debug("Notify Sentence {" + sentence.toSentence() + "}");
            List<Sentence> toSend = procs.getSentences(sentence, getName());
            for (Sentence s : toSend)
                listener.onSentence(RouterMessageImpl.createMessage(s, getName(), cache.getNow()));
        }
	}
	/**
	 * Used by "sources" to push sentences into the stream
	 * @param m The message to be notified to agents
	 */
	protected final void notify(JSONObject m) {

		if (isStarted()) {
            getLogger().debug("Notify Sentence {" + m + "}");
            listener.onSentence(RouterMessageImpl.createMessage(m, getName(), cache.getNow()));
        }
	}

	/**
	 * Sources can use post-proc delegates to add additional elaboration to the sentences they pushes into the stream.
	 * @param f The post processor to be added (sequence is important)
	 */
	protected final void addProc(NMEAPostProcess f) {
		procs.addProcessor(f);
	}

	/**
	 * Must be overridden by targets. They will receive here the stream. 
	 * @param s The sentence just received
	 * @param source The source that originated the sentence
	 */
	protected void doWithSentence(Sentence s, String source) {}

    @Override
    public final NMEASource getSource() {
        return (source?sourceIf:null);
    }
    
    @Override
    public final NMEATarget getTarget() {
        return (target?targetIf:null);
    }

    protected final boolean isSource() {
    	return getSource()!=null;
    }

    protected final boolean isTarget() {
    	return getTarget()!=null;
    }
    
    @Override
    public boolean isUserCanStartAndStop() {
    	return true;
    }
    
    @Override
    public String getType() { 
    	return getClass().getSimpleName();
    }

    @Override
    public void onTimerHR() {
    }

    @Override
    public void onTimer() {
        if (isStarted()) {
            procs.onTimer();
        }
    }

    protected NMEACache getCache() {
        return cache;
    }
}
