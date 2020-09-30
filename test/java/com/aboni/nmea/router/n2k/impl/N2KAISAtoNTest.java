package com.aboni.nmea.router.n2k.impl;

import com.aboni.nmea.router.n2k.PGNDataParseException;
import com.aboni.nmea.router.n2k.messages.impl.N2KAISAtoN;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class N2KAISAtoNTest {

    private static final byte[] b = {(byte) 0x55, (byte) 0xdc, (byte) 0xe7, (byte) 0x27, (byte) 0x3b, (byte) 0x27, (byte) 0x49, (byte) 0xe0, (byte) 0x05, (byte) 0xcf, (byte) 0xc0, (byte) 0x41, (byte) 0x1a, (byte) 0x01, (byte) 0x45, (byte) 0x31, (byte) 0x32, (byte) 0x36,
            (byte) 0x33, (byte) 0x20, (byte) 0x53, (byte) 0x43, (byte) 0x4f, (byte) 0x47, (byte) 0x4c, (byte) 0x49, (byte) 0x4f, (byte) 0x20, (byte) 0x20, (byte) 0x55, (byte) 0x4f, (byte) 0x4c, (byte) 0x41, (byte) 0xff, (byte) 0xfc, (byte) 0x14,
            (byte) 0x00, (byte) 0x14, (byte) 0x00, (byte) 0x0a, (byte) 0x00};

    @Test
    public void test() throws PGNDataParseException {
        for (byte x : b) System.out.print(new String(new byte[]{x}) + " ");
        System.out.println();

        N2KMessageParserImpl p = new N2KMessageParserImpl();
        for (String ss : s0) p.addString(ss);

        N2KAISAtoN aton = (N2KAISAtoN) p.getMessage();
        assertEquals("Fixed light: with sectors", aton.getAtoNType());
        assertEquals("992471004", aton.getMMSI());
    }

    private static final String[] ORIGINAL = new String[]{
            "2070-01-01-21:33:11.733,4,129041,0,255,8,80,36,55,dc,e7,27,3b,27",
            "2070-01-01-21:33:11.735,4,129041,0,255,8,81,49,e0,05,cf,c0,41,1a",
            "2070-01-01-21:33:11.736,4,129041,0,255,8,84,01,45,31,32,36,33,20",
            "2070-01-01-21:33:11.737,4,129041,0,255,8,85,53,43,4f,47,4c,49,4f",
            "2070-01-01-21:33:11.738,4,129041,0,255,8,87,20,20,55,4f,4c,41,ff",
            "2070-01-01-21:33:11.739,4,129041,0,255,8,82,fc,14,00,14,00,0a,00"
    };

    // reordered with fill
    private static final String[] s0 = new String[]{
            "2070-01-01-21:33:11.733,4,129041,0,255,8,80,36,55,dc,e7,27,3b,27",
            "2070-01-01-21:33:11.735,4,129041,0,255,8,81,49,e0,05,cf,c0,41,1a",
            "2070-01-01-21:33:11.739,4,129041,0,255,8,82,fc,14,00,14,00,0a,00",
            "2070-01-01-21:33:11.733,4,129041,0,255,8,83,FF,FF,FF,FF,FF,FF,FF",
            "2070-01-01-21:33:11.736,4,129041,0,255,8,84,01,45,31,32,36,33,20",
            "2070-01-01-21:33:11.737,4,129041,0,255,8,85,53,43,4f,47,4c,49,4f",
            "2070-01-01-21:33:11.733,4,129041,0,255,8,86,FF,FF,FF,FF,FF,FF,FF",
            "2070-01-01-21:33:11.738,4,129041,0,255,8,87,20,20,55,4f,4c,41,ff",
    };

    // as they are (with the "ordinal" tampered) - NO
    private static final String[] s1 = new String[]{
            "2070-01-01-21:33:11.733,4,129041,0,255,8,1,36,55,dc,e7,27,3b,27",
            "2070-01-01-21:33:11.735,4,129041,0,255,8,2,49,e0,05,cf,c0,41,1a",
            "2070-01-01-21:33:11.736,4,129041,0,255,8,3,01,45,31,32,36,33,20",
            "2070-01-01-21:33:11.737,4,129041,0,255,8,4,53,43,4f,47,4c,49,4f",
            "2070-01-01-21:33:11.738,4,129041,0,255,8,5,20,20,55,4f,4c,41,ff",
            "2070-01-01-21:33:11.739,4,129041,0,255,8,6,fc,14,00,14,00,0a,00"
    };

    // reordered without filler
    private static final String[] s2 = new String[]{
            "2070-01-01-21:33:11.733,4,129041,0,255,8,1,36,55,dc,e7,27,3b,27",
            "2070-01-01-21:33:11.735,4,129041,0,255,8,2,49,e0,05,cf,c0,41,1a",
            "2070-01-01-21:33:11.739,4,129041,0,255,8,3,fc,14,00,14,00,0a,00",
            "2070-01-01-21:33:11.736,4,129041,0,255,8,4,01,45,31,32,36,33,20",
            "2070-01-01-21:33:11.737,4,129041,0,255,8,5,53,43,4f,47,4c,49,4f",
            "2070-01-01-21:33:11.738,4,129041,0,255,8,6,20,20,55,4f,4c,41,ff"
    };

}