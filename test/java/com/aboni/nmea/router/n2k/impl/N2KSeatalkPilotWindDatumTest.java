package com.aboni.nmea.router.n2k.impl;

import com.aboni.nmea.router.message.MsgSeatalkPilotWindDatum;
import com.aboni.nmea.router.n2k.messages.impl.N2KSeatalkPilotWindDatumImpl;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class N2KSeatalkPilotWindDatumTest {

    @Test
    public void test() {
        MsgSeatalkPilotWindDatum w = new N2KSeatalkPilotWindDatumImpl(new byte[]{(byte) 0x3b, (byte) 0x9f, (byte) 0xc2, (byte) 0x03, (byte) 0xcc, (byte) 0x08, (byte) 0xfa, (byte) 0xff});
        assertEquals(5.5, w.getWindDatum(), 0.01);
        assertEquals(12.9, w.getRollingAverageWind(), 0.01);

    }

}