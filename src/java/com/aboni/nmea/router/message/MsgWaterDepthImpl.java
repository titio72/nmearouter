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

package com.aboni.nmea.router.message;

public class MsgWaterDepthImpl implements MsgWaterDepth {

    private final int sid;
    private final double depth;
    private final double offset;
    private final double range;

    public MsgWaterDepthImpl(double depth, double offset) {
        this(-1, depth, offset, Double.NaN);
    }

    public MsgWaterDepthImpl(int sid, double depth, double offset, double range) {
        this.sid = sid;
        this.depth = depth;
        this.offset = offset;
        this.range = range;
    }

    @Override
    public int getSID() {
        return sid;
    }

    @Override
    public double getDepth() {
        return depth;
    }

    @Override
    public double getOffset() {
        return offset;
    }

    @Override
    public double getRange() {
        return range;
    }

    @Override
    public String toString() {
        return String.format("Depth: Depth {%.1f} Offset {%.1f}", getDepth(), getOffset());
    }

}
