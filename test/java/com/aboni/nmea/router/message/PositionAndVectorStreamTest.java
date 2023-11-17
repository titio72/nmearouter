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

import com.aboni.nmea.message.MsgPositionAndVector;
import com.aboni.nmea.router.message.PositionAndVectorStream;
import com.aboni.nmea.router.utils.PositionBuilder;
import com.aboni.utils.ProgrammableTimeStampProvider;
import net.sf.marineapi.nmea.util.Position;
import org.junit.Test;

import static org.junit.Assert.*;

public class PositionAndVectorStreamTest {

    private ProgrammableTimeStampProvider tp = new ProgrammableTimeStampProvider();
    private PositionAndVectorStream stream = new PositionAndVectorStream(tp);

    private static class MsgWrapper {
        MsgPositionAndVector pv;
    }

    @Test
    public void test() {
        final MsgWrapper w = new MsgWrapper();
        stream.setListener(msg -> w.pv = msg);
        tp.setTimestamp(0);
        stream.onMessage(PositionBuilder.getMessage(270, 5.4));
        tp.incrementBy(200);
        stream.onMessage(PositionBuilder.getMessage(new Position(43.123, 10.432), tp.getNow()));
        assertNotNull(w.pv);
        assertEquals(270, w.pv.getCOG(), 0.0001);
        assertEquals(5.4, w.pv.getSOG(), 0.0001);
        assertEquals(43.123, w.pv.getPosition().getLatitude(), 0.00001);
        assertEquals(10.432, w.pv.getPosition().getLongitude(), 0.00001);
    }

    @Test
    public void testPosAndVectorTooDistantInTime() {
        final MsgWrapper w = new MsgWrapper();
        stream.setListener(msg -> w.pv = msg);
        tp.setTimestamp(0);
        stream.onMessage(PositionBuilder.getMessage(270, 5.4));
        // let too much time pass to consider position and speed consistent
        tp.incrementBy(1500);
        stream.onMessage(PositionBuilder.getMessage(new Position(43.123, 10.432), tp.getNow()));
        assertNull(w.pv);
    }
}