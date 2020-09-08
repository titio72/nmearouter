package com.aboni.nmea.router.n2k.impl;

import org.junit.Test;

import static org.junit.Assert.*;

public class N2KAISAtoNTest {



    private static final byte[] b = {(byte)0x55, (byte)0xdc, (byte)0xe7, (byte)0x27, (byte)0x3b, (byte)0x27, (byte)0x49, (byte)0xe0, (byte)0x05, (byte)0xcf, (byte)0xc0, (byte)0x41, (byte)0x1a, (byte)0x01, (byte)0x45, (byte)0x31, (byte)0x32, (byte)0x36,
            (byte)0x33, (byte)0x20, (byte)0x53, (byte)0x43, (byte)0x4f, (byte)0x47, (byte)0x4c, (byte)0x49, (byte)0x4f, (byte)0x20, (byte)0x20, (byte)0x55, (byte)0x4f, (byte)0x4c, (byte)0x41, (byte)0xff, (byte)0xfc, (byte)0x14,
            (byte)0x00, (byte)0x14, (byte)0x00, (byte)0x0a, (byte)0x00};

    @Test
    public void test() {
        N2KAISAtoN aton = new N2KAISAtoN(b);
        System.out.println(aton.getAtoNType());
        System.out.println(aton.getMMSI());
        System.out.println(aton.getName());
    }

    /*
    2070-01-01-21:33:11.733,4,129041,0,255,8,80,36,55,dc,e7,27,3b,27
    2070-01-01-21:33:11.735,4,129041,0,255,8,81,49,e0,05,cf,c0,41,1a
    2070-01-01-21:33:11.736,4,129041,0,255,8,84,01,45,31,32,36,33,20
    2070-01-01-21:33:11.737,4,129041,0,255,8,85,53,43,4f,47,4c,49,4f
    2070-01-01-21:33:11.738,4,129041,0,255,8,87,20,20,55,4f,4c,41,ff
    2070-01-01-21:33:11.739,4,129041,0,255,8,82,fc,14,00,14,00,0a,00

     */
}