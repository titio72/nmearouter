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

package com.aboni.nmea.router.n2k.messages.impl;

import com.aboni.nmea.router.message.MsgPressure;
import com.aboni.nmea.router.message.PressureSource;
import com.aboni.nmea.router.message.impl.MsgPressureImpl;
import com.aboni.nmea.router.n2k.N2KMessageHeader;
import com.aboni.nmea.router.n2k.PGNDataParseException;
import com.aboni.utils.Utils;

import static com.aboni.nmea.router.n2k.messages.N2KMessagePGNs.ENVIRONMENT_PRESSURE_PGN;

public class N2KPressureImpl extends N2KMessageImpl implements MsgPressure {

    private MsgPressure pressureData;
    private int instance;
    private int sid;

    public N2KPressureImpl(byte[] data) {
        super(getDefaultHeader(ENVIRONMENT_PRESSURE_PGN), data);
        fill();
    }

    public N2KPressureImpl(N2KMessageHeader header, byte[] data) throws PGNDataParseException {
        super(header, data);
        if (header == null) throw new PGNDataParseException("Null message header!");
        if (header.getPgn() != ENVIRONMENT_PRESSURE_PGN)
            throw new PGNDataParseException(String.format("Incompatible header: expected %d, received %d", ENVIRONMENT_PRESSURE_PGN, header.getPgn()));
        fill();
    }

    private void fill() {

        sid = BitUtils.getByte(data, 0, 0xFF);
        instance = BitUtils.getByte(data, 1, 0xFF);

        PressureSource source = PressureSource.valueOf(BitUtils.getByte(data, 2, 0));

        Double dT = BitUtils.parseDouble(data, 24, 32, 0.1, false);
        double pressure = (dT == null) ? Double.NaN : Utils.round(dT / 100.0, 1);

        pressureData = new MsgPressureImpl(source, pressure);
    }

    @Override
    public int getSID() {
        return sid;
    }

    public int getInstance() {
        return instance;
    }

    @Override
    public PressureSource getPressureSource() {
        return pressureData.getPressureSource();
    }

    @Override
    public double getPressure() {
        return pressureData.getPressure();
    }

    @Override
    public String toString() {
        return String.format("PGN {%s} Source {%d} Instance {%d} PressureSource {%s} Pressure {%.1f}",
                ENVIRONMENT_PRESSURE_PGN, getHeader().getSource(), getInstance(), getPressureSource(), getPressure());
    }
}
