package com.aboni.nmea.router.n2k.impl;

import com.aboni.nmea.router.n2k.PGNDataParseException;
import com.aboni.nmea.router.n2k.messages.N2KSpeed;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class N2KSpeedTest {

    @Test
    public void testSpeedOk() throws PGNDataParseException {
        N2KSpeed s = new N2KSpeedImpl(new byte[]{(byte) 0x00, (byte) 0x7c, (byte) 0x00, (byte) 0xff, (byte) 0xff, (byte) 0x00, (byte) 0xff, (byte) 0xff});
        assertEquals(0, s.getSID());
        assertEquals(2.41, s.getSpeedWaterRef(), 0.01);
        assertTrue(Double.isNaN(s.getSpeedGroundRef()));
        assertEquals("Paddle wheel", s.getWaterRefType());
    }

}