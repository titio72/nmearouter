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

import com.aboni.nmea.router.message.MsgPressure;
import com.aboni.nmea.router.message.PressureSource;
import com.aboni.nmea.router.nmea0183.NMEA0183Message;
import net.sf.marineapi.nmea.parser.DataNotAvailableException;
import net.sf.marineapi.nmea.sentence.MMBSentence;

public class NMEA0183MMBMessage extends NMEA0183Message implements MsgPressure {

    public NMEA0183MMBMessage(MMBSentence sentence) {
        super(sentence);
    }

    @Override
    public int getSID() {
        return -1;
    }

    @Override
    public int getInstance() {
        return 0;
    }

    @Override
    public PressureSource getPressureSource() {
        return PressureSource.ATMOSPHERIC;
    }

    @Override
    public double getPressure() {
        try {
            return ((MMBSentence) getSentence()).getBars() * 1000.0;
        } catch (DataNotAvailableException e) {
            return Double.NaN;
        }
    }
}
