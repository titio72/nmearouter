package com.aboni.geo;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import net.sf.marineapi.nmea.parser.SentenceFactory;
import net.sf.marineapi.nmea.sentence.HDGSentence;
import net.sf.marineapi.nmea.sentence.HDMSentence;
import net.sf.marineapi.nmea.sentence.HDTSentence;
import net.sf.marineapi.nmea.sentence.PositionSentence;
import net.sf.marineapi.nmea.sentence.TalkerId;
import net.sf.marineapi.nmea.util.Position;

public class NMEAMagnetic2TrueConverterTest {

    private NMEAMagnetic2TrueConverter conv;
    
    @Before
    public void setUp() {
        conv = new NMEAMagnetic2TrueConverter(2016);
    }

    @Test
    public void testSetPosition() {
        conv.setPosition(new Position(43.34, 10.01));
        assertEquals(43.34, conv.getPosition().getLatitude(), 0.0000001 );
        assertEquals(10.01, conv.getPosition().getLongitude(), 0.0000001 );
    }

    @Test
    public void testSetPositionNMEASentence() {
        PositionSentence s = (PositionSentence)SentenceFactory.getInstance().createParser("$GPGLL,4320.40,N,01000.60,E,225444,A");
        conv.setPosition(s);
        assertEquals(43.34, conv.getPosition().getLatitude(), 0.0000001 );
        assertEquals(10.01, conv.getPosition().getLongitude(), 0.0000001 );
    }
    
    @Test
    public void testMDM2HDT() {
        HDMSentence m = (HDMSentence)SentenceFactory.getInstance().createParser("$HCHDM,315.4,M");
        conv.setPosition(new Position(43.34, 10.01));
        HDTSentence t = conv.getTrueSentence(m);
        assertEquals("$HCHDT,317.8,T*24", t.toSentence());
    }

    @Test
    public void testHDT() {
        conv.setPosition(new Position(43.34, 10.01));
        HDTSentence t = conv.getTrueSentence(TalkerId.HC, 315.4);
        assertEquals("$HCHDT,317.8,T*24", t.toSentence());
    }

    @Test
    public void testHDG() {
        conv.setPosition(new Position(43.34, 10.01));
        HDGSentence t = conv.getSentence(TalkerId.HC, 315.4, 1.0);
        assertEquals(315.4, t.getHeading(), 0.001);
        assertEquals(2.5, t.getVariation(), 0.001);
        assertEquals(1.0, t.getDeviation(), 0.001);
    }
}
