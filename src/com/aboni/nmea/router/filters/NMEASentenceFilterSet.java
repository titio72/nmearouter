package com.aboni.nmea.router.filters;

import java.util.Iterator;

public interface NMEASentenceFilterSet extends NMEASentenceFilter {
	
	Iterator<NMEASentenceFilter> getFilters();
	void addFilter(NMEASentenceFilter f);
	void dropFilter(NMEASentenceFilter f);
}
