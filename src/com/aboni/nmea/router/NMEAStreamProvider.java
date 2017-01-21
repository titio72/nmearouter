package com.aboni.nmea.router;

import com.aboni.nmea.router.impl.NMEAStreamImpl;

public class NMEAStreamProvider {

	private NMEAStreamProvider() {}

	private static NMEAStream stream;
	
	public synchronized static NMEAStream getStreamInstance() {
		if (stream==null) stream = new NMEAStreamImpl();
		return stream;
	}
	
}
