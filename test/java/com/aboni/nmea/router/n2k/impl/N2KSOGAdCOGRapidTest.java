package com.aboni.nmea.router.n2k.impl;

import com.aboni.nmea.router.message.MsgSOGAdCOG;
import com.aboni.nmea.router.n2k.messages.impl.N2KSOGAdCOGRapidImpl;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class N2KSOGAdCOGRapidTest {

    @Test
    public void testSOGCOGOk() {
        MsgSOGAdCOG s = new N2KSOGAdCOGRapidImpl(new byte[]{(byte) 0xff, (byte) 0xfc, (byte) 0x68, (byte) 0x1b, (byte) 0x89, (byte) 0x00, (byte) 0xff, (byte) 0xff});
        assertEquals(40.2, s.getCOG(), 0.1);
        assertEquals(2.67, s.getSOG(), 0.01);
        assertEquals("True", s.getCOGReference());
        assertTrue(s.isTrueCOG());
    }
}