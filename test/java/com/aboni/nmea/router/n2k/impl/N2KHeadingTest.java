package com.aboni.nmea.router.n2k.impl;

import com.aboni.nmea.router.n2k.PGNDataParseException;
import com.aboni.nmea.router.message.MsgHeading;
import com.aboni.nmea.router.n2k.messages.impl.N2KHeadingImpl;
import org.junit.Test;

import static org.junit.Assert.*;

public class N2KHeadingTest {

    @Test
    public void testHeadingOk() throws PGNDataParseException {
        MsgHeading h = new N2KHeadingImpl(new byte[]{(byte) 0xff, (byte) 0xf7, (byte) 0x12, (byte) 0xff, (byte) 0x7f, (byte) 0xff, (byte) 0x7f, (byte) 0xfd});
        assertEquals(0xFF, h.getSID());
        assertEquals(27.8, h.getHeading(), 0.1);
        assertTrue(Double.isNaN(h.getVariation()));
        assertTrue(Double.isNaN(h.getDeviation()));
        assertEquals("Magnetic", h.getReference());
        assertFalse(h.isTrueHeading());
    }
}