package com.aboni.nmea.router.nmea0183.impl;

import com.aboni.nmea.router.message.MsgPositionAndVector;
import com.aboni.nmea.router.nmea0183.NMEA0183Message;
import com.aboni.nmea.sentences.NMEATimestampExtractor;
import net.sf.marineapi.nmea.parser.DataNotAvailableException;
import net.sf.marineapi.nmea.sentence.RMCSentence;
import net.sf.marineapi.nmea.util.CompassPoint;
import net.sf.marineapi.nmea.util.Position;

import javax.validation.constraints.NotNull;
import java.time.Instant;

public class NMEA0183RMCMessage extends NMEA0183Message implements MsgPositionAndVector {

    protected NMEA0183RMCMessage(@NotNull RMCSentence sentence) {
        super(sentence);
    }

    @Override
    public int getSID() {
        return -1;
    }

    private RMCSentence getRMC() {
        return (RMCSentence)getSentence();
    }

    @Override
    public double getSOG() {
        try {
            return getRMC().getSpeed();
        } catch (DataNotAvailableException e) {
            return Double.NaN;
        }
    }

    @Override
    public double getCOG() {
        try {
            return getRMC().getCourse();
        } catch (DataNotAvailableException e) {
            return Double.NaN;
        }
    }

    @Override
    public String getCOGReference() {
        return "True";
    }

    @Override
    public boolean isTrueCOG() {
        return true;
    }

    @Override
    public Instant getTimestamp() {
        try {
            return NMEATimestampExtractor.extractInstant(getRMC());
        } catch (NMEATimestampExtractor.GPSTimeException e) {
            return null;
        }
    }

    @Override
    public double getVariation() {
        try {
            if (getRMC().getDirectionOfVariation()== CompassPoint.EAST)
                return getRMC().getVariation();
            else
                return -getRMC().getVariation();
        } catch (DataNotAvailableException e) {
            return Double.NaN;
        }
    }

    @Override
    public Position getPosition() {
        try {
            return getRMC().getPosition();
        } catch (DataNotAvailableException e) {
            return null;
        }
    }
}
