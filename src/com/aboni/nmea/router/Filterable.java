package com.aboni.nmea.router;

import java.util.Iterator;

import com.aboni.nmea.router.filters.NMEASentenceFilter;

public interface Filterable {
	
	Iterator<NMEASentenceFilter> getFilters();
	void addFilter(NMEASentenceFilter f);
	void dropFilter(NMEASentenceFilter f);
}
