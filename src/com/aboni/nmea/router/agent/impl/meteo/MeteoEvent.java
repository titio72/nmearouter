package com.aboni.nmea.router.agent.impl.meteo;

import com.aboni.utils.Serie;
import com.aboni.utils.db.Event;

public class MeteoEvent implements Event {

	private final Serie serie;
	private final long ts;
	
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
