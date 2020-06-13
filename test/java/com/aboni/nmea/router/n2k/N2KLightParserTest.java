package com.aboni.nmea.router.n2k;

import org.junit.Test;

import java.time.Instant;

import static org.junit.Assert.assertEquals;

public class N2KLightParserTest {

    @Test
    public void testParse() {
        N2KLightParser p = new N2KLightParser("{\"timestamp\":\"2020-06-05-18:09:59.257\",\"prio\":2,\"src\":204,\"dst\":255,\"pgn\":127250,\"description\":\"Vessel Heading\",\"fields\":{\"Heading\":277.9,\"Reference\":\"Magnetic\"}}");
        assertEquals(127250, p.getPgn());
        assertEquals(204, p.getSource());
        Instant t = Instant.parse("2020-06-05T18:09:59.257Z");
        assertEquals(t.toEpochMilli(), p.getTs());
        assertEquals("{\"Heading\":277.9,\"Reference\":\"Magnetic\"}", p.getFields());
    }

    @Test
    public void testParse1() {
        N2KLightParser p = new N2KLightParser("{\"timestamp\":\"2020-06-05-20:36:53.057\",\"prio\":2,\"src\":2,\"dst\":255,\"pgn\":129026,\"description\":\"COG & SOG, Rapid Update\",\"fields\":{\"COG Reference\":\"True\",\"COG\":90.0,\"SOG\":0.09}}");
        assertEquals(129026, p.getPgn());
        assertEquals(2, p.getSource());
        Instant t = Instant.parse("2020-06-05T20:36:53.057Z");
        assertEquals(t.toEpochMilli(), p.getTs());
        assertEquals("{\"COG Reference\":\"True\",\"COG\":90.0,\"SOG\":0.09}", p.getFields());
    }


}