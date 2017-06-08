package com.aboni.nmea.router.agent;

import com.aboni.nmea.router.NMEASentenceListener;
import com.aboni.nmea.router.filters.NMEASentenceFilterSet;

public interface NMEASource {

    NMEASentenceFilterSet getSourceFilter();

    void setSentenceListener(NMEASentenceListener listener);

    void unsetSentenceListener();

}