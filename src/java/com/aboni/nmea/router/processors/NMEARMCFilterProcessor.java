package com.aboni.nmea.router.processors;

import com.aboni.nmea.router.filters.impl.NMEAPositionFilter;
import com.aboni.utils.Pair;
import com.aboni.utils.ServerLog;
import net.sf.marineapi.nmea.sentence.Sentence;

import javax.inject.Inject;

public class NMEARMCFilterProcessor implements NMEAPostProcess {

    private final NMEAPositionFilter filter;

    @Inject
    public NMEARMCFilterProcessor() {
        filter = new NMEAPositionFilter();
    }

    private static final Pair<Boolean, Sentence[]> OK = new Pair<>(true, null);
    private static final Pair<Boolean, Sentence[]> KO = new Pair<>(false, null);

    @Override
    public Pair<Boolean, Sentence[]> process(Sentence sentence, String src) {
        try {
            return (filter.match(sentence, src) ? OK : KO);
		} catch (Exception e) {
            ServerLog.getLogger().warning("NMEARMCFilterProcessor: Cannot analyze RMC {" + sentence + "} error {" + e.getMessage() + "}");
            return KO;
		}
	}

	private int i = 0;

	@Override
	public void onTimer() {
		i = (i+1) % 60;
		if (i==0) {
			filter.dumpStats();
		}
	}
	
}