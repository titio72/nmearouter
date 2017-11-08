package com.aboni.nmea.router.agent.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.aboni.nmea.router.NMEACache;
import com.aboni.nmea.router.NMEASentenceListener;
import com.aboni.nmea.router.NMEAStream;
import com.aboni.nmea.router.agent.NMEAAgent;
import com.aboni.nmea.router.agent.NMEAAgentStatusListener;
import com.aboni.nmea.router.agent.NMEASource;
import com.aboni.nmea.router.agent.NMEATarget;
import com.aboni.nmea.router.agent.QOS;
import com.aboni.nmea.router.filters.NMEAHDGFiller;
import com.aboni.nmea.router.filters.NMEAPostProcess;
import com.aboni.nmea.router.filters.NMEARMC2VTGProcessor;
import com.aboni.nmea.router.filters.NMEASentenceFilterSet;
import com.aboni.utils.Log;
import com.aboni.utils.ServerLog;

import net.sf.marineapi.nmea.sentence.Sentence;

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

		@Override
		public void unsetSentenceListener() {
			_unsetSentenceListener();
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
	
	private String name;
	private NMEAAgentStatusListener sl;
	private NMEASentenceFilterSet fsetInput;
	private NMEASentenceFilterSet fsetOutput;
	private List<NMEAPostProcess> proc;
	private boolean active;
	private NMEASentenceListener listener;
	private boolean builtin;
	private boolean target;
	private boolean source;
	private _Target targetIf;
	private _Source sourceIf;
	
    public NMEAAgentImpl(NMEACache cache, NMEAStream stream, String name, QOS qos) {
    	targetIf = new _Target();
    	sourceIf = new _Source();
        this.name = name;
        fsetInput = null;
        fsetOutput = null;
        proc = new ArrayList<NMEAPostProcess>();
        active = false;
        if (qos!=null) { 
            if (qos.get("rmc2vtg")) {
                getLogger().Info("QoS {RMC2VTG} Agent {" + name + "}");
                addProc(new NMEARMC2VTGProcessor());
            }
            if (qos.get("enrich_hdg")) {
                getLogger().Info("QoS {ENRICH_HDG} Agent {" + name + "}");
                addProc(new NMEAHDGFiller(cache));
            }
            if (qos.get("builtin")) {
                getLogger().Info("QoS {BuiltIn} Agent {" + name + "}");
                builtin = true;
            }
        }
        target = true;
        source = true;
    }
    
	public NMEAAgentImpl(NMEACache cache, NMEAStream stream, String name) {
		this(cache, stream, name, null);
	}
	
	protected void setBuiltIn() {
		builtin = true;
	}

	protected void setSourceTarget(boolean isSource, boolean isTarget) {
	    target = isTarget;
	    source = isSource;
	}
	
	
	protected Log getLogger() {
		return ServerLog.getLogger();
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
		return active;
	}

	@Override
	public void start() {
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
	
	private void notifyStatus() {
		if (sl!=null) sl.onStatusChange(this);
	}

	@Override
	public void stop() {
		if (active) {
			getLogger().Info("Deactivating {" + getName() + "}");
			onDeactivate();
			active = false;
			notifyStatus();
		}
	}

    @Override
    public void setStatusListener(NMEAAgentStatusListener listener) {
        sl = listener;
    }

    @Override
    public void unsetStatusListener() {
        sl = null;
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
		return getName();
	}
	
	protected boolean onActivate() {
		return true;
	}
	
	protected void onDeactivate() {
	    
	}
	
	/**
	 * Used by "sources" to push sentences into the stream
	 * @param sentence
	 */
	protected final void notify(Sentence sentence) {
		if (isStarted()) {
			if (_getSourceFilter()==null || _getSourceFilter().match(sentence, getName())) {
				getLogger().Debug("Notify Sentence {" + sentence.toSentence() + "}");
                for (NMEAPostProcess pp: proc) {
                    Sentence[] outSS = pp.process(sentence, this.getName());
					if (outSS!=null) {
						for (Sentence outS: outSS) {
							listener.onSentence(outS, this);
						}
					}
                }
                listener.onSentence(sentence, this);
			}
		}
	}
	
	/**
	 * Sources can use post-proc delegates to add additional elaboration to the sentences they pushes into the stream.
	 * @param f
	 */
	protected final void addProc(NMEAPostProcess f) {
		proc.add(f);
	}

	/**
	 * Sources can use post-proc delegates to add additional elaboration to the sentences they pushes into the stream.
	 * @param f
	 */
	protected final Collection<NMEAPostProcess> getProc() {
		return proc;
	}

	/**
	 * Must be overridden by targets. They will receive here the stream. 
	 * @param s
	 * @param source
	 */
	protected abstract void doWithSentence(Sentence s, NMEAAgent source);

    
    private void _setSentenceListener(NMEASentenceListener listener) {
        this.listener = listener;
    }
    
    private void _unsetSentenceListener() {
        listener = null;
    }
    
    private void _pushSentence(Sentence s, NMEAAgent source) {
		try {
			if (isStarted() && 
					(_getTargetFilter()==null || _getTargetFilter().match(s,  source.getName()))) {
				doWithSentence(s, source);
			}
		} catch (Throwable t) {
			getLogger().Error("Error delivering sentence to agent", t);
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
    	return getSource()!=null;
    }
    
    @Override
    public boolean isUserCanStartAndStop() {
    	return true;
    }
    
    @Override
    public String getType() { 
    	return getClass().getSimpleName();
    }
}
