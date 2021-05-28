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

package com.aboni.nmea.router.processors;

import com.aboni.nmea.router.impl.DefaultTimestampProvider;
import com.aboni.nmea.router.message.Message;
import com.aboni.nmea.router.message.MsgSOGAdCOG;
import com.aboni.nmea.router.message.MsgWindData;
import com.aboni.nmea.router.message.impl.MsgSOGAndCOGImpl;
import com.aboni.nmea.router.message.impl.MsgWindDataImpl;
import com.aboni.utils.Pair;
import org.junit.Test;

import static org.junit.Assert.*;

public class NMEAMWVTrueTest {

    @Test
    public void test() throws NMEARouterProcessorException {
        MsgWindData msgWindData = new MsgWindDataImpl(10, 60, true);
        MsgSOGAdCOG msgSOGAdCOG = new MsgSOGAndCOGImpl(10, 0);
        NMEAMWVTrue processor = new NMEAMWVTrue(new DefaultTimestampProvider(), true);
        Pair<Boolean, Message[]> res = processor.process(msgSOGAdCOG, "test");
        assertTrue(res.first);
        assertNull(res.second);
        res = processor.process(msgWindData, "test");
        assertTrue(res.first);
        assertEquals(120.0, ((MsgWindData) res.second[0]).getAngle(), 0.01);
        assertEquals(10.0, ((MsgWindData) res.second[0]).getSpeed(), 0.01);
        assertTrue(((MsgWindData) res.second[0]).isTrue());
    }

    @Test
    public void testRun() throws NMEARouterProcessorException {
        MsgWindData msgWindData = new MsgWindDataImpl(10, 180, true);
        MsgSOGAdCOG msgSOGAdCOG = new MsgSOGAndCOGImpl(5, 0);
        NMEAMWVTrue processor = new NMEAMWVTrue(new DefaultTimestampProvider(), true);
        Pair<Boolean, Message[]> res = processor.process(msgSOGAdCOG, "test");
        assertTrue(res.first);
        assertNull(res.second);
        res = processor.process(msgWindData, "test");
        assertTrue(res.first);
        assertEquals(180.0, ((MsgWindData) res.second[0]).getAngle(), 0.01);
        assertEquals(15.0, ((MsgWindData) res.second[0]).getSpeed(), 0.01);
        assertTrue(((MsgWindData) res.second[0]).isTrue());
    }
}
