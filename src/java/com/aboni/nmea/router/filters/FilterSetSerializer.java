package com.aboni.nmea.router.filters;

public interface FilterSetSerializer {
    String exportFilter(NMEASentenceFilterSet s);

    NMEASentenceFilterSet importFilter(String jsonFilter);
}
