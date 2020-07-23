package com.aboni.nmea.router.n2k.impl;

import org.junit.Test;

public class N2KAISPositionReportATest {

    private static final byte[] data = new byte[]{
            (byte) 0xc1, (byte) 0xb8, (byte) 0x68, (byte) 0xbc, (byte) 0x0e, (byte) 0x31, (byte) 0x95, (byte) 0xf7, (byte) 0x05,
            (byte) 0xde, (byte) 0x5d, (byte) 0xa9, (byte) 0x19, (byte) 0x98, (byte) 0x16, (byte) 0x13, (byte) 0x60, (byte) 0x03,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x68, (byte) 0x12, (byte) 0x00, (byte) 0x00, (byte) 0xf0, (byte) 0xfe};


    @Test
    public void test() {
        N2KAISPositionReportA p = new N2KAISPositionReportA(data);
        // TODO

    }

}