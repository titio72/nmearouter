package com.aboni.nmea.router.n2k;

import org.junit.Test;

import static org.junit.Assert.*;

public class CANBOATStreamTest {


    @Test
    public void sendFirstMessage() {
        CANBOATStream.PGN o = new CANBOATStream(getLogger()).getMessage(ss[0]);
        assertNotNull(o);
        assertEquals(127250, o.getPgn());
        assertEquals(277.9, o.getFields().getDouble("Heading"), 0.001);
    }

    @Test
    public void skipNewMessageTooSoon() {
        CANBOATStream stream = new CANBOATStream(getLogger());
        assertNotNull(stream.getMessage(ss[0]));
        assertNull(stream.getMessage(ss[1]));
    }

    @Test
    public void skipNewMessageUnchanged() {
        // skip the second because the long timeout (1000ms) is not expired and the values are the same
        CANBOATStream stream = new CANBOATStream(getLogger());
        assertNotNull(stream.getMessage(ss[0]));
        assertNull(stream.getMessage(ss[2]));
    }

    @Test
    public void sendSecondMessageBecauseChanged() {
        // send second because the short timeout is expired (350ms) and the value is different
        CANBOATStream stream = new CANBOATStream(getLogger());
        assertNotNull(stream.getMessage(ss[0]));
        assertNotNull(stream.getMessage(ss[3]));
    }

    @Test
    public void sendSecondMessageTimeout() {
        // send second because the long timeout is expired (so no matter the values are changed or not
        CANBOATStream stream = new CANBOATStream(getLogger());
        assertNotNull(stream.getMessage(ss[0]));
        assertNotNull(stream.getMessage(ss[4]));
    }


    String[] ss = new String[]{
            /*0*/"{\"timestamp\":\"2020-06-05-18:09:59.257\",\"prio\":2,\"src\":204,\"dst\":255,\"pgn\":127250,\"description\":\"Vessel Heading\",\"fields\":{\"Heading\":277.9,\"Reference\":\"Magnetic\"}}",
            /*1*/"{\"timestamp\":\"2020-06-05-18:09:59.357\",\"prio\":2,\"src\":204,\"dst\":255,\"pgn\":127250,\"description\":\"Vessel Heading\",\"fields\":{\"Heading\":277.9,\"Reference\":\"Magnetic\"}}",
            /*2*/"{\"timestamp\":\"2020-06-05-18:10:00.057\",\"prio\":2,\"src\":204,\"dst\":255,\"pgn\":127250,\"description\":\"Vessel Heading\",\"fields\":{\"Heading\":277.9,\"Reference\":\"Magnetic\"}}",
            /*3*/"{\"timestamp\":\"2020-06-05-18:10:00.157\",\"prio\":2,\"src\":204,\"dst\":255,\"pgn\":127250,\"description\":\"Vessel Heading\",\"fields\":{\"Heading\":277.8,\"Reference\":\"Magnetic\"}}",
            /*4*/"{\"timestamp\":\"2020-06-05-18:10:00.357\",\"prio\":2,\"src\":204,\"dst\":255,\"pgn\":127250,\"description\":\"Vessel Heading\",\"fields\":{\"Heading\":277.9,\"Reference\":\"Magnetic\"}}"
    };
}