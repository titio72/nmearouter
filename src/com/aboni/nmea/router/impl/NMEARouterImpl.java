package com.aboni.nmea.router.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeSet;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;

import org.json.JSONObject;

import com.aboni.nmea.router.NMEACache;
import com.aboni.nmea.router.NMEARouter;
import com.aboni.nmea.router.NMEAStream;
import com.aboni.nmea.router.agent.NMEAAgent;
import com.aboni.nmea.router.agent.NMEAAgentStatusListener;
import com.aboni.nmea.router.agent.NMEATarget;
import com.aboni.utils.ServerLog;

import net.sf.marineapi.nmea.sentence.Sentence;

public class NMEARouterImpl implements NMEARouter {

	private Timer timer;
	private AtomicBoolean started;
	private ExecutorService exec;
	
	private class SentenceEvent {
		SentenceEvent(Sentence s, NMEAAgent src) {
			this.s = s;
			this.src = src;
		}
		
		final Sentence s;
		final NMEAAgent src;
	}
	
	private final Map<String, NMEAAgent> agents;
	private final BlockingQueue<SentenceEvent> sentenceQueue;
	private final NMEACache cache;
	private final NMEAStream stream;

	private static final int TIMER_FACTOR  	= 4; // every "FACTOR" HighRes timer a regular timer is invoked
	private static final int TIMER_HR		= 250;
	private int timer_count = 0;
	
	private long lastStatsTime;
	private static final long STATS_PERIOD = 60; // seconds
	
	@Inject
	public NMEARouterImpl(NMEACache cache, NMEAStream stream) {
	    agents = new HashMap<String, NMEAAgent>();
		sentenceQueue = new LinkedBlockingQueue<NMEARouterImpl.SentenceEvent>();
		started  = new AtomicBoolean(false);
		this.cache = cache;
		this.stream = stream;
		exec = Executors.newFixedThreadPool(4);
		timer = null;
	}

	private void onTimerHR() {
		synchronized (agents) {
			timer_count = (timer_count+1) % TIMER_FACTOR;
			for (NMEAAgent a: agents.values()) {
				exec.execute(()->a.onTimerHR());
				if (timer_count==0) {
					exec.execute(()->a.onTimer());
					dumpStats();
				}
			}
		}
	}
	
	private void dumpStats() {
		long t = System.currentTimeMillis();
		if (t - lastStatsTime >= (STATS_PERIOD * 1000)) {
			lastStatsTime = t;
			ServerLog.getLogger().Info(String.format("Router Queue Size {%d}", sentenceQueue.size()) + "}");
		}
	}

	@Override
	public void start() {
		synchronized (this) {
			if (!started.get()) {
				started.set(true);
				initProcessingThread();
				
				if (timer==null) {
					timer = new Timer(true);
					timer.scheduleAtFixedRate(new TimerTask() {
						
						@Override
						public void run() {
							onTimerHR();
						}
					}, 0, TIMER_HR);
				}
			}
		}
	}

	@Override
	public void stop() {
		synchronized (this) {
			timer.cancel();
			timer.purge();
			started.set(false);
		}
	}
	
	@Override
	public boolean isStarted() {
		return started.get();
	}
	
	private void initProcessingThread() {
		exec.execute(()->{
				while (started.get()) {
					try { 
						SentenceEvent e = sentenceQueue.take(); 
						_routeSentence(e.s, e.src);
					} catch (InterruptedException e1) { e1.printStackTrace(); }
				}
			});
	}

	@Override
	public void addAgent(NMEAAgent agent) {
		synchronized (agents) {
			ServerLog.getLogger().Info("Adding Agent {" + agent.getName() + "}");
			agents.put(agent.getName(), agent);
			agent.setStatusListener(new NMEAAgentStatusListener() {
				
				@Override
				public void onStatusChange(NMEAAgent agent) { _onStatusChange(agent); }

				@Override
				public void onData(JSONObject s, NMEAAgent src) { _queueUpData(s, src); }
			});
			if (agent.getSource()!=null) {
			    agent.getSource().setSentenceListener((Sentence s, NMEAAgent src)->_queueUpSentence(s, src));
			}
		}
	}
	
	@Override
	public NMEAAgent getAgent(String name) {
		synchronized (agents) {
			return agents.get(name);
		}
	}
	
	@Override
	public Collection<String> getAgents() {
		synchronized (agents) {
			return new TreeSet<String>(agents.keySet());
		}
	}
	
	private void _onStatusChange(NMEAAgent src) {
	}

    private void _queueUpSentence(Sentence s, NMEAAgent src) {
        SentenceEvent e = new SentenceEvent(s, src);
        try {
			sentenceQueue.put(e);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
    }

    private void _queueUpData(JSONObject s, NMEAAgent src) {
		stream.pushData(s, src);
    }
	
	private void _routeSentence(Sentence s, NMEAAgent src) {
		if (started.get()) {
			cache.onSentence(s, src.getName());
			routeToTarget(s, src);
			stream.pushSentence(s, src);
		}
	}

	private void routeToTarget(Sentence s, NMEAAgent src) {
		synchronized (agents) {
			for (Iterator<NMEAAgent> i = agents.values().iterator(); i.hasNext(); ) {
				try {
				    NMEAAgent tgt = i.next();
				    NMEATarget target = tgt.getTarget();        
				    if (target!=null && !src.getName().equals(tgt.getName())) {
				    	exec.execute(()->target.pushSentence(s, src));
				    }
				} catch (Exception e) {
					ServerLog.getLogger().Error("Error dispatching to target!", e);
				}
			}
		}
	}
}
