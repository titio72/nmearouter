package com.aboni.nmea.router.agent.impl;

import com.aboni.nmea.router.NMEACache;
import com.aboni.nmea.router.NMEASentenceListener;
import com.aboni.nmea.router.agent.*;
import com.aboni.nmea.router.filters.NMEASentenceFilterSet;
import com.aboni.nmea.router.processors.*;
import com.aboni.utils.Log;
import com.aboni.utils.Pair;
import com.aboni.utils.ServerLog;
import net.sf.marineapi.nmea.sentence.Sentence;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

public abstract class NMEAAgentImpl implements NMEAAgent {

	private class _Source implements NMEASource {

		@Override
		public NMEASentenceFilterSet getFilter() {
			return _getSourceFilter();
		}

		@Override
		public void setFilter(NMEASentenceFilterSet s) {
			_setSourceFilter(s);
		}

		@Override
		public void setSentenceListener(NMEASentenceListener listener) {
			_setSentenceListener(listener);
		}

	}
	
	private class _Target implements NMEATarget {

		@Override
		public NMEASentenceFilterSet getFilter() {
			return _getTargetFilter();
		}

		@Override
		public void setFilter(NMEASentenceFilterSet s) {
			_setTargetFilter(s);
		}

		@Override
		public void pushSentence(Sentence e, NMEAAgent src) {
			_pushSentence(e, src);
		}
	}
	
	private final String name;
	private NMEAAgentStatusListener sl;
	private NMEASentenceFilterSet fsetInput;
	private NMEASentenceFilterSet fsetOutput;
	private final List<NMEAPostProcess> proc;
	private boolean active;
	private NMEASentenceListener listener;
	private boolean builtin;
	private boolean target;
	private boolean source;
	private final _Target targetIf;
	private final _Source sourceIf;
	
    public NMEAAgentImpl(NMEACache cache, String name, QOS qos) {
    	targetIf = new _Target();
    	sourceIf = new _Source();
        this.name = name;
        fsetInput = null;
        fsetOutput = null;
        active = false;
        proc = new ArrayList<>();
        handleQos(cache, name, qos);
        target = true;
        source = true;
    }

