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

import com.aboni.nmea.router.message.DirectionReference;
import com.aboni.nmea.router.message.MsgSpeedAndHeading;
import com.aboni.nmea.router.nmea0183.NMEA0183Message;
import net.sf.marineapi.nmea.parser.DataNotAvailableException;
import net.sf.marineapi.nmea.sentence.VHWSentence;

public class NMEA0183VHWMessage extends NMEA0183Message implements MsgSpeedAndHeading {

    public NMEA0183VHWMessage(VHWSentence sentence) {
        super(sentence);
    }

    @Override
    public int getSID() {
        return -1;
    }

    @Override
    public double getHeading() {
        return ((VHWSentence)getSentence()).getHeading();
    }

    @Override
    public double getDeviation() {
        return Double.NaN;
    }

    @Override
    public double getVariation() {
        return Double.NaN;
    }

    @Override
    public DirectionReference getReference() {
        return ((VHWSentence)getSentence()).isTrue()?DirectionReference.TRUE:DirectionReference.MAGNETIC;
    }

    @Override
    public boolean isTrueHeading() {
        return false;
    }

    @Override
    public double getSpeedWaterRef() {
        try {
            return ((VHWSentence)getSentence()).getSpeedKnots();
        } catch (DataNotAvailableException e) {
            return Double.NaN;
        }
    }

    @Override
    public double getSpeedGroundRef() {
        return Double.NaN;
    }

    @Override
    public String getSpeedSensorType() {
        return "Paddle wheel";
    }

    @Override
    public int getSpeedDirection() {
        try {
            return (int)Math.round(((VHWSentence)getSentence()).getMagneticHeading());
        } catch (DataNotAvailableException e) {
            return 0;
        }
    }
}
