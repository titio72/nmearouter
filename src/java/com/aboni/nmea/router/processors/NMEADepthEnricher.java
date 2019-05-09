package com.aboni.nmea.router.processors;

import com.aboni.utils.HWSettings;
import com.aboni.utils.Pair;
import com.aboni.utils.ServerLog;
import net.sf.marineapi.nmea.parser.SentenceFactory;
import net.sf.marineapi.nmea.sentence.DBTSentence;
import net.sf.marineapi.nmea.sentence.DPTSentence;
import net.sf.marineapi.nmea.sentence.Sentence;
import net.sf.marineapi.nmea.sentence.SentenceId;

/**
 * Enrich HDG heading information:
 * 1) Listen to GPS location to set the magnetic variation into the HDG sentence (if not present)
 * 2) Split the sentence in HDM & HDT   
 * @author aboni
 *
 */
public class NMEADepthEnricher implements NMEAPostProcess {

    @Override
    public Pair<Boolean, Sentence[]> process(Sentence sentence, String src) {
        try {
            if (sentence instanceof DBTSentence) {
            	double offset = HWSettings.getPropertyAsDouble("depth.offset", 0.0);
            	DBTSentence dbt = (DBTSentence)sentence;
            	DPTSentence dpt = (DPTSentence)SentenceFactory.getInstance().createParser(dbt.getTalkerId(), SentenceId.DPT);
            	dpt.setDepth(dbt.getDepth());
            	dpt.setOffset(offset);
                return new Pair<>(Boolean.TRUE, new Sentence[] {dpt});
            }
        } catch (Exception e) {
            ServerLog.getLogger().warning("Cannot enrich depth process message {" + sentence + "} erro {" + e.getLocalizedMessage() + "}");
        }
        return new Pair<>(Boolean.TRUE, null);
    }

	@Override
	public void onTimer() {
        // nothing to do
	}    
}
