package com.aboni.nmea.router.agent;

import com.aboni.nmea.router.Filterable;

public interface NMEASource {

    Filterable getSourceFilter();

    void setSentenceListener(NMEASentenceListener listener);

    void unsetSentenceListener();

}