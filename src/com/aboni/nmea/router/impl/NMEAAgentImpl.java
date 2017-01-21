package com.aboni.nmea.router.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.aboni.nmea.router.Filterable;
import com.aboni.nmea.router.NMEAAgentStatusListener;
import com.aboni.nmea.router.agent.NMEAAgent;
import com.aboni.nmea.router.agent.NMEASentenceListener;
import com.aboni.nmea.router.agent.NMEASource;
import com.aboni.nmea.router.agent.NMEATarget;
import com.aboni.nmea.router.agent.QOS;
import com.aboni.nmea.router.filters.NMEAFilterSet;
import com.aboni.nmea.router.filters.NMEAHDGFiller;
import com.aboni.nmea.router.filters.NMEAPostProcess;
import com.aboni.nmea.router.filters.NMEARMC2VTGProcessor;
import com.aboni.utils.Log;
import com.aboni.utils.ServerLog;

import net.sf.marineapi.nmea.sentence.Sentence;

public abstract class NMEAAgentImpl implements NMEAAgent, NMEASource, NMEATarget {

	private String name;
	private NMEAAgentStatusListener sl;
	private NMEAFilterSet fsetInput;
	private NMEAFilterSet fsetOutput;
	private List<NMEAPostProcess> proc;
	private boolean active;
	private NMEASentenceListener listener;
	private boolean builtin;
	private boolean target;
	private boolean source;
	
    public NMEAAgentImpl(String name, QOS qos) {
        this.name = name;
        fsetInput = new NMEAFilterSet();
        fsetOutput = new NMEAFilterSet();
        proc = new ArrayList<NMEAPostProcess>();
        active = false;
        if (qos!=null) { 
            if (qos.get("rmc2vtg")) {
                getLogger().Info("QoS {RMC2VTG}");
                addProc(new NMEARMC2VTGProcessor());
            }
            if (qos.get("enrich_hdg")) {
                getLogger().Info("QoS {ENRICH_HDG}");
                addProc(new NMEAHDGFiller(true, true));
            }
            if (qos.get("builtin")) {
                builtin = true;
            }
        }
        target = true;
        source = true;
    }
    
	public NMEAAgentImpl(String name) {
		this(name, null);
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
			getLogger().Info("Activating");
			if (onActivate()) {
				active = true;
				notifyStatus();
			} else {
				getLogger().Error("Cannot activate agent");
			}
		}
	}
	
	private void notifyStatus() {
		if (sl!=null) sl.onStatusChange(this);
	}

	@Override
	public void stop() {
		if (active) {
			getLogger().Info("Active {no}");
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
	
	@Override
	public Filterable getInputFilter() {
		return fsetInput;
	}
	
	@Override
	public Filterable getOutputFilter() {
		return fsetOutput;
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
	
	protected final NMEAFilterSet getOutputFilterSet() {
		return fsetOutput;
	}

	protected final NMEAFilterSet getInputFilterSet() {
		return fsetInput;
	}
	
	/**
	 * Used by "sources" to push sentences into the stream
	 * @param sentence
	 */
	protected final void notify(Sentence sentence) {
		if (isStarted()) {
			if (getOutputFilterSet().accept(sentence, getName())) {
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

    
    @Override
    public final void setSentenceListener(NMEASentenceListener listener) {
        this.listener = listener;
    }
    
    @Override
    public final void unsetSentenceListener() {
        listener = null;
    }
    
    @Override
    public final void pushSentence(Sentence s, NMEAAgent source) {
    	if (isStarted()) {
    		doWithSentence(s, source);
    	}
    }
    
    @Override
    public final NMEASource getSource() {
        return (source?this:null);
    }
    
    @Override
    public final NMEATarget getTarget() {
        return (target?this:null);
    }

    protected final boolean isSource() {
    	return getSource()!=null;
    }

    protected final boolean isTarget() {
    	return getSource()!=null;
    }
}
