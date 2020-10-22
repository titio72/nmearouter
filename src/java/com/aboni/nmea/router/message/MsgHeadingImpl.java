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

import org.json.JSONObject;

public class MsgHeadingImpl implements MsgHeading {

    private final double heading;
    private final boolean magnetic;
    private final double variation;
    private final double deviation;
    private final int sid;

    public MsgHeadingImpl(double heading, boolean magnetic) {
        this.magnetic = magnetic;
        this.heading = heading;
        this.variation = Double.NaN;
        this.deviation = Double.NaN;
        this.sid = -1;
    }

    public MsgHeadingImpl(double heading, double variation, boolean magnetic) {
        this.magnetic = magnetic;
        this.heading = heading;
        this.variation = variation;
        this.deviation = Double.NaN;
        this.sid = -1;
    }

    public MsgHeadingImpl(int sid, double heading, double variation, double deviation, boolean magnetic) {
        this.magnetic = magnetic;
        this.heading = heading;
        this.variation = variation;
        this.deviation = deviation;
        this.sid = sid;
    }

    @Override
    public int getSID() {
        return sid;
    }

    @Override
    public double getHeading() {
        return heading;
    }

    @Override
    public double getDeviation() {
        return deviation;
    }

    @Override
    public double getVariation() {
        return variation;
    }

    @Override
    public DirectionReference getReference() {
        return magnetic ? DirectionReference.MAGNETIC : DirectionReference.TRUE;
    }

    @Override
    public boolean isTrueHeading() {
        return !magnetic;
    }

    @Override
    public String toString() {
        return String.format("Heading: Head {%.1f} Variation {%.1f} Ref {%s}", getHeading(), getVariation(), getReference());
    }

    @Override
    public JSONObject toJSON() {
        if (!Double.isNaN(getHeading())) {
            JSONObject json = new JSONObject();
            json.put("topic", DirectionReference.TRUE == getReference() ? "HDT" : "HDM");
            json.put("angle", getHeading());
            return json;
        } else {
            return null;
        }
    }
}
