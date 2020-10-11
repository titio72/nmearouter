package com.aboni.nmea.router.nmea0183.impl;

import com.aboni.nmea.router.message.MsgGNSSPosition;
import com.aboni.nmea.router.message.MsgSOGAdCOG;
import com.aboni.nmea.router.nmea0183.NMEA0183Message;
import com.aboni.nmea.sentences.NMEATimestampExtractor;
import net.sf.marineapi.nmea.parser.DataNotAvailableException;
import net.sf.marineapi.nmea.sentence.RMCSentence;
import net.sf.marineapi.nmea.util.Position;

import javax.validation.constraints.NotNull;
import java.time.Instant;

public class NMEA0183RMCMessage extends NMEA0183Message implements MsgGNSSPosition, MsgSOGAdCOG {

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
    public boolean isValidSID() {
        return false;
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
    public double getAltitude() {
        return Double.NaN;
    }

    @Override
    public String getGnssType() {
        return "GPS";
    }

    @Override
    public String getMethod() {
        return null;
    }

    @Override
    public String getIntegrity() {
        return null;
    }

    @Override
    public int getNSatellites() {
        return 0;
    }

    @Override
    public double getHDOP() {
        return 0;
    }

    @Override
    public boolean isHDOP() {
        return false;
    }

    @Override
    public double getPDOP() {
        return 0;
    }

    @Override
    public boolean isPDOP() {
        return false;
    }

    @Override
    public double getGeoidalSeparation() {
        return 0;
    }

    @Override
    public int getReferenceStations() {
        return 0;
    }

    @Override
    public String getReferenceStationType() {
        return null;
    }

    @Override
    public int getReferenceStationId() {
        return 0;
    }

    @Override
    public double getAgeOfDgnssCorrections() {
        return 0;
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