    private void handleQos(NMEACache cache, String name, QOS qos) {
        if (qos!=null) { 
            if (qos.get("dpt")) {
                getLogger().Info("QoS {DPT} Agent {" + name + "}");
                addProc(new NMEADepthEnricher());
            }
            if (qos.get("rmc2vtg")) {
                getLogger().Info("QoS {RMC2VTG} Agent {" + name + "}");
                addProc(new NMEARMC2VTGProcessor());
            }
            if (qos.get("truewind_sog")) {
                getLogger().Info("QoS {TRUEWIND_SOG} Agent {" + name + "}");
                addProc(new NMEAMWVTrue(true));
            }
            if (qos.get("truewind")) {
                getLogger().Info("QoS {TRUEWIND} Agent {" + name + "}");
                addProc(new NMEAMWVTrue(false));
            }
            if (qos.get("enrich_hdg")) {
                getLogger().Info("QoS {ENRICH_HDG} Agent {" + name + "}");
                addProc(new NMEAHDGFiller(cache));
            }
            if (qos.get("enrich_hdm")) {
                getLogger().Info("QoS {ENRICH_HDM} Agent {" + name + "}");
                addProc(new NMEAHeadingEnricher(cache));
            }
            if (qos.get("rmc_filter")) {
                getLogger().Info("QoS {RMC filter} Agent {" + name + "}");
                addProc(new NMEARMCFilter());
            }
            if (qos.get("builtin")) {
                getLogger().Info("QoS {BuiltIn} Agent {" + name + "}");
                builtin = true;
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
	
	protected class _Log implements Log {

	    private final Log log;
	    
	    private _Log() {
	        log = ServerLog.getLogger();
	    }
	    
        @Override
        public void setError() {}

        @Override
        public void setWarning() {}

        @Override
        public void setInfo() {}

        @Override
        public void setDebug() {}

        @Override
        public void setNone() {}

        @Override
        public void Error(String msg) {
            log.Error("Agent " + NMEAAgentImpl.this.toString() + " Name {" + getName() + "} " + msg);
        }

        @Override
        public void Error(String msg, Throwable t) {
            log.Error("Agent " + NMEAAgentImpl.this.toString() + " Name {" + getName() + "} " + " " + msg, t);
        }

        @Override
        public void Warning(String msg) {
            log.Warning("Agent " + NMEAAgentImpl.this.toString() + " Name {" + getName() + "} " + " " + msg);
        }

        @Override
        public void Info(String msg) {
            log.Info("Agent " + NMEAAgentImpl.this.toString() + " Name {" + getName() + "} " + " " + msg);
        }

        @Override
        public void Debug(String msg) {
            log.Debug("Agent " + NMEAAgentImpl.this.toString() + " Name {" + getName() + "} " + " " + msg);
        }

        @Override
        public Logger getBaseLogger() {
            return log.getBaseLogger();
        }
	}
	
	private Log _log;
	
	protected Log getLogger() {
	    if (_log==null) _log = new _Log();
		return _log;
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
				getLogger().Info("Activating agent {" + getName() + "}");
				if (onActivate()) {
					active = true;
					notifyStatus();
				} else {
					getLogger().Error("Cannot activate agent {" + getName() + "}");
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
				getLogger().Info("Deactivating {" + getName() + "}");
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

    private NMEASentenceFilterSet _getTargetFilter() {
		return fsetInput;
	}
	
	private void _setTargetFilter(NMEASentenceFilterSet s) {
		fsetInput = s;
	}
	
	private NMEASentenceFilterSet _getSourceFilter() {
		return fsetOutput;
	}
	
	private void _setSourceFilter(NMEASentenceFilterSet s) {
		fsetOutput = s;
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

	/**
	 * Used by "sources" to push sentences into the stream
	 * @param sentence Teh sentemce to be notified to agents
	 */
	protected final void notify(Sentence sentence) {
		if (isStarted()) {
			if (_getSourceFilter()==null || _getSourceFilter().match(sentence, getName())) {
				getLogger().Debug("Notify Sentence {" + sentence.toSentence() + "}");
                List<Sentence> toSend = new ArrayList<>();
				for (NMEAPostProcess pp: proc) {
                    Pair<Boolean, Sentence[]> res = pp.process(sentence, this.getName());
                    if (res!=null) {
                    	if (!res.first) {
                    		return; // skip the sentence
                    	} else if (res.second!=null) {
							Collections.addAll(toSend, res.second);
                    	}
                    }
                }
				if (listener!=null) {
	                listener.onSentence(sentence, this);
	                for (Sentence s: toSend) listener.onSentence(s, this);
				}
			}
		}
	}
	
	/**
	 * Sources can use post-proc delegates to add additional elaboration to the sentences they pushes into the stream.
	 * @param f The post processor to be added (sequence is important)
	 */
	protected final void addProc(NMEAPostProcess f) {
		proc.add(f);
	}

	/**
	 * Must be overridden by targets. They will receive here the stream. 
	 * @param s The sentence just received
	 * @param source The source that originated the sentence
	 */
	protected void doWithSentence(Sentence s, NMEAAgent source) {}

    
    private void _setSentenceListener(NMEASentenceListener listener) {
        this.listener = listener;
    }

	private void _pushSentence(Sentence s, NMEAAgent source) {
		try {
			if (isStarted() && 
					(_getTargetFilter()==null || _getTargetFilter().match(s,  source.getName()))) {
				doWithSentence(s, source);
			}
		} catch (Throwable t) {
			getLogger().Warning("Error delivering sentence to agent {" + s + "} error {" + t.getMessage() + "}");
    	}
    }
    
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
			synchronized (proc) {
		    	for (NMEAPostProcess p: proc) {
		    		p.onTimer();
		    	}
    		}
    	}
    } 
}