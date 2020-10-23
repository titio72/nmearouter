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

package com.aboni.nmea.router.message.beans;

import com.aboni.nmea.router.message.MsgSOGAdCOG;

public class MsgSOGAndCOGImpl implements MsgSOGAdCOG {

    private final double cog;
    private final double sog;
    private final String ref;
    private final int sid;

    public MsgSOGAndCOGImpl(double sog, double cog) {
        this(-1, sog, cog, "True");
    }

    public MsgSOGAndCOGImpl(int sid, double sog, double cog, String cogReference) {
        this.sid = sid;
        this.cog = cog;
        this.sog = sog;
        this.ref = cogReference;
    }

    @Override
    public int getSID() {
        return sid;
    }

    @Override
    public double getSOG() {
        return sog;
    }

    @Override
    public double getCOG() {
        return cog;
    }

    @Override
    public String getCOGReference() {
        return ref;
    }

    @Override
    public boolean isTrueCOG() {
        return "True".equals(ref);
    }

    @Override
    public String toString() {
        return String.format("SOG/COG: SOG {%.1f} COG {%.1f} COGRef {%s}", getSOG(), getCOG(), getCOGReference());
    }
}
