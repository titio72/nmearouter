package com.aboni.nmea.router.n2k.impl;

import com.aboni.nmea.router.n2k.N2KMessage;
import com.aboni.nmea.router.n2k.messages.impl.N2KSystemTimeImpl;
import com.aboni.nmea.router.nmea0183.impl.Message2NMEA0183Impl;
import net.sf.marineapi.nmea.sentence.Sentence;
import net.sf.marineapi.nmea.sentence.ZDASentence;
import net.sf.marineapi.nmea.util.Date;
import net.sf.marineapi.nmea.util.Time;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class Message2NMEA0183ImplTest {

    @Test
    public void testSystemTime() {
        N2KMessage s = new N2KSystemTimeImpl(new byte[]{(byte) 0x01, (byte) 0xf0, (byte) 0x02, (byte) 0x48, (byte) 0x90, (byte) 0x30, (byte) 0x05, (byte) 0x12});
        Sentence[] sentences = new Message2NMEA0183Impl().convert(s);
        assertEquals(1, sentences.length);
        assertTrue(sentences[0] instanceof ZDASentence);
        assertEquals(new Time("082353"), ((ZDASentence) sentences[0]).getTime());
        assertEquals(new Date("21062020"), ((ZDASentence) sentences[0]).getDate());
    }
}