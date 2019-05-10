package com.aboni.nmea.router.agent.impl.meteo;

import com.aboni.utils.StatsSample;

public interface StatsWriter {
	void write(StatsSample s, long ts);
	void init();
	void dispose();
}
