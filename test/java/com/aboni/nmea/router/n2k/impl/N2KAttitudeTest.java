package com.aboni.nmea.router.n2k.impl;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class N2KAttitudeTest {

    @Test
    public void testAttitudeOk() {
        N2KAttitude a = new N2KAttitude(new byte[]{(byte) 0xff, (byte) 0xea, (byte) 0x12, (byte) 0x1d, (byte) 0x01, (byte) 0x91, (byte) 0xfd, (byte) 0xff});

        assertEquals(27.7, a.getYaw(), 0.01);
        assertEquals(1.6, a.getPitch(), 0.01);
        assertEquals(-3.6, a.getRoll(), 0.01);
    }

}