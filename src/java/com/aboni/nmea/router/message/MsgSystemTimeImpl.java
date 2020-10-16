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

import java.time.Instant;

public class MsgSystemTimeImpl implements MsgSystemTime {

    private final String type;
    private final Instant time;

    public MsgSystemTimeImpl(String src, Instant time) {
        this.type = src;
        this.time = time;
    }

    @Override
    public int getSID() {
        return -1;
    }

    @Override
    public Instant getTime() {
        return time;
    }

    @Override
    public String getTimeSourceType() {
        return type;
    }

    @Override
    public String toString() {
        return String.format("Time: time {%s} Source {%s}", getTime(), getTimeSourceType());
    }
}
