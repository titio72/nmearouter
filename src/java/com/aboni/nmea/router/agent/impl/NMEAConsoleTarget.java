package com.aboni.nmea.router.agent.impl;

import com.aboni.nmea.router.NMEACache;
import com.aboni.nmea.router.OnSentence;
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

    @OnSentence
    public void onSentence(Sentence s, String src) {
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

    @Override
    public String toString() {
        return getType();
    }
}
