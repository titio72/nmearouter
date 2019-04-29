package com.aboni.nmea.router.filters;

import com.aboni.nmea.sentences.NMEASentenceFilter;
import net.sf.marineapi.nmea.sentence.STALKSentence;
import net.sf.marineapi.nmea.sentence.Sentence;

public class STalkFilter implements NMEASentenceFilter {

	private final String command;
	private final boolean negate;
	
	public STalkFilter(String command, boolean negate) {
		this.command = command;
		this.negate = negate;
	}
	
	public String getCOmmand() {
		return command;
	}
	
	public boolean isNegate() {
		return negate;
	}
	
	@Override
	public boolean match(Sentence s, String src) {
		if (s instanceof STALKSentence) {
			boolean b = command.equals(((STALKSentence)s).getCommand()); 
			return negate == (!b);
		}
		return false;
	}

}
