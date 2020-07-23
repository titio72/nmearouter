package com.aboni.nmea.router.filters;

import com.aboni.nmea.router.RouterMessage;

public interface NMEAFilter {

    boolean match(RouterMessage m);

}
