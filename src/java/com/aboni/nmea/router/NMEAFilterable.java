package com.aboni.nmea.router;

import com.aboni.nmea.router.filters.NMEASentenceFilterSet;

public interface NMEAFilterable {
    NMEASentenceFilterSet getFilter();
    void setFilter(NMEASentenceFilterSet s);
}
