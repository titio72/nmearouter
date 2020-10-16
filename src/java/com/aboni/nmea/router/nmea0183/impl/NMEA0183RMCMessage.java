/*
 * Copyright (c) 2020,  Andrea Boni
 * This file is part of NMEARouter.
 * NMEARouter is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * NMEARouter is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with NMEARouter.  If not, see <http://www.gnu.org/licenses/>.
 */

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
