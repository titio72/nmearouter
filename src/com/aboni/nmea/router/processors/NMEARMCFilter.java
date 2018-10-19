package com.aboni.nmea.router.processors;

import com.aboni.nmea.router.agent.impl.track.PositionFilter;
import com.aboni.utils.Pair;
import com.aboni.utils.ServerLog;

import net.sf.marineapi.nmea.sentence.RMCSentence;
import net.sf.marineapi.nmea.sentence.Sentence;

/**
 * Used to produce a VTG sentence from a RMC to match requirement of NKE
 * @author aboni
 */
public class NMEARMCFilter implements NMEAPostProcess {
	/*
	RMC Recommended Minimum Navigation Information
	 12
	 1 2 3 4 5 6 7 8 9 10 11|
	 | | | | | | | | | | | |
	$--RMC,hhmmss.ss,A,llll.ll,a,yyyyy.yy,a,x.x,x.x,xxxx,x.x,a*hh
	 1) Time (UTC)
	 2) Status, V = Navigation receiver warning
	 3) Latitude
	 4) N or S
	 5) Longitude
	 6) E or W
	 7) Speed over ground, knots
	 8) Track made good, degrees true
	 9) Date, ddmmyy
	10) Magnetic Variation, degrees
	11) E or W
	12) Checksum
	*/
	
	
    private PositionFilter filter;
    
	public NMEARMCFilter() {
		filter = new PositionFilter();
	}

	private static Pair<Boolean, Sentence[]> OK = new Pair<>(true, null);
	private static Pair<Boolean, Sentence[]> KO = new Pair<>(false, null);
	
	@Override
	public Pair<Boolean, Sentence[]> process(Sentence sentence, String src) {
		try {
			if (sentence instanceof RMCSentence) {
				RMCSentence rmc = (RMCSentence)sentence;
				return new Pair<>(filter.acceptPoint(rmc), null);
			}
		} catch (Exception e) {
            ServerLog.getLogger().Warning("Cannot anayze RMC {" + sentence + "} error {" + e.getMessage() + "}");
            return KO;
		}
		return OK;
	}

	private int i = 0;

	@Override
	public void onTimer() {
		i = (i+1) % 10;
		if (i==0) {
			filter.dumpStats();
		}
	}
	
}
