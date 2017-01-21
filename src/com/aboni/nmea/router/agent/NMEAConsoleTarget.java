package com.aboni.nmea.router.agent;

import java.text.DateFormat;
import java.util.Date;

import com.aboni.nmea.router.impl.NMEAAgentImpl;

import net.sf.marineapi.nmea.sentence.Sentence;

public class NMEAConsoleTarget extends NMEAAgentImpl {

	public NMEAConsoleTarget(String name) {
		super(name);
	      setSourceTarget(false, true);
	}
	
	@Override
	protected void doWithSentence(Sentence s, NMEAAgent src) {
		System.out.println(
				DateFormat.getTimeInstance(DateFormat.MEDIUM).format(new Date()) + 
				" [" + getName() + "] [" + src.getName() + "] " + s);
	}
}
