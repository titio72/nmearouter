package com.aboni.nmea.router.message;

import net.sf.marineapi.nmea.util.Position;

public interface MsgPosition extends Message {

    Position getPosition();
}
