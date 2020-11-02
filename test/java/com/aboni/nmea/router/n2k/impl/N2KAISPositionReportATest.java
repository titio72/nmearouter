package com.aboni.nmea.router.n2k.impl;

import com.aboni.nmea.router.n2k.messages.impl.N2KAISPositionReportAImpl;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class N2KAISPositionReportATest {

    private static final byte[] data = new byte[]{
            (byte) 0xc1, (byte) 0xb8, (byte) 0x68, (byte) 0xbc, (byte) 0x0e, (byte) 0x31, (byte) 0x95, (byte) 0xf7, (byte) 0x05,
            (byte) 0xde, (byte) 0x5d, (byte) 0xa9, (byte) 0x19, (byte) 0x98, (byte) 0x16, (byte) 0x13, (byte) 0x60, (byte) 0x03,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x68, (byte) 0x12, (byte) 0x00, (byte) 0x00, (byte) 0xf0, (byte) 0xfe};


    @Test
    public void test() {
        N2KAISPositionReportAImpl p = new N2KAISPositionReportAImpl(data);
        System.out.println(p);
        assertEquals("247228600", p.getMMSI());
        assertEquals("A", p.getAISClass());
        assertEquals(43.0530014, p.getPosition().getLatitude(), 0.00001);
        assertEquals(10.0111665, p.getPosition().getLongitude(), 0.00001);
        assertEquals(27.0, p.getHeading(), 0.01);
        assertEquals(16.8, p.getSOG(), 0.01);
        assertEquals(28.0, p.getCOG(), 0.01);
        assertEquals("Under way using engine", p.getNavStatus());
    }

}