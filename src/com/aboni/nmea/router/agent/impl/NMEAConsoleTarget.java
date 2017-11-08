package com.aboni.nmea.router.agent.impl;

import java.text.DateFormat;
import java.util.Date;

import com.aboni.nmea.router.NMEACache;
import com.aboni.nmea.router.NMEAStream;
import com.aboni.nmea.router.agent.NMEAAgent;
import com.aboni.nmea.router.agent.QOS;

import net.sf.marineapi.nmea.sentence.Sentence;

public class NMEAConsoleTarget extends NMEAAgentImpl {

	public NMEAConsoleTarget(NMEACache cache, NMEAStream stream, String name, QOS q) {
		super(cache, stream, name, q);
	    setSourceTarget(false, true);
	}
	
	@Override
	protected void doWithSentence(Sentence s, NMEAAgent src) {
		System.out.println(
				DateFormat.getTimeInstance(DateFormat.MEDIUM).format(new Date()) + 
				" [" + getName() + "] [" + src.getName() + "] " + s);
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
