/*
(C) 2020, Andrea Boni
This file is part of NMEARouter.
NMEARouter is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.
NMEARouter is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.
You should have received a copy of the GNU General Public License
along with NMEARouter.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.aboni.nmea.router.impl;

import com.aboni.nmea.router.NMEACache;
import com.aboni.nmea.router.NMEARouter;
import com.aboni.nmea.router.NMEAStream;
import com.aboni.nmea.router.RouterMessage;
import com.aboni.nmea.router.agent.NMEAAgent;
import com.aboni.nmea.router.agent.NMEATarget;
import com.aboni.nmea.router.n2k.N2KMessage;
import com.aboni.nmea.router.processors.NMEAPostProcess;
import com.aboni.nmea.router.processors.NMEAProcessorSet;
import com.aboni.utils.ServerLog;
import net.sf.marineapi.nmea.sentence.Sentence;
import org.json.JSONObject;

import javax.inject.Inject;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class NMEARouterImpl implements NMEARouter {

    private Timer timer;
    private final AtomicBoolean started;
    private final ExecutorService exec;

    private final Map<String, NMEAAgent> agents;
    private final BlockingQueue<RouterMessage> sentenceQueue;
    private final NMEAProcessorSet processors;
    private final NMEACache cache;
    private final NMEAStream stream;

    private static final int TIMER_FACTOR  	= 4; // every "FACTOR" HighRes timer a regular timer is invoked
    private static final int TIMER_HR		= 250;
    private int timerCount = 0;

    private long lastStatsTime;
    private static final long STATS_PERIOD = 60; // seconds

    @Inject
    public NMEARouterImpl(NMEACache cache, NMEAStream stream) {
        agents = new HashMap<>();
        sentenceQueue = new LinkedBlockingQueue<>();
        processors = new NMEAProcessorSet();
        started  = new AtomicBoolean(false);
        this.cache = cache;
        this.stream = stream;
        exec = Executors.newFixedThreadPool(4);
        timer = null;
    }

    private void onTimerHR() {
        synchronized (agents) {
            if (timerCount % 4 == 0) notifyDiag();
            timerCount = (timerCount +1) % TIMER_FACTOR;
            for (NMEAAgent a: agents.values()) {
                exec.execute(a::onTimerHR);
                if (timerCount ==0) {
                    exec.execute(a::onTimer);
                    dumpStats();
                }
            }
        }
    }

    private void dumpStats() {
        long t = cache.getNow();
        if (t - lastStatsTime >= (STATS_PERIOD * 1000)) {
            lastStatsTime = t;
            ServerLog.getLogger().info(String.format("Router Queue Size {%d}", sentenceQueue.size()) + "}");
        }
    }

    private void notifyDiag() {
        JSONObject msg = new JSONObject();
        msg.put("topic", "diag");
        msg.put("queue", sentenceQueue.size());
        msg.put("free_memory", Runtime.getRuntime().freeMemory()/1024/1024);
        RouterMessage m = RouterMessageImpl.createMessage(msg, "SYS", cache.getNow());
        privateQueueUpSentence(m);

    }

    @Override
    public void start() {
        synchronized (this) {
            if (!started.get()) {
                started.set(true);
                if (timer == null) {
                    timer = new Timer(true);
                    timer.scheduleAtFixedRate(new TimerTask() {

                        @Override
                        public void run() {
                            onTimerHR();
                        }
                    }, 0, TIMER_HR);
                }
                while (started.get()) {
                    try {
                        routeSentence(sentenceQueue.take());
                    } catch (InterruptedException e1) {
                        Thread.currentThread().interrupt();
                    }
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

    @Override
    public void addProcessor(NMEAPostProcess processor) {
        processors.addProcessor(processor);
    }

    @Override
    public void addAgent(NMEAAgent agent) {
        synchronized (agents) {
            ServerLog.getLogger().info("Adding Agent {" + agent.getName() + "}");
            agents.put(agent.getName(), agent);
            agent.setStatusListener(this::privateOnStatusChange);
            if (agent.getSource()!=null) {
                agent.getSource().setSentenceListener(this::privateQueueUpSentence);
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
            return new TreeSet<>(agents.keySet());
        }
    }

    private void privateOnStatusChange(NMEAAgent src) {
        ServerLog.getLogger().debug("New status received for {" + src + "}");
    }

    private void privateQueueUpSentence(RouterMessage s) {
        try {
            sentenceQueue.put(s);
        } catch (InterruptedException e1) {
            Thread.currentThread().interrupt();
        }
    }

    private void routeSentence(RouterMessage m) {
        if (started.get()) {
            if (m.getPayload() instanceof N2KMessage) {
                routeToTarget(new RouterMessage[]{m});
            } else if (m.getPayload() instanceof Sentence) {
                Sentence s = (Sentence) m.getPayload();
                Collection<Sentence> toSend = processors.getSentences(s, m.getSource());
                final RouterMessage[] messages = new RouterMessage[toSend.size()];
                int counter = 0;
                for (Sentence ss : toSend) {
                    cache.onSentence(ss, m.getSource());
                    RouterMessage mm = RouterMessageImpl.createMessage(ss, m.getSource(), m.getTimestamp());
                    messages[counter] = mm;
                }
                routeToTarget(messages);
                exec.execute(() -> {
                    for (RouterMessage mm : messages) {
                        stream.pushSentence(mm);
                    }
                });
            } else if (m.getPayload() instanceof JSONObject) {
                exec.execute(() -> stream.pushSentence(m));
            }
        }
    }

    private void routeToTarget(final RouterMessage[] mm) {
        synchronized (agents) {
            for (NMEAAgent nmeaAgent : agents.values()) {
                try {
                    final NMEATarget target = nmeaAgent.getTarget();
                    exec.execute(() -> {
                        for (RouterMessage m : mm) {
                            if (target != null && !m.getSource().equals(nmeaAgent.getName())) {
                                target.pushMessage(m);
                            }
                        }
                    });
                } catch (Exception e) {
                    ServerLog.getLogger().error("Error dispatching to target!", e);
                }
            }
        }
    }
}
