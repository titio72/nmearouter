package com.aboni.nmea.router.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.aboni.nmea.router.NMEAAgentStatusListener;
import com.aboni.nmea.router.NMEACache;
import com.aboni.nmea.router.NMEARouter;
import com.aboni.nmea.router.NMEAStreamProvider;
import com.aboni.nmea.router.agent.NMEAAgent;
import com.aboni.nmea.router.agent.NMEASentenceListener;
import com.aboni.nmea.router.agent.NMEATarget;
import com.aboni.nmea.router.conf.LogLevelType;
import com.aboni.utils.ServerLog;

import net.sf.marineapi.nmea.sentence.Sentence;

public class NMEARouterImpl implements NMEARouter {

	private NMEAAgentStatusListener agentStatusListener;
	private NMEASentenceListener sentenceListener;
	private NMEACacheImpl headPosCache;
	
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
			SentenceEvent e = new SentenceEvent(s, src);
			synchronized (sentenceQueue) { 
				sentenceQueue.add(e);
				sentenceQueue.notifyAll();
			}
			
		}
	}
	
	private class SentenceEvent {
		SentenceEvent(Sentence s, NMEAAgent src) {
			this.s = s;
			this.src = src;
		}
		
		Sentence s;
		NMEAAgent src;
	}
	
	private Map<String, NMEAAgent> agents;
	
	private Queue<SentenceEvent> sentenceQueue;
	
	private Thread processingThread;
	private LogLevelType logLevel = LogLevelType.INFO;
	
	public NMEARouterImpl() {
	    agents = new HashMap<String, NMEAAgent>();
		agentStatusListener = new InternalAgentStatusListener();
		sentenceListener = new InternalSentenceListener();
		sentenceQueue = new ConcurrentLinkedQueue<NMEARouterImpl.SentenceEvent>();
		started = false;
		headPosCache = new NMEACacheImpl();
	}
	
	/* (non-Javadoc)
	 * @see com.aboni.nmea.router.INMEARouter#start()
	 */
	@Override
	public synchronized void start() {
		if (!started) {
			started = true;
			headPosCache.start();
			initProcessingThread();
		}
	}

	/* (non-Javadoc)
	 * @see com.aboni.nmea.router.INMEARouter#stop()
	 */
	@Override
	public synchronized void stop() {
		started = false;
		if (processingThread!=null) {
			processingThread = null;
		}
        headPosCache.stop();
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
							_onSentence(e.s, e.src);
						}
					} while (e!=null);
					synchronized (sentenceQueue) {
						try { sentenceQueue.wait(); } catch (InterruptedException e1) { e1.printStackTrace(); }
					}
				}
			}
		});
		processingThread.setDaemon(true);
		processingThread.start();
	}

	@Override
	public void addAgent(NMEAAgent agent) {
		synchronized (agents) {
			ServerLog.getLogger().Info("Adding NMEA Agent " + agent.toString());
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
			return agents.keySet();
		}
	}
	
	private synchronized void _onStatusChange(NMEAAgent src) {
	}

	private synchronized void _onSentence(Sentence s, NMEAAgent src) {
		if (started) {
			headPosCache.onSentence(s, src);
			NMEAStreamProvider.getStreamInstance().pushSentence(s, src);
			routeToTarget(s, src);
		}
	}

	private void routeToTarget(Sentence s, NMEAAgent src) {
		synchronized (agents) {
			for (Iterator<NMEAAgent> i = agents.values().iterator(); i.hasNext(); ) {
				try {
				    NMEATarget target = i.next().getTarget();        
				    if (src.equals(target)) {
				        // do nothing, skip routing messages to the originator
				    } else if (target!=null) {
				        target.pushSentence(s, src);
				    }
				} catch (Exception e) {
					ServerLog.getLogger().Error("Error dispatching to target!", e);
				}
			}
		}
	}

    @Override
    public NMEACache getCache() {
        return headPosCache;
    }

    public void setPreferedLogLevel(LogLevelType level) {
    	logLevel = level;
    }

	@Override
	public LogLevelType getPreferredLogLevelType() {
    	return logLevel ;
	}
}
