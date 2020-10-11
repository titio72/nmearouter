package com.aboni.nmea.router.nmea0183.impl;

import com.aboni.nmea.router.message.MsgPosition;
import com.aboni.nmea.router.nmea0183.NMEA0183Message;
import net.sf.marineapi.nmea.sentence.PositionSentence;
import net.sf.marineapi.nmea.util.Position;

import javax.validation.constraints.NotNull;

public class NMEA0813MsgPosition extends NMEA0183Message implements MsgPosition {

    public NMEA0813MsgPosition(@NotNull PositionSentence sentence) {
        super(sentence);
    }

    @Override
    public Position getPosition() {
        return ((PositionSentence)getSentence()).getPosition();
    }
}
