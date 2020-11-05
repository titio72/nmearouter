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

import com.aboni.nmea.router.*;
import com.aboni.nmea.router.agent.NMEAAgent;
import com.aboni.nmea.router.agent.NMEATarget;
import com.aboni.nmea.router.message.Message;
import com.aboni.nmea.router.processors.NMEAPostProcess;
import com.aboni.nmea.router.processors.NMEAProcessorSet;
import com.aboni.nmea.router.processors.NMEARouterProcessorException;
import com.aboni.utils.Log;
import com.aboni.utils.LogStringBuilder;
import org.json.JSONObject;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class NMEARouterImpl implements NMEARouter {

    public static final String ROUTER_CATEGORY = "Router";
    public static final String AGENT_KEY_NAME = "agent";
    private Timer timer;
    private final AtomicBoolean started;
    private final ExecutorService exec;

    private final Map<String, NMEAAgent> agents;
    private final BlockingQueue<RouterMessage> sentenceQueue;
    private final NMEAProcessorSet processors;
    private final NMEACache cache;
    private final TimestampProvider timestampProvider;
    private final RouterMessageFactory messageFactory;
    private final Log log;

    private static final int THREADS_POOL  	= 4;
    private static final int TIMER_FACTOR  	= 4; // every "FACTOR" HighRes timer a regular timer is invoked
    private static final int TIMER_HR		= 250;
    private int timerCount = 0;

    private long lastStatsTime;
    private static final long STATS_PERIOD = 60; // seconds
    private long lastTotalGarbageCollections = 0;
    private long lastGarbageCollectionTime = 0;
    private static int threadN = 0;

    @Inject
    public NMEARouterImpl(@NotNull TimestampProvider tp, @NotNull NMEACache cache, @NotNull RouterMessageFactory messageFactory, @NotNull Log log) {
        agents = new HashMap<>();
        this.log = log;
        this.messageFactory = messageFactory;
        this.cache = cache;
        this.timestampProvider = tp;
        sentenceQueue = new LinkedBlockingQueue<>();
        processors = new NMEAProcessorSet();
        started = new AtomicBoolean(false);
        exec = Executors.newFixedThreadPool(THREADS_POOL, r -> {
            Thread t = new Thread(r, "Router thread " + threadN);
            threadN++;
            return t;
        });
        timer = null;
    }

    private void onTimerHR() {
        synchronized (agents) {
            if (timerCount % 4 == 0) notifyDiagnostic();
            timerCount = (timerCount + 1) % TIMER_FACTOR;
            for (NMEAAgent a : agents.values()) {
                runMe(a::onTimerHR, "timerHR", AGENT_KEY_NAME, a.getName());
                if (timerCount == 0) {
                    runMe(a::onTimer, "timer", AGENT_KEY_NAME, a.getName());
                    dumpStats();
                }
            }
        }
    }

    private void runMe(Runnable r, String logOp, String logkey, String logvalue) {
        try {
            exec.execute(r);
        } catch (Exception e) {
            log.error(LogStringBuilder.start(ROUTER_CATEGORY).wO(logOp).wV(logkey, logvalue).toString(), e);
        }
    }

    private void printGCStats() {
        long totalGarbageCollections = 0;
        long garbageCollectionTime = 0;

        for(GarbageCollectorMXBean gc : ManagementFactory.getGarbageCollectorMXBeans()) {
            long count = gc.getCollectionCount();
            if(count >= 0) totalGarbageCollections += count;
            long time = gc.getCollectionTime();
            if(time >= 0) garbageCollectionTime += time;
        }
        LogStringBuilder.start(ROUTER_CATEGORY).wO("GC stats").
                wV("count", totalGarbageCollections - lastTotalGarbageCollections).
                wV("time", garbageCollectionTime - lastGarbageCollectionTime).info(log);
        lastTotalGarbageCollections = totalGarbageCollections;
        lastGarbageCollectionTime = garbageCollectionTime;
    }

    private void dumpStats() {
        long t = timestampProvider.getNow();
        if (t - lastStatsTime >= (STATS_PERIOD * 1000)) {
            lastStatsTime = t;
            log.info(LogStringBuilder.start(ROUTER_CATEGORY).wO("stats").wV("queue", sentenceQueue.size()).
                    wV("mem", Runtime.getRuntime().freeMemory()).toString());
            printGCStats();
        }
    }

    private void notifyDiagnostic() {
        JSONObject msg = new JSONObject();
        msg.put("topic", "diagnostic");
        msg.put("queue", sentenceQueue.size());
        msg.put("free_memory", Runtime.getRuntime().freeMemory() / 1024 / 1024);
        RouterMessage m = messageFactory.createMessage(msg, "SYS", timestampProvider.getNow());
        privateQueueUpSentence(m);
    }

    @Override
    public void start() {
        synchronized (this) {
            if (!started.get()) {
                started.set(true);
                if (timer == null) {
                    timer = new Timer("Router timer", true);
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
                        LogStringBuilder.start(ROUTER_CATEGORY).wO("route loop").error(log, e1);
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
            log.info(LogStringBuilder.start(ROUTER_CATEGORY).wO("add agent").wV(AGENT_KEY_NAME, agent.getName()).toString());
            agents.put(agent.getName(), agent);
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

    private void privateQueueUpSentence(RouterMessage s) {
        try {
            if (isStarted()) sentenceQueue.put(s);
        } catch (InterruptedException e1) {
            Thread.currentThread().interrupt();
            log.errorForceStacktrace("Thread error", e1);
        }
    }

    private void routeSentence(RouterMessage m) {
        if (started.get()) {
            if (m.getPayload() instanceof JSONObject) {
                routeToTargets(new RouterMessage[] {m});
            } else if (m.getPayload() instanceof Message) {
                Message s = (Message) m.getPayload();
                Collection<Message> toSend = null;
                try {
                    toSend = processors.getSentences(s, m.getSource());
                } catch (NMEARouterProcessorException e) {
                    log.error(LogStringBuilder.start(ROUTER_CATEGORY).wO("route message").wV("message", m.getPayload()).toString(), e);
                }
                if (toSend != null) {
                    final RouterMessage[] messages = new RouterMessage[toSend.size()];
                    int counter = 0;
                    for (Message ss : toSend) {
                        cache.onSentence(ss, m.getSource());
                        RouterMessage mm = messageFactory.createMessage(ss, m.getSource(), m.getTimestamp());
                        messages[counter] = mm;
                        counter++;
                    }
                    routeToTargets(messages);
                }
            }
        }
    }

    private void routeToTargets(final RouterMessage[] mm) {
        synchronized (agents) {
            for (NMEAAgent nmeaAgent : agents.values()) {
                try {
                    final NMEATarget target = nmeaAgent.getTarget();
                    final String name = nmeaAgent.getName();
                    if (target != null) {
                        runMe(() -> {
                            for (RouterMessage m : mm) {
                                routeToTarget(name, target, m);
                            }
                        }, "dispatch message", "messages", ""+mm.length);
                    }
                } catch (Exception e) {
                    log.error(LogStringBuilder.start(ROUTER_CATEGORY).wO("dispatch message").toString(), e);
                }
            }
        }
    }

    private void routeToTarget(String targetName, NMEATarget targetInterface, RouterMessage m) {
        if (!m.getSource().equals(targetName)) {
            try {
                targetInterface.pushMessage(m);
            } catch (Exception e) {
                log.errorForceStacktrace("PHAVA", e);
            }
        }
    }
}
