package com.aboni.nmea.router.n2k.impl;

import com.aboni.nmea.router.n2k.N2KMessage;
import org.junit.Test;

import static org.junit.Assert.*;

public class CANBOATStreamTest {


    @Test
    public void sendFirstMessage() {
        N2KMessage o = new N2KStreamImpl().getMessage(ss[0]);
        assertNotNull(o);
        assertEquals(127250, o.getHeader().getPgn());
    }

    @Test
    public void skipNewMessageTooSoon() {
        N2KStreamImpl stream = new N2KStreamImpl(null, true);
        assertNotNull(stream.getMessage(ss[0]));
        assertNull(stream.getMessage(ss[1]));
    }

    @Test
    public void skipNewMessageUnchanged() {
        // skip the second because the long timeout (1000ms) is not expired and the values are the same
        N2KStreamImpl stream = new N2KStreamImpl(null, true);
        assertNotNull(stream.getMessage(ss[0]));
        assertNull(stream.getMessage(ss[2]));
    }

    @Test
    public void sendSecondMessageBecauseChanged() {
        // send second because the short timeout is expired (350ms) and the value is different
        N2KStreamImpl stream = new N2KStreamImpl();
        assertNotNull(stream.getMessage(ss[0]));
        assertNotNull(stream.getMessage(ss[3]));
    }

    @Test
    public void sendSecondMessageTimeout() {
        // send second because the long timeout is expired (so no matter the values are changed or not
        N2KStreamImpl stream = new N2KStreamImpl();
        assertNotNull(stream.getMessage(ss[0]));
        assertNotNull(stream.getMessage(ss[4]));
    }

    String[] ss = new String[]{
            /*0*/"1970-01-01-18:09:59.257,2,127250,204,255,8,ff,87,be,ff,7f,ff,7f,fd",
            /*1*/"1970-01-01-18:09:59.357,2,127250,204,255,8,ff,80,be,ff,7f,ff,7f,fd",
            /*2*/"1970-01-01-18:10:00.000,2,127250,204,255,8,ff,87,be,ff,7f,ff,7f,fd",
            /*3*/"1970-01-01-18:10:00.157,2,127250,204,255,8,ff,6f,be,ff,7f,ff,7f,fd",
            /*4*/"1970-01-01-18:10:00.357,2,127250,204,255,8,ff,69,be,ff,7f,ff,7f,fd"
    };
}