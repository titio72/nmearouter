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

package com.aboni.nmea.router.message.impl;

import com.aboni.nmea.router.message.MsgWindData;

public class MsgWindDataImpl implements MsgWindData {

    private final int sid;
    private final double speed;
    private final double angle;
    private final boolean apparent;

    public MsgWindDataImpl(double speed, double angle, boolean apparent) {
        this.sid = -1;
        this.speed = speed;
        this.angle = angle;
        this.apparent = apparent;
    }

    public MsgWindDataImpl(int sid, double speed, double angle, boolean apparent) {
        this.sid = sid;
        this.speed = speed;
        this.angle = angle;
        this.apparent = apparent;
    }

    @Override
    public int getSID() {
        return sid;
    }

    @Override
    public double getSpeed() {
        return speed;
    }

    @Override
    public double getAngle() {
        return angle;
    }

    @Override
    public boolean isApparent() {
        return apparent;
    }

    @Override
    public String toString() {
        return String.format("Wind: Ref {%s} Speed {%.1f} Angle {%.1f}", isApparent() ? "Apparent" : "True", getSpeed(), getAngle());
    }
}
