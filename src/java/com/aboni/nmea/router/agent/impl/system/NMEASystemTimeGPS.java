package com.aboni.nmea.router.agent.impl.system;

import com.aboni.nmea.router.NMEACache;
import com.aboni.nmea.router.OnSentence;
import com.aboni.nmea.router.agent.impl.NMEAAgentImpl;
import net.sf.marineapi.nmea.sentence.Sentence;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;

public class NMEASystemTimeGPS extends NMEAAgentImpl {

    private final SystemTimeChecker systemTimeCHecker;

    @Inject
    public NMEASystemTimeGPS(@NotNull NMEACache cache) {
        super(cache);
        setSourceTarget(false, true);
        systemTimeCHecker = new SystemTimeChecker(cache);
    }

    @Override
    public String getType() {
        return "GPSTime";
    }

    @Override
    public String getDescription() {
        return "Sync up system time with GPS UTC time feed [" + (systemTimeCHecker.isSynced() ? "Sync " + systemTimeCHecker.getTimeSkew() : "Not Sync") + "]";
    }

    @OnSentence
    public void onSentence(Sentence s, String src) {
        systemTimeCHecker.checkAndSetTime(s);
    }

    @Override
    protected boolean onActivate() {
        return true;
    }
}
