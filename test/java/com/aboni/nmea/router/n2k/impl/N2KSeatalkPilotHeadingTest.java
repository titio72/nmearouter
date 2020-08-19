package com.aboni.nmea.router.n2k.impl;

import com.aboni.nmea.router.n2k.messages.N2KSeatalkPilotHeading;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class N2KSeatalkPilotHeadingTest {

    @Test
    public void test() {
        N2KSeatalkPilotHeading h = new N2KSeatalkPilotHeadingImpl(new byte[]{(byte) 0x3b, (byte) 0x9f, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0x3b, (byte) 0x0c, (byte) 0xff});
        assertEquals(17.9, h.getHeadingMagnetic(), 0.01);
        assertTrue(Double.isNaN(h.getHeadingTrue()));
    }

}