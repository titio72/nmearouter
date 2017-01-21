package com.aboni.nmea.router.agent;

import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;

import com.aboni.nmea.router.impl.NMEAAgentImpl;
import com.aboni.nmea.sentences.XDPParser;
import com.aboni.nmea.sentences.XDPSentence;

import net.sf.marineapi.nmea.parser.SentenceFactory;
import net.sf.marineapi.nmea.sentence.DPTSentence;
import net.sf.marineapi.nmea.sentence.Sentence;
import net.sf.marineapi.nmea.sentence.TalkerId;

public class DepthStatsAgent extends NMEAAgentImpl {

    private class DepthT {
        int depth;
        long TS;
    }
    
    private Deque<DepthT> queue;

    private int max = Integer.MIN_VALUE;
    private int min = Integer.MAX_VALUE;    
    
    public DepthStatsAgent(String name) {
        this(name, null);
    }

    public DepthStatsAgent(String name, QOS qos) {
        super(name, qos);
        setSourceTarget(true, true);
        queue = new LinkedList<DepthStatsAgent.DepthT>();
    }

    @Override
    protected void doWithSentence(Sentence s, NMEAAgent source) {
        if (s instanceof DPTSentence) {
            DepthT d = handleDepth(((DPTSentence) s).getDepth(), System.currentTimeMillis());
            
            XDPSentence x = (XDPSentence)SentenceFactory.getInstance().createParser(TalkerId.P, XDPParser.NMEA_SENTENCE_TYPE);
            x.setDepth((float)d.depth/10f);
            if (min!=Integer.MAX_VALUE) x.setMinDepth1h((float)min/10f);
            if (min!=Integer.MIN_VALUE)x.setMaxDepth1h((float)max/10f);
            notify(x);
        }
    }

    /**
     * For testing purposes only
     * @param d
     * @param ts
     */
    public void _pushDepth(double d, long ts) {
        handleDepth(d, ts);
    }
    
    private DepthT handleDepth(double depth, long ts) {
        DepthT d = new DepthT();
        d.depth = (int)(depth*10f);
        d.TS = ts;
        
        if (depth>0.1) {
            queue.add(d);
    
            
            max = Math.max(d.depth, max);
            min = Math.min(d.depth, min);
    
            boolean dirty = false;
            boolean goon = true;
            synchronized (this) {
                while (goon) {
                    goon = false;
                    DepthT d0 = queue.getFirst();
                    if ((d.TS-d0.TS)>60*60*1000) {
                        DepthT dd = queue.pop();
                        dirty = dirty || (!(dd.depth<max && dd.depth>min));
                        goon = true;
                    }
                }
            }
    
            if (dirty) {
                calcMinMax();
            }
        }
        return d;
    }
    
    private boolean calcMinMax() {
        synchronized (this) { 
            int oldMax = max;
            int oldMin = min;
            max = Integer.MIN_VALUE;
            min = Integer.MAX_VALUE;
            for (Iterator<DepthT> i = queue.iterator(); i.hasNext(); ) {
                DepthT d = i.next(); 
                max = Math.max(d.depth, max);
                min = Math.min(d.depth, min);
            }
            return (oldMin!=min || oldMax!=max);
        }
    }
    
    public float getMaxDepth() {
        synchronized (this) {
            return (float)max/10f;
        }
    }

    public float getMinDepth() {
        synchronized (this) {
            return (float)min/10f;
        }
    }
    
}
