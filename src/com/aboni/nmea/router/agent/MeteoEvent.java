package com.aboni.nmea.router.agent;

import com.aboni.utils.Serie;

public class MeteoEvent implements Event {

	private Serie serie;
	private long ts;
	
	public MeteoEvent(Serie s, long ts) {
		this.serie = s;
		this.ts = ts;
	}
	
	public Serie getSerie() {
		return serie;
	}
	
	@Override
	public long getTime() {
		return ts;
	}

}
