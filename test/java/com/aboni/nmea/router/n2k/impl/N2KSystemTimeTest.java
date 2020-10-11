package com.aboni.nmea.router.n2k.impl;

import com.aboni.nmea.router.message.MsgSystemTime;
import com.aboni.nmea.router.n2k.messages.impl.N2KSystemTimeImpl;
import org.junit.Test;

import java.time.Instant;

import static org.junit.Assert.assertEquals;

public class N2KSystemTimeTest {

    @Test
    public void testSystemTimeOK() {
        MsgSystemTime s = new N2KSystemTimeImpl(new byte[]{(byte) 0x01, (byte) 0xf0, (byte) 0x02, (byte) 0x48, (byte) 0x90, (byte) 0x30, (byte) 0x05, (byte) 0x12});
        assertEquals(1, s.getSID());
        assertEquals("GPS", s.getTimeSourceType());
        assertEquals(Instant.parse("2020-06-21T08:23:53Z"), s.getTime());
    }

}