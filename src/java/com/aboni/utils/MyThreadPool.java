/*
 * Copyright (c) 2022,  Andrea Boni
 * This file is part of NMEARouter.
 * NMEARouter is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * NMEARouter is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with NMEARouter.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.aboni.utils;

import com.aboni.nmea.router.Startable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;


public class MyThreadPool implements Startable {

    public static class Stats {
        long[] processed;
        int queue;
        int maxQueue;

        public long getProcessed(int i) {
            if (i>=0 && i<processed.length)
                return processed[i];
            else
                return 0;
        }

        public int getPoolSize() {
            return processed.length;
        }

        public int getQueueSize() {
            return queue;
        }

        public int getMaxQueueSize() {
            return maxQueue;
        }

        public long getTotProcessed() {
            long t = 0;
            for (long p: processed) t+=p;
            return t;
        }

        Stats(int n) {
            processed = new long[n];
        }
    }

    private class Worker implements Runnable {

        String name;
        AtomicLong c = new AtomicLong();

        AtomicBoolean active = new AtomicBoolean(false);

        Thread myThread;

        Worker(String name) {
            this.name = name;
        }

        void start() {
            active.set(true);
            myThread = new Thread(this, name);
            myThread.setDaemon(true);
            myThread.start();
        }

        void stop() {
            active.set(false);
        }

        @Override
        public void run() {
            while (active.get()) {
                Runnable r;
                try {
                     r = q.take();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                try {
                    r.run();
                } catch (Throwable t) {
                    if (errorHandler!=null) errorHandler.onThrowable(t);
                }
                c.incrementAndGet();
            }
        }
    }

    public interface ErrorHandler {
        void onThrowable(Throwable t);
    }

    private final List<Worker> lw = new ArrayList<>();
    private final BlockingQueue<Runnable> q = new LinkedBlockingQueue<>();
    private final AtomicBoolean doRun = new AtomicBoolean(false);

    private final ErrorHandler errorHandler;

    private int maxQueueStat;

    private final int nThreads;
    private final int maxQueueSize;

    public MyThreadPool(int nThreads, int maxQueueSize) {
        this(nThreads, maxQueueSize, (Throwable t)->{});
    }

    public MyThreadPool(int nThreads, int maxQueueSize, ErrorHandler errorHandler) {
        this.maxQueueSize = maxQueueSize;
        this.nThreads = nThreads;
        this.errorHandler = errorHandler;
    }

    public void exec(Runnable m) {
        if (isStarted()) {
            int qs = q.size();
            if (maxQueueSize<=0 || qs <= maxQueueSize)
                q.add(m);
            else
                throw new RuntimeException("Queue full");
            if (maxQueueStat<qs) maxQueueStat = qs;
        } else
            throw new RuntimeException("Pool not started");
    }

    public int getQueueSize() {
        return q.size();
    }

    @Override
    public void start() {
        synchronized (this) {
            for (int i = 0; i<nThreads; i++) {
                lw.add(new Worker("W" + i));
            }
            doRun.set(true);
            for (Worker t : lw) t.start();
        }
    }

    @Override
    public void stop() {
        for (int i = 0; i<nThreads; i++) {
            lw.get(i).stop();
        }
        lw.clear();
        synchronized (this) {
            doRun.set(false);
        }
    }

    @Override
    public boolean isStarted() {
        synchronized (this) {
            return doRun.get();
        }
    }

    public Stats getStats() {
        Stats s = new Stats(lw.size());
        for (int i = 0; i<lw.size(); i++) {
            s.processed[i] = lw.get(i).c.get();
        }
        s.queue = q.size();
        s.maxQueue = maxQueueStat;
        return s;
    }
}