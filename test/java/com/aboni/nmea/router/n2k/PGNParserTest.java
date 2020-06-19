package com.aboni.nmea.router.n2k;

import com.aboni.nmea.router.Constants;
import org.junit.Test;

import java.time.Instant;

import static org.junit.Assert.assertEquals;

public class PGNParserTest {
    private static final String S1 = "2011-11-24-22:42:04.388,2,127251,36,255,8,7d,0b,7d,02,00,ff,ff,ff";
    private static final String S2 = "2011-11-24-22:42:04.390,2,127250,36,255,8,00,5a,7c,00,00,00,00,fd";
    private static final String S3 = "2011-11-24-22:42:04.437,2,130306,36,255,8,b1,5c,00,ee,f0,fa,ff,ff";
    private static final String S4 = "2011-11-24-22:42:04.437,2,129025,22,255,8,AF,6D,1,1A,A6,A8,22,6";

    private static final String J1 = "{\"timestamp\":\"2011-11-24-22:42:04.388\",\"prio\":2,\"src\":36,\"dst\":255,\"pgn\":127251,\"description\":\"Rate of Turn\"," +
            "\"fields\":{\"SID\":125,\"Rate\":0.0934}}";
    private static final String J2 = "{\"timestamp\":\"2011-11-24-22:42:04.390\",\"prio\":2,\"src\":36,\"dst\":255,\"pgn\":127250,\"description\":\"Vessel Heading\"," +
            "\"fields\":{\"SID\":0,\"Heading\":182.4,\"Deviation\":0.0,\"Variation\":0.0,\"Reference\":\"Magnetic\"}}";
    private static final String J3 = "{\"timestamp\":\"2011-11-24-22:42:04.437\",\"prio\":2,\"src\":36,\"dst\":255,\"pgn\":130306,\"description\":\"Wind Data\"," +
            "\"fields\":{\"SID\":177,\"Wind Speed\":0.92,\"Wind Angle\":353.4,\"Reference\":\"Apparent\"}}";
    private static final String J4 = "{\"timestamp\":\"2011-11-24-22:42:04.437\",\"prio\":2,\"src\":22,\"dst\":255,\"pgn\":129025,\"description\":\"Position, Rapid Update\"," +
            "\"fields\":{\"Latitude\":43.6775106,\"Longitude\":10.2740244}}";

    private PGNs pgnDefs;

    public PGNParserTest() throws Exception {
        pgnDefs = new PGNs(Constants.CONF_DIR + "/pgns.json", null);
    }

    @Test
    public void testHeading() throws Exception {
        PGNParser parser = new PGNParser(pgnDefs, S2);
        assertEquals(127250, parser.getPgn());
        assertEquals(Instant.parse("2011-11-24T22:42:04.390Z"), parser.getTime());
        assertEquals(36, parser.getSource());
        assertEquals("Magnetic", parser.getCanBoatJson().getJSONObject("fields").getString("Reference"));
        assertEquals(0.0, parser.getCanBoatJson().getJSONObject("fields").getDouble("Variation"), 0.001);
        assertEquals(182.4, parser.getCanBoatJson().getJSONObject("fields").getDouble("Heading"), 0.001);
    }

    @Test
    public void testWind() throws Exception {
        PGNParser parser = new PGNParser(pgnDefs, S3);
        assertEquals(130306, parser.getPgn());
        assertEquals(36, parser.getSource());
        assertEquals("Apparent", parser.getCanBoatJson().getJSONObject("fields").getString("Reference"));
        assertEquals(0.92, parser.getCanBoatJson().getJSONObject("fields").getDouble("Wind Speed"), 0.001);
        assertEquals(353.4, parser.getCanBoatJson().getJSONObject("fields").getDouble("Wind Angle"), 0.001);
    }

    @Test
    public void testPosition() throws Exception {
        PGNParser parser = new PGNParser(pgnDefs, S4);
        assertEquals(129025, parser.getPgn());
        assertEquals(22, parser.getSource());
        assertEquals(43.6301231, parser.getCanBoatJson().getJSONObject("fields").getDouble("Latitude"), 0.001);
        assertEquals(10.2934694, parser.getCanBoatJson().getJSONObject("fields").getDouble("Longitude"), 0.001);
    }
}