package com.aboni.nmea.router.agent.impl.system;

import com.aboni.nmea.router.NMEACache;
import com.aboni.nmea.router.agent.NMEAAgent;
import com.aboni.nmea.router.agent.QOS;
import com.aboni.nmea.router.agent.impl.NMEAAgentImpl;
import net.sf.marineapi.nmea.sentence.Sentence;

public class NMEASystemTimeGPS extends NMEAAgentImpl {

	private final SystemTimeChecker systemTimeCHecker;

	private final NMEACache cache;

	public NMEASystemTimeGPS(NMEACache cache, String name, QOS qos) {
		super(cache, name, qos);
		this.cache = cache;
		setSourceTarget(false, true);
		systemTimeCHecker = new SystemTimeChecker(cache);
	}

	@Override
	public String getType() {
		if (cache!=null) {
			return "GPSTime " + cache.isTimeSynced() + " " + cache.getTimeSkew();
		} else {
			return "GPSTime";
		}
	}
	
	@Override
    public String getDescription() {
    	return "Sync up system time with GPS UTC time feed";
    }
    

	@Override
	protected void doWithSentence(Sentence s, NMEAAgent src) {
		systemTimeCHecker.checkAndSetTime(s);
	}

	@Override
	protected boolean onActivate() {
		return true;
	}

    @Override
    public boolean isUserCanStartAndStop() {
    	return false;
    }
}
