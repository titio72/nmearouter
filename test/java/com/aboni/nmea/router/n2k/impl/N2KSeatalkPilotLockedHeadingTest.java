package com.aboni.nmea.router.n2k.impl;

import com.aboni.nmea.router.message.MsgSeatalkPilotLockedHeading;
import com.aboni.nmea.router.n2k.messages.impl.N2KSeatalkPilotLockedHeadingImpl;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class N2KSeatalkPilotLockedHeadingTest {

    @Test
    public void test() {
        MsgSeatalkPilotLockedHeading h = new N2KSeatalkPilotLockedHeadingImpl(new byte[]{(byte) 0x3b, (byte) 0x9f, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0x7f, (byte) 0x0c, (byte) 0xff});
        assertEquals(18.3, h.getLockedHeadingMagnetic(), 0.01);
        assertTrue(Double.isNaN(h.getLockedHeadingTrue()));

    }

}