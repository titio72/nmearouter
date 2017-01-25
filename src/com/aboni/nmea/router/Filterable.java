package com.aboni.nmea.router;

import java.util.Iterator;

import com.aboni.nmea.router.filters.NMEASentenceFilter;

import net.sf.marineapi.nmea.sentence.Sentence;

public interface Filterable {
	
	boolean accept(Sentence s, String soucre);
	
	Iterator<NMEASentenceFilter> getFilters();
	void addFilter(NMEASentenceFilter f);
	void dropFilter(NMEASentenceFilter f);
}
