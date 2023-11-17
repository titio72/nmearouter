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

import com.aboni.log.Log;
import com.aboni.log.LogStringBuilder;
import com.aboni.log.SafeLog;
import com.aboni.nmea.router.NMEACache;
import com.aboni.nmea.router.NMEARouter;
import com.aboni.nmea.router.RouterMessage;
import com.aboni.nmea.router.RouterMessageFactory;
import com.aboni.nmea.router.agent.NMEAAgent;
import com.aboni.nmea.router.agent.NMEATarget;
import com.aboni.nmea.message.Message;
import com.aboni.utils.*;
import org.json.JSONObject;

import javax.inject.Inject;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class NMEARouterImpl implements NMEARouter {

    public static final String ROUTER_CATEGORY = "Router";
    public static final String AGENT_KEY_NAME = "agent";
    private Timer timer;
    private final AtomicBoolean started;
    private final MyThreadPool threadPool;

    private final Map<String, NMEAAgent> agents;
    private final BlockingQueue<RouterMessage> sentenceQueue;
    private final NMEACache cache;
    private final TimestampProvider timestampProvider;
    private final RouterMessageFactory messageFactory;
    private final Log log;

    private static final int THREADS_POOL = 8;
    private static final int TIMER_FACTOR = 4; // every "FACTOR" HighRes timer a regular timer is invoked
    private static final int TIMER_HR = 250;
    private int timerCount = 0;

    private long lastStatsTime;
    private static final long STATS_PERIOD = 60; // seconds
    private final Stats stats = new Stats();

    private final long[] lastGC = new long[]{0, 0};

    private static final class Stats {
        long timerHR;
        long timer;
        long dispatchedMessages;
        long receivedMessages;
        long exec;

        void onTimerHR() {
            synchronized (this) {
                timerHR++;
            }
        }

        void onTimer() {
            synchronized (this) {
                timer++;
            }
        }

        void onDispatchedMessage() {
            synchronized (this) {
                dispatchedMessages++;
            }
        }

        void onReceivedMessage() {
            synchronized (this) {
                receivedMessages++;
            }
        }

        void onExec() {
            synchronized (this) {
                exec++;
            }
        }

        void reset() {
            synchronized (this) {
                timer = 0;
                exec = 0;
                timerHR = 0;
                dispatchedMessages = 0;
                receivedMessages = 0;
            }
        }

        LogStringBuilder dump(LogStringBuilder l) {
            synchronized (this) {
                return l.wV("exec", exec).wV("timers", timer).
                        wV("timerHRs", timerHR).
                        wV("dispMsg", dispatchedMessages).
                        wV("recvMsg", receivedMessages);
            }
        }
    }

    @Inject
    public NMEARouterImpl(TimestampProvider tp, NMEACache cache, RouterMessageFactory messageFactory, Log log) {
        agents = new HashMap<>();
        this.log = SafeLog.getSafeLog(log);
        if (cache==null) throw new IllegalArgumentException("Cache is null");
        if (tp==null) throw new IllegalArgumentException("Timestamp provider is null");
        if (messageFactory==null) throw new IllegalArgumentException("Message factory is null");
        this.messageFactory = messageFactory;
        this.cache = cache;
        this.timestampProvider = tp;
        sentenceQueue = new LinkedBlockingQueue<>();
        started = new AtomicBoolean(false);
        threadPool = new MyThreadPool(THREADS_POOL, -1);
        threadPool.start();
        timer = null;
    }

    private void onTimerHR() {
        synchronized (agents) {
            if (timerCount % 4 == 0) notifyDiagnostic();
            timerCount = (timerCount + 1) % TIMER_FACTOR;
            agents.values().forEach((NMEAAgent a) -> {
                stats.onTimerHR();
                runMe(a::onTimerHR, "timerHR", AGENT_KEY_NAME, a.getName());
                if (timerCount == 0) {
                    stats.onTimer();
                    runMe(a::onTimer, "timer", AGENT_KEY_NAME, a.getName());
                    dumpStats();
                }
            });
        }
    }

    private void runMe(Runnable r, String logOp, String logKey, String logValue) {
        try {
            threadPool.exec(r);
            stats.onExec();
        } catch (Exception e) {
            log.error(LogStringBuilder.start(ROUTER_CATEGORY).wO(logOp).wV(logKey, logValue).toString(), e);
        }
    }

    private void dumpStats() {
        long t = timestampProvider.getNow();
        if (t - lastStatsTime >= (STATS_PERIOD * 1000)) {
            lastStatsTime = t;
            long[] gc = Utils.printGCStats();
            LogStringBuilder lb = LogStringBuilder.start(ROUTER_CATEGORY).wO("stats").
                    wV("queue", sentenceQueue.size()).
                    wV("mem", Runtime.getRuntime().freeMemory()).
                    wV("gc", gc[0] - lastGC[0]).
                    wV("gcTime", gc[1] - lastGC[1]);
            lb = stats.dump(lb);
            log.info(lb.toString());
            lastGC[0] = gc[0];
            lastGC[1] = gc[1];
            stats.reset();
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
                        routeMessage(sentenceQueue.take());
                    } catch (InterruptedException e1) {
                        log.error(() -> LogStringBuilder.start(ROUTER_CATEGORY).wO("route loop").toString(), e1);
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }
    }

    @Override
    public void stop() {
        synchronized (agents) {
            for (NMEAAgent agent: agents.values()) {
                agent.stop();
            }
        }
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
        stats.onReceivedMessage();
        try {
            if (isStarted()) sentenceQueue.put(s);
        } catch (InterruptedException e1) {
            Thread.currentThread().interrupt();
            log.errorForceStacktrace("Thread error", e1);
        }
    }

    private void routeMessage(RouterMessage routerMessage) {
        if (started.get()) {
            if (routerMessage.getPayload() != null) {
                Message s = routerMessage.getPayload();
                cache.onSentence(s, routerMessage.getAgentSource());
            }
            dispatchToTargets(routerMessage);
        }
    }

    private void dispatchToTargets(final RouterMessage mm) {
        synchronized (agents) {
            stats.onDispatchedMessage();
            for (NMEAAgent nmeaAgent : agents.values()) {
                try {
                    if (nmeaAgent.isStarted()) {
                        final NMEATarget target = nmeaAgent.getTarget();
                        final String name = nmeaAgent.getName();
                        if (target != null) {
                            runMe(() -> dispatchToTarget(name, target, mm), "dispatch message", "messages", "" + 1);
                        }
                    }
                } catch (Exception e) {
                    log.error(LogStringBuilder.start(ROUTER_CATEGORY).wO("dispatch message").toString(), e);
                }
            }
        }
    }

    private void dispatchToTarget(String targetName, NMEATarget targetInterface, RouterMessage m) {
        if (!m.getAgentSource().equals(targetName)) {
            try {
                targetInterface.pushMessage(m);
            } catch (Exception e) {
                log.errorForceStacktrace("Unhandled exception dispatching to agents", e);
            }
        }
    }
}
