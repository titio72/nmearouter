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

import com.aboni.nmea.router.message.HumiditySource;
import com.aboni.nmea.router.message.MsgHumidity;
import com.aboni.nmea.router.message.impl.MsgHumidityImpl;
import com.aboni.nmea.router.n2k.N2KMessageHeader;
import com.aboni.nmea.router.n2k.PGNDataParseException;
import com.aboni.utils.Utils;

import static com.aboni.nmea.router.n2k.messages.N2KMessagePGNs.ENVIRONMENT_HUMIDITY_PGN;

public class N2KHumidityImpl extends N2KMessageImpl implements MsgHumidity {

    private final MsgHumidity msgHumidity;

    public N2KHumidityImpl(byte[] data) {
        super(getDefaultHeader(ENVIRONMENT_HUMIDITY_PGN), data);
        msgHumidity = fill(data);
    }

    public N2KHumidityImpl(N2KMessageHeader header, byte[] data) throws PGNDataParseException {
        super(header, data);
        if (header == null) throw new PGNDataParseException("Null message header!");
        if (header.getPgn() != ENVIRONMENT_HUMIDITY_PGN)
            throw new PGNDataParseException(String.format("Incompatible header: expected %d, received %d", ENVIRONMENT_HUMIDITY_PGN, header.getPgn()));
        msgHumidity = fill(data);
    }

    private static MsgHumidity fill(byte[] data) {

        int sid = N2KBitUtils.getByte(data, 0, 0xFF);
        int instance = N2KBitUtils.getByte(data, 1, 0xFF);

        HumiditySource source = HumiditySource.valueOf(N2KBitUtils.getByte(data, 2, 0));

        Double dT = N2KBitUtils.parseDouble(data, 24, 16, 0.004, true);
        double humidity = (dT == null) ? Double.NaN : Utils.round(dT, 1);

        Double dST = N2KBitUtils.parseDouble(data, 40, 16, 0.004, true);
        double setHumidity = (dST == null) ? Double.NaN : Utils.round(dST, 1);

        return new MsgHumidityImpl(sid, instance, source, humidity, setHumidity);
    }

    @Override
    public int getSID() {
        return msgHumidity.getSID();
    }

    public int getInstance() {
        return msgHumidity.getInstance();
    }

    @Override
    public HumiditySource getHumiditySource() {
        return msgHumidity.getHumiditySource();
    }

    @Override
    public double getHumidity() {
        return msgHumidity.getHumidity();
    }

    public double getSetHumidity() {
        return msgHumidity.getSetHumidity();
    }

    @Override
    public String toString() {
        return String.format("PGN {%s} Source {%d} Instance {%d} HumiditySource {%s} Humidity {%.1f} SetHumidity {%.1f}",
                ENVIRONMENT_HUMIDITY_PGN, getHeader().getSource(), getInstance(), getHumiditySource(), getHumidity(), getSetHumidity());
    }
}
