package com.aboni.nmea.router.agent;

import com.aboni.nmea.router.NMEACache;
import com.aboni.nmea.router.NMEAStream;
import com.aboni.nmea.sentences.NMEA2JSONb;

import net.sf.marineapi.nmea.sentence.Sentence;

public class NMEA2JSONSocketTarget extends NMEASocketServer {

	private NMEA2JSONb js;
	
	public NMEA2JSONSocketTarget(NMEACache cache, NMEAStream stream, String name, int port, QOS q) {
		super(cache, stream, name, port, q);
		setSourceTarget(true, true);
		js = new NMEA2JSONb();
	}

	@Override
	protected String getOutSentence(Sentence s) {
		return js.convert(s).toString();
	}
}
