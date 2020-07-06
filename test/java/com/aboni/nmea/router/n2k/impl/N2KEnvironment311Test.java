package com.aboni.nmea.router.n2k.impl;

import com.aboni.nmea.router.n2k.PGNDataParseException;
import org.junit.Test;

import static org.junit.Assert.*;

public class N2KEnvironment311Test {

    @Test
    public void testAll() throws PGNDataParseException {
        N2KEnvironment311 e = new N2KEnvironment311(new byte[]{(byte) 0x01, (byte) 0x02, (byte) 0xb7, (byte) 0x75, (byte) 0xb0, (byte) 0x36, (byte) 0xf6, (byte) 0x03});
        assertEquals("Inside", e.getHumiditySource());
        assertEquals("Inside Temperature", e.getTempSource());
        assertEquals(56.0, e.getHumidity(), 0.001);
        assertEquals(28.2, e.getTemperature(), 0.001);
        assertEquals(1014.0, e.getAtmosphericPressure(), 0.1);
    }

    @Test
    public void testNoPressure() throws PGNDataParseException {
        N2KEnvironment311 e = new N2KEnvironment311(new byte[]{(byte) 0x01, (byte) 0x02, (byte) 0xb7, (byte) 0x75, (byte) 0xb0, (byte) 0x36, (byte) 0xff, (byte) 0xff});
        assertEquals("Inside", e.getHumiditySource());
        assertEquals("Inside Temperature", e.getTempSource());
        assertEquals(56.0, e.getHumidity(), 0.001);
        assertEquals(28.2, e.getTemperature(), 0.001);
        assertTrue(Double.isNaN(e.getAtmosphericPressure()));
    }

    @Test
    public void testOnlyTemp() throws PGNDataParseException {
        N2KEnvironment311 e = new N2KEnvironment311(new byte[]{(byte) 0x01, (byte) 0xc2, (byte) 0xb7, (byte) 0x75, (byte) 0xff, (byte) 0x7f, (byte) 0xff, (byte) 0xff});
        assertNull(e.getHumiditySource());
        assertEquals("Inside Temperature", e.getTempSource());
        assertEquals(28.2, e.getTemperature(), 0.001);
        assertTrue(Double.isNaN(e.getAtmosphericPressure()));
        assertTrue(Double.isNaN(e.getHumidity()));
    }
}