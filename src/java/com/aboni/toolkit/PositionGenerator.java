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

package com.aboni.toolkit;

import com.aboni.geo.Course;
import com.aboni.nmea.router.message.MsgGNSSPosition;
import com.aboni.nmea.router.message.MsgPositionAndVector;
import com.aboni.nmea.router.message.MsgPositionAndVectorFacade;
import com.aboni.nmea.router.message.MsgSOGAdCOG;
import com.aboni.nmea.router.message.beans.MsgGNSSPositionImpl;
import com.aboni.nmea.router.message.beans.MsgSOGAndCOGImpl;
import net.sf.marineapi.nmea.util.Position;

import java.time.Instant;

public class PositionGenerator {

    private PositionGenerator() {
    }

    public static MsgPositionAndVector getMessage(Position p, double cog, double sog, long ts) {
        return new MsgPositionAndVectorFacade(
                new MsgGNSSPositionImpl(p, Instant.ofEpochMilli(ts), 12, 0.87, 1.1),
                new MsgSOGAndCOGImpl(sog, cog)
        );
    }

    public static MsgSOGAdCOG getMessage(double cog, double sog) {
        return new MsgSOGAndCOGImpl(sog, cog);
    }

    public static MsgGNSSPosition getMessage(Position p, long ts) {
        return new MsgGNSSPositionImpl(p, Instant.ofEpochMilli(ts), 12, 0.87, 1.1);
    }

    public static Position getNewPos(Position start, double cog, double distance) {
        Course course = new Course(start, cog, distance);
        return course.getP1();
    }
}
