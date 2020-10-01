package com.aboni.nmea.router.n2k.impl;

import com.aboni.nmea.router.n2k.N2KMessageParser;
import com.aboni.nmea.router.n2k.PGNDataParseException;
import com.aboni.nmea.router.n2k.messages.N2KHeading;
import com.aboni.nmea.router.n2k.messages.impl.N2KMessageFactoryImpl;
import org.junit.Test;

import static org.junit.Assert.*;

public class N2KMessageParserTest {

    @Test
    public void testParse() throws PGNDataParseException {
        N2KMessageParser p = new N2KMessageParserImpl(new N2KMessageFactoryImpl());
        p.addString("2020-06-21-08:23:52.452,2,127250,204,255,8,ff,f7,12,ff,7f,ff,7f,fd");
        assertEquals(127250, p.getHeader().getPgn());
        assertEquals(2, p.getHeader().getPriority());
        assertEquals(204, p.getHeader().getSource());
        assertEquals(255, p.getHeader().getDest());
        assertEquals(8, p.getLength());
        assertEquals(0xff, p.getData()[0] & 0xff);
        assertEquals(0xf7, p.getData()[1] & 0xff);
        assertEquals(0x12, p.getData()[2] & 0xff);
        assertEquals(0xff, p.getData()[3] & 0xff);
        assertEquals(0x7f, p.getData()[4] & 0xff);
        assertEquals(0xff, p.getData()[5] & 0xff);
        assertEquals(0x7f, p.getData()[6] & 0xff);
        assertEquals(0xfd, p.getData()[7] & 0xff);
    }

    public void testMessage() throws PGNDataParseException {
        N2KMessageParser p = new N2KMessageParserImpl(new N2KMessageFactoryImpl());
        p.addString("2020-06-21-08:23:52.452,2,127250,204,255,8,ff,f7,12,ff,7f,ff,7f,fd");
        assertNotNull(p.getMessage());
        assertTrue(p.getMessage() instanceof N2KHeading);
    }

    public void testUnsupported() throws PGNDataParseException {
        N2KMessageParser p = new N2KMessageParserImpl(new N2KMessageFactoryImpl());
        p.addString("2020-06-21-08:23:52.452,2,999999,204,255,8,ff,ff,ff,ff,ff,ff,ff,ff");
        try {
            p.getMessage();
            fail("Message should be unsupported");
        } catch (PGNDataParseException e) {
            // ok
        }
    }

    @Test
    public void testFast() throws PGNDataParseException {
        N2KMessageParser p = new N2KMessageParserImpl(new N2KMessageFactoryImpl());
        p.addString(FAST[0]);
        assertTrue(p.needMore());
        p.addString(FAST[1]);
        assertTrue(p.needMore());
        p.addString(FAST[2]);
        assertTrue(p.needMore());
        p.addString(FAST[3]);
        assertFalse(p.needMore());

        byte[] data = new byte[]{
                (byte) 0xc1, (byte) 0xb8, (byte) 0x68, (byte) 0xbc, (byte) 0x0e, (byte) 0x31, (byte) 0x95, (byte) 0xf7, (byte) 0x05,
                (byte) 0xde, (byte) 0x5d, (byte) 0xa9, (byte) 0x19, (byte) 0x98, (byte) 0x16, (byte) 0x13, (byte) 0x60, (byte) 0x03,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x68, (byte) 0x12, (byte) 0x00, (byte) 0x00, (byte) 0xf0, (byte) 0xfe};

        assertEquals(data.length, p.getLength());
        for (int i = 0; i < data.length; i++) {
            assertEquals(data[i], p.getData()[i]);
        }
    }

    @Test
    public void testFastExpanded() throws PGNDataParseException {
        N2KMessageParser p = new N2KMessageParserImpl(new N2KMessageFactoryImpl());
        p.addString("2020-06-21-08:15:42.595,4,129038,0,255,27," +
                "c1,b8,68,bc,0e,31,95,f7,05," +
                "de,5d,a9,19,98,16,13,60,03," +
                "00,00,00,68,12,00,00,f0,fe");
        assertFalse(p.needMore());
    }

    private static final String[] FAST = new String[]{
            "2020-06-21-08:15:42.595,4,129038,0,255,8,a0,1b,c1,b8,68,bc,0e,31",
            "2020-06-21-08:15:42.595,4,129038,0,255,8,a1,95,f7,05,de,5d,a9,19",
            "2020-06-21-08:15:42.595,4,129038,0,255,8,a2,98,16,13,60,03,00,00",
            "2020-06-21-08:15:42.596,4,129038,0,255,8,a3,00,68,12,00,00,f0,fe"};
}