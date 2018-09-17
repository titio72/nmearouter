package com.aboni.nmea.router.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeSet;

import javax.inject.Inject;

import com.aboni.nmea.router.NMEACache;
import com.aboni.nmea.router.NMEARouter;
import com.aboni.nmea.router.NMEASentenceListener;
import com.aboni.nmea.router.NMEAStream;
import com.aboni.nmea.router.agent.NMEAAgent;
import com.aboni.nmea.router.agent.NMEAAgentStatusListener;
import com.aboni.nmea.router.agent.NMEATarget;
import com.aboni.nmea.router.conf.LogLevelType;
import com.aboni.utils.ServerLog;

import net.sf.marineapi.nmea.sentence.Sentence;

public class NMEARouterImpl implements NMEARouter {

	private NMEAAgentStatusListener agentStatusListener;
	private NMEASentenceListener sentenceListener;
	
	private Timer timer;
	
	private boolean started;
	
	private class InternalAgentStatusListener implements NMEAAgentStatusListener {
		
		@Override
		public void onStatusChange(NMEAAgent agent) {
			_onStatusChange(agent);
		}
	}

	private class InternalSentenceListener implements NMEASentenceListener {

		@Override
		public void onSentence(Sentence s, NMEAAgent src) {
		    _queueUpSentence(s, src);
		}
	}
	
	private class SentenceEvent {
		SentenceEvent(Sentence s, NMEAAgent src) {
			this.s = s;
			this.src = src;
		}
		
		final Sentence s;
		final NMEAAgent src;
	}
	
	private final Map<String, NMEAAgent> agents;
	
	private final Queue<SentenceEvent> sentenceQueue;
	
	private Thread processingThread;
	private LogLevelType logLevel = LogLevelType.INFO;
	
	private final NMEACache cache;
	private final NMEAStream stream;

	private static final int TIMER = 1000;
	
	@Inject
	public NMEARouterImpl(NMEACache cache, NMEAStream stream) {
	    agents = new HashMap<String, NMEAAgent>();
		agentStatusListener = new InternalAgentStatusListener();
		sentenceListener = new InternalSentenceListener();
		sentenceQueue = new LinkedList<NMEARouterImpl.SentenceEvent>();
		started = false;
		this.cache = cache;
		this.stream = stream;
		timer = null;
	}

	private void onTimer() {
		synchronized (agents) {
			for (Iterator<NMEAAgent> i = agents.values().iterator(); i.hasNext(); ) {
				try {
					i.next().onTimer();
				} catch (Exception e) {
					ServerLog.getLogger().Error("Error dispatching timer!", e);
				}
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see com.aboni.nmea.router.INMEARouter#start()
	 */
	@Override
	public synchronized void start() {
		if (!started) {
			started = true;
			initProcessingThread();
			
			if (timer==null) {
				timer = new Timer(true);
				timer.scheduleAtFixedRate(new TimerTask() {
					
					@Override
					public void run() {
						onTimer();
					}
				}, 0, TIMER);
			}
		}
	}

	/* (non-Javadoc)
	 * @see com.aboni.nmea.router.INMEARouter#stop()
	 */
	@Override
	public synchronized void stop() {
		
		timer.cancel();
		timer.purge();
		
		started = false;
		if (processingThread!=null) {
			processingThread = null;
		}
	}
	
	/* (non-Javadoc)
	 * @see com.aboni.nmea.router.INMEARouter#isStarted()
	 */
	@Override
	public boolean isStarted() {
		return started;
	}
	
	private void initProcessingThread() {
		processingThread = new Thread(new Runnable() {
			@Override
			public void run() {
				while (started) {
					SentenceEvent e = null;
					do  { 
						synchronized (sentenceQueue) {
							e = sentenceQueue.poll();
						}
						if (e!=null) {
							_routeSentence(e.s, e.src);
						}
					} while (e!=null);
					synchronized (sentenceQueue) {
						try { sentenceQueue.wait(); } catch (InterruptedException e1) { e1.printStackTrace(); }
					}
				}
			}
		});
		//processingThread.setDaemon(true);
		processingThread.start();
	}

	@Override
	public void addAgent(NMEAAgent agent) {
		synchronized (agents) {
			ServerLog.getLogger().Info("Adding Agent {" + agent.getName() + "}");
			agents.put(agent.getName(), agent);
			agent.setStatusListener(agentStatusListener);
			if (agent.getSource()!=null) {
			    agent.getSource().setSentenceListener(sentenceListener);
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
        synchronized (sentenceQueue) { 
            sentenceQueue.add(e);
            sentenceQueue.notifyAll();
        }
    }
	
	private void _routeSentence(Sentence s, NMEAAgent src) {
		if (started) {
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
				    if (src.getName().equals(tgt.getName())) {
				        // do nothing, do not route messages back to the originator
				    } else if (target!=null) {
				        target.pushSentence(s, src);
				    }
				} catch (Exception e) {
					ServerLog.getLogger().Error("Error dispatching to target!", e);
				}
			}
		}
	}

    public void setPreferedLogLevel(LogLevelType level) {
    	logLevel = level;
    }

	@Override
	public LogLevelType getPreferredLogLevelType() {
    	return logLevel ;
	}
}
