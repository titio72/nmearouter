package com.aboni.nmea.router.n2k.impl;

import com.aboni.nmea.router.n2k.PGNDataParseException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class N2KWaterDepthTest {

    @Test
    public void testWaterDepthOk() throws PGNDataParseException {
        N2KWaterDepth d = new N2KWaterDepth(new byte[]{(byte) 0x00, (byte) 0x25, (byte) 0x1c, (byte) 0x00, (byte) 0x00, (byte) 0xc8, (byte) 0x00, (byte) 0xff});
        assertEquals(72.05, d.getDepth(), 0.01);
        assertEquals(0.200, d.getOffset(), 0.001);
        assertTrue(Double.isNaN(d.getRange()));
        assertEquals(0, d.getSID());
    }
}