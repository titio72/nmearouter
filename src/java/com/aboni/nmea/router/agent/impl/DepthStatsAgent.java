package com.aboni.nmea.router.agent.impl;

import com.aboni.nmea.router.NMEACache;
import com.aboni.nmea.router.agent.QOS;
import com.aboni.nmea.sentences.XDPParser;
import com.aboni.nmea.sentences.XDPSentence;
import net.sf.marineapi.nmea.parser.SentenceFactory;
import net.sf.marineapi.nmea.sentence.DPTSentence;
import net.sf.marineapi.nmea.sentence.Sentence;
import net.sf.marineapi.nmea.sentence.TalkerId;

import java.util.Deque;
import java.util.LinkedList;

public class DepthStatsAgent extends NMEAAgentImpl {

    private static class DepthT {
        int depth;
        long timestamp;
    }
    
    private final Deque<DepthT> queue;

    private int max = Integer.MIN_VALUE;
    private int min = Integer.MAX_VALUE;    
    
    private static final long DEFAULT_WINDOW = 60L * 60L * 1000L; // 1 hour

    public DepthStatsAgent(NMEACache cache, String name, QOS qos) {
        super(cache, name, qos);
        setSourceTarget(true, true);
        queue = new LinkedList<>();
    }

    @Override
    public String getType() {
        return "DepthStats";
    }

    @Override
	public String getDescription() {
		return "Calculates max and min depth over last hour period";
	}

    @Override
    protected void doWithSentence(Sentence s, String source) {
        if (s instanceof DPTSentence) {
            DepthT d = handleDepth(((DPTSentence) s).getDepth(), getCache().getNow());
            
            XDPSentence x = (XDPSentence)SentenceFactory.getInstance().createParser(TalkerId.P, XDPParser.NMEA_SENTENCE_TYPE);
            x.setDepth((float)d.depth/10f);
            if (min!=Integer.MAX_VALUE) x.setMinDepth1h((float)min/10f);
            if (min!=Integer.MIN_VALUE) x.setMaxDepth1h((float)max/10f);
            notify(x);
        }
    }

    /**
     * For testing purposes only
     * @param d The value of the depth
     * @param ts The timestamp (unix time) of the reading
     */
    @SuppressWarnings("unused")
    public void privatePushDepth(double d, long ts) {
        handleDepth(d, ts);
    }
    
    private DepthT handleDepth(double depth, long ts) {
        DepthT d = new DepthT();
        d.depth = (int)(depth*10f);
        d.timestamp = ts;
        
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
                    if ((d.timestamp -d0.timestamp)>DEFAULT_WINDOW) {
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
    
    private void calcMinMax() {
        synchronized (this) { 
            max = Integer.MIN_VALUE;
            min = Integer.MAX_VALUE;
            for (DepthT d : queue) {
                max = Math.max(d.depth, max);
                min = Math.min(d.depth, min);
            }
        }
    }

    @Override
    public boolean isUserCanStartAndStop() {
    	return true;
    }
    
}
