package com.aboni.geo;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import net.sf.marineapi.nmea.parser.SentenceFactory;
import net.sf.marineapi.nmea.sentence.HDMSentence;
import net.sf.marineapi.nmea.sentence.HDTSentence;
import net.sf.marineapi.nmea.sentence.PositionSentence;
import net.sf.marineapi.nmea.util.Position;

public class NMEAMagnetic2TrueConverterTest {

    private NMEAMagnetic2TrueConverter conv;
    
    @Before
    public void setUp() throws Exception {
        conv = new NMEAMagnetic2TrueConverter();
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
    public void tesHDT() {
        HDMSentence m = (HDMSentence)SentenceFactory.getInstance().createParser("$HCHDM,315.4,M");
        conv.setPosition(new Position(43.34, 10.01));
        HDTSentence t = conv.getTrueSentence(m);
        assertEquals("$HCHDT,317.9,T*25", t.toSentence());
    }
}
