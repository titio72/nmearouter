package com.aboni.nmea.router.agent.impl;

import com.aboni.nmea.router.NMEACache;
import com.aboni.nmea.router.agent.QOS;
import com.aboni.utils.ServerLog;
import net.sf.marineapi.nmea.sentence.Sentence;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.text.DateFormat;
import java.util.Date;

public class NMEAConsoleTarget extends NMEAAgentImpl {

    @Inject
    public NMEAConsoleTarget(@NotNull NMEACache cache) {
        super(cache);
        setSourceTarget(false, true);
    }

    @Override
    protected final void onSetup(String name, QOS qos) {
        // do nothing
    }

    @Override
    protected void doWithSentence(Sentence s, String src) {
        ServerLog.getLogger().console(DateFormat.getTimeInstance(DateFormat.MEDIUM).format(new Date()) +
                " [" + src + "] " + s);
    }

    @Override
    public String getDescription() {
        return "Console monitor";
	}

    @Override
    public String getType() {
    	return "Console";
    }

}
