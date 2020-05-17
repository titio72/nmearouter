package com.aboni.utils;

public interface StatsWriter {
	void write(StatsSample s, long ts);
	void init();
	void dispose();
}
