package com.aboni.nmea.router.n2k.impl;

import com.aboni.nmea.router.n2k.PGNDataParseException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class N2KWindDataTest {

    @Test
    public void testWindOk() throws PGNDataParseException {
        N2KWindData w = new N2KWindData(new byte[]{(byte) 0xb1, (byte) 0x5c, (byte) 0x00, (byte) 0xee, (byte) 0xf0, (byte) 0xfa, (byte) 0xff, (byte) 0xff});
        assertEquals(353.4, w.getAngle(), 0.1);
        assertEquals(1.79, w.getSpeed(), 0.01);
        assertEquals(177, w.getSID());
        assertTrue(w.isApparent());
    }
}