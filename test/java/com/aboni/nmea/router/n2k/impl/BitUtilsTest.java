package com.aboni.nmea.router.n2k.impl;

import com.aboni.nmea.router.n2k.messages.impl.BitUtils;
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

    @Test
    public void test1() {
        assertEquals(0xb1, BitUtils.getByte(b, 0));
        assertEquals(BitUtils.extractBits(b, 0, 0, 16, false).v, BitUtils.get2ByteInt(b, 0));
        assertEquals(BitUtils.extractBits(b, 0, 0, 24, false).v, BitUtils.get3ByteInt(b, 0));
        assertEquals(BitUtils.extractBits(b, 0, 0, 32, false).v, BitUtils.get4ByteInt(b, 0));
    }

    @Test
    public void test2BytesUnsigned() {
        assertEquals(0x005c, BitUtils.get2ByteInt(b, 1));
        assertEquals(0xf0ee, BitUtils.get2ByteInt(b, 3));
        assertEquals(BitUtils.extractBits(b, 0, 24, 16, false).v, BitUtils.get2ByteInt(b, 3));
    }

    @Test
    public void test3BytesUnsigned() {
        assertEquals(0xee005c, BitUtils.get3ByteInt(b, 1));
    }

    @Test
    public void test4BytesUnsigned() {
        assertEquals(0xf0ee005cL, BitUtils.get4ByteInt(b, 1));
    }
}