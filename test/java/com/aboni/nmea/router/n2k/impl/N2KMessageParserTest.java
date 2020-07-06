package com.aboni.nmea.router.n2k.impl;

import com.aboni.nmea.router.n2k.N2KMessageParser;
import com.aboni.nmea.router.n2k.PGNDataParseException;
import org.junit.Test;

import static org.junit.Assert.*;

public class N2KMessageParserTest {

    @Test
    public void testParse() throws PGNDataParseException {
        N2KMessageParser p = new N2KMessageParser("2020-06-21-08:23:52.452,2,127250,204,255,8,ff,f7,12,ff,7f,ff,7f,fd");
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
        N2KMessageParser p = new N2KMessageParser("2020-06-21-08:23:52.452,2,127250,204,255,8,ff,f7,12,ff,7f,ff,7f,fd");
        assertNotNull(p.getMessage());
        assertTrue(p.getMessage() instanceof N2KHeading);
    }

    public void testUnsupported() throws PGNDataParseException {
        N2KMessageParser p = new N2KMessageParser("2020-06-21-08:23:52.452,2,999999,204,255,8,ff,ff,ff,ff,ff,ff,ff,ff");
        try {
            p.getMessage();
            fail("Message should be unsupported");
        } catch (PGNDataParseException e) {
            // ok
        }
    }
}