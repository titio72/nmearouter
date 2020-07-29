package com.aboni.nmea.router.n2k.impl;

import org.junit.Test;

import static org.junit.Assert.*;

public class n2KAISStaticDataBPartBTest {

    @Test
    public void test() {
       N2KAISStaticDataBPartB pb = new N2KAISStaticDataBPartB(new byte[] {
               (byte)0x18,(byte)0x88,(byte)0x78,(byte)0xbe,(byte)0x0e,(byte)0x24,(byte)0x28,(byte)0xc8,(byte)0x22,(byte)0x94,
               (byte)0x0c,(byte)0x00,(byte)0x38,(byte)0x40,(byte)0x40,(byte)0x40,(byte)0x40,(byte)0x40,(byte)0x40,
               (byte)0x40,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,
               (byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0xeb});
        assertEquals("B", pb.getAISClass());
        assertEquals("", pb.getCallSign());
        assertEquals("247363720", pb.getMMSI());
        assertEquals("Sailing", pb.getTypeOfShip());

    }
    //2070-01-01-10:51:44.835,6,129810,0,255,33,18,88,78,be,0e,24,28,c8,22,94,0c,00,38,40,40,40,40,40,40,40,00,00,00,00,00,00,00,00,00,00,00,00,eb

}