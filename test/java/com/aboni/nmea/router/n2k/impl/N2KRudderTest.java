package com.aboni.nmea.router.n2k.impl;

import com.aboni.nmea.router.n2k.PGNDataParseException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class N2KRudderTest {

    @Test
    public void testPosition() throws PGNDataParseException {
        N2KRudder r = new N2KRudder(new byte[]{(byte) 0x00, (byte) 0xff, (byte) 0xfe, (byte) 0x00, (byte) 0x65, (byte) 0x01, (byte) 0xff, (byte) 0xff});
        assertEquals(0, r.getInstance());
        assertEquals(2.0, r.getPosition(), 0.01);
        assertEquals(1.5, r.getAngleOrder(), 0.01);
        assertEquals(-1, r.getDirectionOrder());
    }

    @Test
    public void testOrder() throws PGNDataParseException {
        N2KRudder r = new N2KRudder(new byte[]{(byte) 0xfc, (byte) 0xf8, (byte) 0xfe, (byte) 0x00, (byte) 0xff, (byte) 0x7f, (byte) 0xff, (byte) 0xff});
        assertEquals(252, r.getInstance());
        assertEquals(0, r.getDirectionOrder(), 0.01);
        assertEquals(1.5, r.getAngleOrder(), 0.01);
        assertTrue(Double.isNaN(r.getPosition()));
    }
}