package com.aboni.toolkit;

import com.aboni.utils.MyThreadPool;
import com.aboni.utils.Utils;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class TestMyThreadPool {

    static final int THREADS = 4;
    static final int MAX_QUEUE_DELAY = 32;

    static final int MAX_QUEUE = 200;

    public static void main(String... args) {
        final AtomicInteger errors = new AtomicInteger();
        MyThreadPool pool = new MyThreadPool(THREADS, MAX_QUEUE, (Throwable t)-> errors.incrementAndGet());
        pool.start();
        int queued = 0;
        final Random r = new Random();
        long t0 = System.currentTimeMillis();
        while (pool.isStarted()) {
            final int i = r.nextInt(MAX_QUEUE_DELAY);
            Utils.pause(i);
            pool.exec(() -> {
                try {Thread.sleep(r.nextInt((MAX_QUEUE_DELAY - 2) * THREADS));} catch (Exception e) { e.printStackTrace();}
                if (r.nextInt(1000)>998) throw new RuntimeException("random error");
                //System.out.println("Job " + i);
            });
            queued++;
            long t1 = System.currentTimeMillis();
            if ((t1-t0)>1000) {
                System.out.print("(" + queued + ") [");
                MyThreadPool.Stats s = pool.getStats();
                for (int j = 0; j<s.getPoolSize(); j++) System.out.printf("%d ", s.getProcessed(j));
                System.out.printf(" - %d] %d %d Err %d\n", s.getTotProcessed(), s.getQueueSize(), s.getMaxQueueSize(), errors.get());
                t0 = t1;
            }
        }
    }

}
