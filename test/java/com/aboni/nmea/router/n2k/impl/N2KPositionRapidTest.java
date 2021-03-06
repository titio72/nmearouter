package com.aboni.nmea.router.n2k.impl;

import com.aboni.nmea.router.n2k.PGNDataParseException;
import com.aboni.nmea.router.message.MsgPosition;
import com.aboni.nmea.router.n2k.messages.impl.N2KPositionRapidImpl;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class N2KPositionRapidTest {

    @Test
    public void testPosOk() throws PGNDataParseException {
        MsgPosition s = new N2KPositionRapidImpl(null, new byte[]{(byte) 0x36, (byte) 0x1a, (byte) 0xab, (byte) 0x19, (byte) 0x04, (byte) 0x42, (byte) 0xde, (byte) 0x05});

        assertEquals(43.0643766, s.getPosition().getLatitude(), 0.0000001);
        assertEquals(9.8451972, s.getPosition().getLongitude(), 0.0000001);
        assertNotNull(s.getPosition());
        assertEquals(43.0643766, s.getPosition().getLatitude(), 0.0000001);
        assertEquals(9.8451972, s.getPosition().getLongitude(), 0.0000001);
    }

}