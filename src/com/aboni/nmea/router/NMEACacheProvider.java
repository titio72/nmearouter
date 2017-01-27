package com.aboni.nmea.router;

import com.aboni.nmea.router.impl.NMEACacheImpl;

public class NMEACacheProvider {

	private NMEACacheProvider() {}
	
	private static NMEACacheImpl cache = new NMEACacheImpl();
	static {
		cache.start();
	}
	
	public static synchronized NMEACache getCache() {
		return cache;
	}
	
}
