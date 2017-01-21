package com.aboni.nmea.router.agent;

import com.aboni.nmea.sentences.NMEA2JSON;

import net.sf.marineapi.nmea.sentence.Sentence;

public class NMEA2JSONSocketTarget extends NMEASocketTarget {

	private NMEA2JSON js;
	
	public NMEA2JSONSocketTarget(String name, int port) {
		super(name, port);
		setSourceTarget(false, true);
		js = new NMEA2JSON();
	}

	public NMEA2JSONSocketTarget(String name) {
		this(name, 1113);
	}
	
	@Override
	protected String getOutSentence(Sentence s) {
		return js.convert(s);
	}
}
