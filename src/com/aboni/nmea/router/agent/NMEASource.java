package com.aboni.nmea.router.agent;

import com.aboni.nmea.router.NMEASentenceFilterSet;

public interface NMEASource {

    NMEASentenceFilterSet getSourceFilter();

    void setSentenceListener(NMEASentenceListener listener);

    void unsetSentenceListener();

}