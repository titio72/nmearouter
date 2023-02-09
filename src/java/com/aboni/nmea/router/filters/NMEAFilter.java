package com.aboni.nmea.router.filters;

import com.aboni.nmea.router.JSONable;
import com.aboni.nmea.router.RouterMessage;

public interface NMEAFilter extends JSONable {

    boolean match(RouterMessage m);

}
