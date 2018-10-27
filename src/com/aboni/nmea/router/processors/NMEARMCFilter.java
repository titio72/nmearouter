package com.aboni.nmea.router.processors;

import com.aboni.nmea.router.filters.NMEAPositionFilter;
import com.aboni.utils.Pair;
import com.aboni.utils.ServerLog;

import net.sf.marineapi.nmea.sentence.Sentence;

/**
 * Used to produce a VTG sentence from a RMC to match requirement of NKE
 * @author aboni
 */
public class NMEARMCFilter implements NMEAPostProcess {
	
    private NMEAPositionFilter filter;
    
	public NMEARMCFilter() {
		filter = new NMEAPositionFilter();
	}

	private static Pair<Boolean, Sentence[]> OK = new Pair<>(true, null);
	private static Pair<Boolean, Sentence[]> KO = new Pair<>(false, null);
	
	@Override
	public Pair<Boolean, Sentence[]> process(Sentence sentence, String src) {
		try {
			return (filter.match(sentence, src)?OK:KO);
		} catch (Exception e) {
            ServerLog.getLogger().Warning("Cannot anayze RMC {" + sentence + "} error {" + e.getMessage() + "}");
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
