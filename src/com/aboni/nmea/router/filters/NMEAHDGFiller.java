package com.aboni.nmea.router.filters;

import java.util.ArrayList;
import java.util.List;

import com.aboni.geo.NMEAMagnetic2TrueConverter;
import com.aboni.geo.Utils;
import com.aboni.nmea.router.NMEACache;
import com.aboni.utils.DataEvent;
import com.aboni.utils.ServerLog;

import net.sf.marineapi.nmea.parser.DataNotAvailableException;
import net.sf.marineapi.nmea.parser.SentenceFactory;
import net.sf.marineapi.nmea.sentence.HDGSentence;
import net.sf.marineapi.nmea.sentence.HDMSentence;
import net.sf.marineapi.nmea.sentence.HDTSentence;
import net.sf.marineapi.nmea.sentence.PositionSentence;
import net.sf.marineapi.nmea.sentence.Sentence;
import net.sf.marineapi.nmea.sentence.SentenceId;
import net.sf.marineapi.nmea.util.Position;

/**
 * Enrich HDG heading information:
 * 1) Listen to GPS location to set the magnetic variation into the HDG sentence (if not present)
 * 2) Split the sentence in HDM & HDT   
 * @author aboni
 *
 */
public class NMEAHDGFiller implements NMEAPostProcess {

    private NMEAMagnetic2TrueConverter m;
    private boolean doHDM;
    private boolean doHDT;
    private NMEACache cache;
    
    public NMEAHDGFiller(boolean createHDM, boolean createHDT, NMEACache cache) {
        m = new NMEAMagnetic2TrueConverter();
        doHDM = createHDM;
        doHDT = createHDT;
        this.cache = cache;
    }

    public NMEAHDGFiller(NMEACache cache) {
        this(false, false, cache);
    }

    @Override
    public Sentence[] process(Sentence sentence, String src) {
        try {
            
            if (sentence instanceof HDGSentence) {
                Position lastPosition = null;
                DataEvent<PositionSentence> ev = cache.getLastPosition();
                if (ev!=null) {
                    lastPosition = ev.data.getPosition(); 
                }
                
                boolean canDoT = false;
                List<Sentence> out = new ArrayList<Sentence>(3); 
                HDGSentence hdg = (HDGSentence)sentence;
                try {
                    hdg.getVariation();
                    canDoT = true;
                } catch (DataNotAvailableException e) {
                    if (lastPosition!=null) {
                        double d = m.getDeclination(lastPosition);
                        d = Utils.normalizeDegrees180_180(d);
                        hdg.setVariation(d);
                        canDoT = true;
                    } else {
                        //hdg.setVariation(0.0);
                    }
                }
                if (doHDM) {
                    HDMSentence hdm = (HDMSentence) SentenceFactory.getInstance().createParser(hdg.getTalkerId(), SentenceId.HDM);
                    hdm.setHeading(hdg.getHeading());
                    out.add(hdm);
                }
                if (doHDT && canDoT) {
                    HDTSentence hdt = (HDTSentence) SentenceFactory.getInstance().createParser(hdg.getTalkerId(), SentenceId.HDT);
                    double var = 0.0;
                    double dev = 0.0;
                    try { var = hdg.getVariation(); } catch (Exception e) {}
                    try { dev = hdg.getDeviation(); } catch (Exception e) {}
                    hdt.setHeading(hdg.getHeading() + var + dev);
                    out.add(hdt);
                }
                
                return (Sentence[]) out.toArray(new Sentence[0]);
            }
        } catch (Exception e) {
            ServerLog.getLogger().Error("Cannot process message!", e);
        }
        return null;
    }
    
}
