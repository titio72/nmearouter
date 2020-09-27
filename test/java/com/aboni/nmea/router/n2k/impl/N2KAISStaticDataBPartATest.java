package com.aboni.nmea.router.n2k.impl;

import com.aboni.nmea.router.n2k.messages.impl.N2KAISStaticDataBPartAImpl;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class N2KAISStaticDataBPartATest {

    byte[] data = new byte[]{(byte) 0x18, (byte) 0xc8, (byte) 0x53, (byte) 0xbc, (byte) 0x0e, (byte) 0x4e, (byte) 0x41, (byte) 0x53, (byte) 0x48,
            (byte) 0x4f, (byte) 0x52, (byte) 0x4e, (byte) 0x20, (byte) 0x20, (byte) 0x20, (byte) 0x20, (byte) 0x20, (byte) 0x20,
            (byte) 0x20, (byte) 0x20, (byte) 0x20, (byte) 0x20, (byte) 0x20, (byte) 0x20, (byte) 0x20, (byte) 0xff, (byte) 0xff};

    @Test
    public void test() {
        N2KAISStaticDataBPartAImpl m = new N2KAISStaticDataBPartAImpl(data);
        assertEquals("NASHORN", m.getName());
        assertEquals("247223240", m.getMMSI());
    }
}