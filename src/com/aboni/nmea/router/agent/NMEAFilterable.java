package com.aboni.nmea.router.agent;

import com.aboni.nmea.router.filters.NMEASentenceFilterSet;

public interface NMEAFilterable {
    NMEASentenceFilterSet getFilter();
    void setFilter(NMEASentenceFilterSet s);
}
