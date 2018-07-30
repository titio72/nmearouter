package com.aboni.nmea.router.filters;

import net.sf.marineapi.nmea.sentence.STALKSentence;
import net.sf.marineapi.nmea.sentence.Sentence;

public class STalkFilter implements NMEASentenceFilter {

	private String command = "84";
	private boolean negate = false; 
	
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
			return negate?(!b):b;
		}
		return false;
	}

}
