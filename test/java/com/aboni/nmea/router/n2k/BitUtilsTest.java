package com.aboni.nmea.router.n2k;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class BitUtilsTest {

    private byte[] b = new byte[]{
            (byte) 0xb1, (byte) 0x5c, (byte) 0x00, (byte) 0xee,
            (byte) 0xf0, (byte) 0xfa, (byte) 0xff, (byte) 0xff};

    @Test
    public void extractShort() {
        long v = BitUtils.extractBits(b, 0, 8, 16, false).v;
        assertEquals(92, v);
    }

    @Test
    public void extractLessThanOneByte() {
        long v1 = BitUtils.extractBits(b, 0, 40, 3, false).v;
        assertEquals(2, v1);
    }

}