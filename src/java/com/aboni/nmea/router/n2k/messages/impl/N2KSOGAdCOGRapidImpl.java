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

import com.aboni.nmea.router.message.MsgSOGAdCOG;
import com.aboni.nmea.router.message.impl.MsgSOGAndCOGImpl;
import com.aboni.nmea.router.n2k.N2KLookupTables;
import com.aboni.nmea.router.n2k.N2KMessageHeader;
import com.aboni.nmea.router.n2k.PGNDataParseException;
import com.aboni.utils.Utils;
import org.json.JSONObject;

import static com.aboni.nmea.router.n2k.N2KLookupTables.LOOKUP_MAPS.DIRECTION_REFERENCE;
import static com.aboni.nmea.router.n2k.messages.N2KMessagePGNs.SOG_COG_RAPID_PGN;

public class N2KSOGAdCOGRapidImpl extends N2KMessageImpl implements MsgSOGAdCOG {

    private final MsgSOGAdCOG sogAndCog;

    public N2KSOGAdCOGRapidImpl(byte[] data) {
        super(getDefaultHeader(SOG_COG_RAPID_PGN), data);
        sogAndCog = fill(data);
    }

    public N2KSOGAdCOGRapidImpl(N2KMessageHeader header, byte[] data) throws PGNDataParseException {
        super(header, data);
        if (header == null) throw new PGNDataParseException("Null message header!");
        if (header.getPgn() != SOG_COG_RAPID_PGN)
            throw new PGNDataParseException(String.format("Incompatible header: expected %d, received %d", SOG_COG_RAPID_PGN, header.getPgn()));
        sogAndCog = fill(data);
    }

    private static MsgSOGAdCOG fill(byte[] data) {
        int sid = N2KBitUtils.getByte(data, 0, 0xFF);

        Double dCog = N2KBitUtils.parseDouble(data, 16, 16, 0.0001, false);
        double cog = dCog == null ? Double.NaN : Utils.round(Math.toDegrees(dCog), 1);

        Double dSog = N2KBitUtils.parseDouble(data, 32, 16, 0.01, false);
        double sog = dSog == null ? Double.NaN : Utils.round(dSog * 3600.0 / 1852.0, 2);

        String cogReference = N2KBitUtils.parseEnum(data, 8, 0, 2, N2KLookupTables.getTable(DIRECTION_REFERENCE));

        return new MsgSOGAndCOGImpl(sid, sog, cog, cogReference);
    }

    @Override
    public int getSID() {
        return sogAndCog.getSID();
    }

    @Override
    public double getSOG() {
        return sogAndCog.getSOG();
    }

    @Override
    public double getCOG() {
        return sogAndCog.getCOG();
    }

    @Override
    public String getCOGReference() {
        return sogAndCog.getCOGReference();
    }

    @Override
    public boolean isTrueCOG() {
        return sogAndCog.isTrueCOG();
    }

    @Override
    public String toString() {
        return String.format("PGN {%s} Source {%d} SOG {%.1f} COG {%.1f} Ref {%s}",
                SOG_COG_RAPID_PGN, getHeader().getSource(), getSOG(), getCOG(), getCOGReference());
    }

    @Override
    public JSONObject toJSON() {
        return sogAndCog.toJSON();
    }
}
