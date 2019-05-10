package com.aboni.nmea.router.agent.impl.meteo;

import com.aboni.utils.StatsSample;
import com.aboni.utils.db.Event;

public class MeteoEvent implements Event {

	private final StatsSample statsSample;
	private final long ts;
	
	public MeteoEvent(StatsSample s, long ts) {
		this.statsSample = s;
		this.ts = ts;
	}
	
	public StatsSample getStatsSample() {
		return statsSample;
	}
	
	@Override
	public long getTime() {
		return ts;
	}

}
