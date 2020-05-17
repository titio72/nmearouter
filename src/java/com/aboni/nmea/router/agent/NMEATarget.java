package com.aboni.nmea.router.agent;

import com.aboni.nmea.router.NMEAFilterable;
import com.aboni.nmea.router.RouterMessage;

public interface NMEATarget extends NMEAFilterable {

    void pushMessage(RouterMessage e);
}