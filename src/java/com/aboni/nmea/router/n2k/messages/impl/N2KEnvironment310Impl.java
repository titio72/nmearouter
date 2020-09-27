/*
(C) 2020, Andrea Boni
This file is part of NMEARouter.
NMEARouter is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.
NMEARouter is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.
You should have received a copy of the GNU General Public License
along with NMEARouter.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.aboni.nmea.router.n2k.messages.impl;

import com.aboni.misc.Utils;
import com.aboni.nmea.router.n2k.N2KMessageHeader;
import com.aboni.nmea.router.n2k.PGNDataParseException;
import com.aboni.nmea.router.n2k.messages.N2KEnvironment310;

import static com.aboni.nmea.router.n2k.messages.N2kMessagePGNs.ENVIRONMENT_130310_PGN;

public class N2KEnvironment310Impl extends N2KMessageImpl implements N2KEnvironment310 {

    private int sid;
    private double waterTemp;
    private double airTemp;
    private double atmosphericPressure;

    public N2KEnvironment310Impl(byte[] data) {
        super(getDefaultHeader(ENVIRONMENT_130310_PGN), data);
        fill();
    }

    public N2KEnvironment310Impl(N2KMessageHeader header, byte[] data) throws PGNDataParseException {
        super(header, data);
        if (header == null) throw new PGNDataParseException("Null message header!");
        if (header.getPgn() != ENVIRONMENT_130310_PGN)
            throw new PGNDataParseException(String.format("Incompatible header: expected %d, received %d", ENVIRONMENT_130310_PGN, header.getPgn()));
        fill();
    }

    private void fill() {
        sid = getByte(data, 0, 0xFF);

        Double dWT = parseDouble(data, 8, 16, 0.01, false);
        waterTemp = (dWT == null) ? Double.NaN : Utils.round(dWT - 273.15, 1);

        Double dAT = parseDouble(data, 24, 16, 0.01, false);
        airTemp = (dAT == null) ? Double.NaN : Utils.round(dAT - 273.15, 1);

        Long dP = parseInteger(data, 40, 16);
        atmosphericPressure = (dP == null) ? Double.NaN : Utils.round(dP / 100.0, 1);

    }

    @Override
    public int getSID() {
        return sid;
    }

    @Override
    public double getWaterTemp() {
        return waterTemp;
    }

    @Override
    public double getAirTemp() {
        return airTemp;
    }

    @Override
    public double getAtmosphericPressure() {
        return atmosphericPressure;
    }

    @Override
    public String toString() {
        return String.format("PGN {%s} Source {%d} Water Temp {%.1f} Air Temp {%.1f} Atmo Press. {%.1f}",
                ENVIRONMENT_130310_PGN, getHeader().getSource(), getWaterTemp(), getAirTemp(), getAtmosphericPressure());
    }
}
