package com.aboni.nmea.router.agent.impl.system;

import com.aboni.nmea.router.NMEACache;
import com.aboni.nmea.router.agent.NMEAAgent;
import com.aboni.nmea.router.agent.QOS;
import com.aboni.nmea.router.agent.impl.NMEAAgentImpl;
import net.sf.marineapi.nmea.sentence.Sentence;

public class NMEASystemTimeGPS extends NMEAAgentImpl {

	private SystemTimeChecker systemTimeCHecker;
	
	public NMEASystemTimeGPS(NMEACache cache, String name, QOS qos) {
		super(cache, name, qos);
		setSourceTarget(false, true);
		systemTimeCHecker = new SystemTimeChecker(cache);
	}

	@Override
	public String getType() {
		return "GPSTime";
	}
	
	@Override
    public String getDescription() {
    	return "Sync up system time with GPS UTC time feed";
    }
    

	@Override
	protected void doWithSentence(Sentence s, NMEAAgent src) {
		systemTimeCHecker.checkAndSetTime(s, src);
	}

	@Override
	protected boolean onActivate() {
		return true;
	}

	@Override
	protected void onDeactivate() {
	}

    @Override
    public boolean isUserCanStartAndStop() {
    	return false;
    }
}
