package com.aboni.nmea.router.n2k.impl;

import com.aboni.nmea.router.n2k.messages.impl.N2KEnvironment310Impl;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class N2KEnvironment310Test {

    @Test
    public void test310Ok() {
        N2KEnvironment310Impl e = new N2KEnvironment310Impl(new byte[]{(byte) 0x00, (byte) 0x0f, (byte) 0x73, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff});
        assertEquals(21.4, e.getWaterTemp(), 0.001);
        assertTrue(Double.isNaN(e.getAirTemp()));
        assertTrue(Double.isNaN(e.getAtmosphericPressure()));
    }
}