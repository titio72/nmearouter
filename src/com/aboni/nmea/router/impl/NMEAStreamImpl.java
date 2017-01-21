package com.aboni.nmea.router.impl;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.aboni.nmea.router.NMEAStream;
import com.aboni.nmea.router.agent.NMEAAgent;
import com.aboni.nmea.router.agent.NMEASentenceListener;
import com.aboni.utils.ServerLog;

import net.sf.marineapi.nmea.sentence.Sentence;

public class NMEAStreamImpl implements NMEAStream {

	private Set<NMEASentenceListener> listeners;
	
	public NMEAStreamImpl() {
		listeners = new HashSet<NMEASentenceListener>();
	}

	@Override
	public void addSentenceListener(NMEASentenceListener listener) {
		synchronized (listeners) {
			listeners.add(listener);
		}
	}

	@Override
	public void dropSentenceListener(NMEASentenceListener listener) {
		synchronized (listeners) {
			listeners.remove(listener);
		}
	}

	@Override
	public void pushSentence(Sentence s, NMEAAgent src) {
		synchronized (listeners) {
			for (Iterator<NMEASentenceListener> i = listeners.iterator(); i.hasNext(); ) {
				try {
					i.next().onSentence(s, src);
				} catch (Exception e) {
					ServerLog.getLogger().Error("Error dispatchinc event to listener!", e);
				}
			}
		}
	}

}
