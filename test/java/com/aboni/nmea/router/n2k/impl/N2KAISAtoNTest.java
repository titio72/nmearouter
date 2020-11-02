package com.aboni.nmea.router.n2k.impl;

import com.aboni.nmea.router.n2k.messages.impl.N2KAISAtoN;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class N2KAISAtoNTest {

    @Test
    public void testMeloria() {
        N2KAISAtoN aton = new N2KAISAtoN(SECCHE_MEL);
        assertEquals("MPA SECCHE DELLA MEL  IA-D", aton.getName());
        assertEquals("Floating AtoN: special mark", aton.getAtoNType());
        assertEquals(43.5349998, aton.getGPSInfo().getPosition().getLatitude(), 0.0000001);
        assertEquals(10.2388334, aton.getGPSInfo().getPosition().getLongitude(), 0.0000001);
    }

    private static byte[] SECCHE_MEL = new byte[] {
            (byte)0x55, (byte)0x6c, (byte)0xe8, (byte)0x27, (byte)0x3b,
            (byte)0x6e, (byte)0x52, (byte)0x1a, (byte)0x06, (byte)0xee,
            (byte)0xe9, (byte)0xf2, (byte)0x19, (byte)0xfc, (byte)0x00,
            (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
            (byte)0xff, (byte)0xff, (byte)0x5e, (byte)0xe0, (byte)0xff,
            (byte)0xe0, (byte)0x1c, (byte)0x01, (byte)0x4d, (byte)0x50,
            (byte)0x41, (byte)0x20, (byte)0x53, (byte)0x45, (byte)0x43,
            (byte)0x43, (byte)0x48, (byte)0x45, (byte)0x20, (byte)0x44,
            (byte)0x45, (byte)0x4c, (byte)0x4c, (byte)0x41, (byte)0x20,
            (byte)0x4d, (byte)0x45, (byte)0x4c, (byte)0x20, (byte)0x20,
            (byte)0x49, (byte)0x41, (byte)0x2d, (byte)0x44, (byte)0xff

    };
}