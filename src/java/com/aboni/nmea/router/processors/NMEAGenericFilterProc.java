package com.aboni.nmea.router.processors;

import com.aboni.nmea.sentences.NMEASentenceFilter;
import com.aboni.utils.Pair;
import com.aboni.utils.ServerLog;
import net.sf.marineapi.nmea.sentence.Sentence;

import javax.validation.constraints.NotNull;

public class NMEAGenericFilterProc implements NMEAPostProcess {

    private final NMEASentenceFilter filter;

    public NMEAGenericFilterProc(@NotNull NMEASentenceFilter filter) {
        this.filter = filter;
    }

    private static final Pair<Boolean, Sentence[]> OK = new Pair<>(true, null);
    private static final Pair<Boolean, Sentence[]> KO = new Pair<>(false, null);

    @Override
    public Pair<Boolean, Sentence[]> process(Sentence sentence, String src) {
        try {
            return (filter.match(sentence, src) ? OK : KO);
        } catch (Exception e) {
            ServerLog.getLogger().warning("Filter processor: Cannot analyze sentence {" + sentence + "} error {" + e.getMessage() + "}");
            return KO;
        }
    }

    @Override
    public void onTimer() {
        // nothing to do on timer
    }
}
