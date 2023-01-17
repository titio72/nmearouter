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

import com.aboni.nmea.router.message.HumiditySource;
import com.aboni.nmea.router.message.MsgHumidity;
import com.aboni.nmea.router.nmea0183.NMEA0183Message;
import net.sf.marineapi.nmea.parser.DataNotAvailableException;
import net.sf.marineapi.nmea.sentence.MHUSentence;

public class NMEA0183MHUMessage extends NMEA0183Message implements MsgHumidity {

    public NMEA0183MHUMessage(MHUSentence sentence) {
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
    public HumiditySource getHumiditySource() {
        return HumiditySource.INSIDE;
    }

    @Override
    public double getHumidity() {
        try {
            return ((MHUSentence) getSentence()).getRelativeHumidity();
        } catch (DataNotAvailableException e) {
            return Double.NaN;
        }
    }

    @Override
    public double getSetHumidity() {
        return Double.NaN;
    }
}
