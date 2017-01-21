package com.aboni.nmea.router.agent;

import com.aboni.nmea.router.Filterable;

public interface NMEASource {

    Filterable getOutputFilter();

    void setSentenceListener(NMEASentenceListener listener);

    void unsetSentenceListener();

}