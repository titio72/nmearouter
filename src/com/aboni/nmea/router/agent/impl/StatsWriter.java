package com.aboni.nmea.router.agent.impl;

import com.aboni.utils.Serie;

public interface StatsWriter {
	void init();
	void write(Serie s,  long ts);
	void dispose();
}
