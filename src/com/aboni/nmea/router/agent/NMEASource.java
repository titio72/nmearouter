package com.aboni.nmea.router.agent;

import com.aboni.nmea.router.NMEAFilterable;
import com.aboni.nmea.router.NMEASentenceListener;

public interface NMEASource extends NMEAFilterable {
    
	void setSentenceListener(NMEASentenceListener listener);
    void unsetSentenceListener();
}