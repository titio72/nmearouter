package com.aboni.nmea.router.agent.impl.meteo;

import com.aboni.utils.Serie;

public interface StatsWriter {
	void write(Serie s,  long ts);
	boolean init();
	void dispose();
}
