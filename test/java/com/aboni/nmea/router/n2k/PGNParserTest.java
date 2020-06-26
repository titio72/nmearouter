package com.aboni.nmea.router.n2k;

import com.aboni.nmea.router.Constants;
import org.json.JSONObject;
import org.junit.Test;

import java.time.Instant;

import static org.junit.Assert.*;

public class PGNParserTest {
    private static final String S1 = "2011-11-24-22:42:04.388,2,127251,36,255,8,7d,0b,7d,02,00,ff,ff,ff";
    private static final String S2 = "2011-11-24-22:42:04.390,2,127250,36,255,8,00,5a,7c,00,00,00,00,fd";
    private static final String S3 = "2011-11-24-22:42:04.437,2,130306,36,255,8,b1,5c,00,ee,f0,fa,ff,ff";
    private static final String S4 = "2011-11-24-22:42:04.437,2,129025,22,255,8,AF,6D,1,1A,A6,A8,22,6";

    private static final String S5 = "2020-06-21-08:23:51.549,2,127237,172,255,8,00,15,3c,c2,1f,fe,00,ff";
    private static final String S6 = "2020-06-21-08:23:51.550,2,127237,172,255,8,01,ff,ff,ff,ff,ff,ff,ff";
    private static final String S7 = "2020-06-21-08:23:51.555,2,127237,172,255,8,02,ff,7f,ff,7f,ff,7f,ff";
    private static final String S8 = "2020-06-21-08:23:51.558,2,127237,172,255,8,03,ff,ff,ff,ff,ff,ff,ff";

    private static final String S9 = "2020-06-21-08:24:09.457,7,65359,204,255,8,3b,9f,ff,ff,ff,af,13,ff";

    private static final String S10 = "2020-06-21-08:24:11.859,7,130916,204,255,8,20,07,3b,9f,ff,ff,ff,7f";
    private static final String S11 = "2020-06-21-08:24:08.122,3,126992,22,255,8,01,f0,02,48,80,7a,07,12";

    private static final String[] AIS1 = new String[]{
            "2020-06-21-08:23:52.582,4,129038,0,255,8,a0,1b,c1,b8,68,bc,0e,c8",
            "2020-06-21-08:23:52.582,4,129038,0,255,8,a1,33,fa,05,74,bb,ae,19",
            "2020-06-21-08:23:52.582,4,129038,0,255,8,a2,cc,8b,09,55,03,00,00",
            "2020-06-21-08:23:52.583,4,129038,0,255,8,a3,00,dc,08,00,00,f0,fe"};

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
    public void test() {
        PGNParser parser = new PGNParser(pgnDefs, S11);
        assertEquals(126992, parser.getPgn());
        JSONObject j = parser.getCanBoatJson();
        System.out.println(j);
    }

    @Test
    public void testUnsupported() {
        PGNParser parser = new PGNParser(pgnDefs, S10);
        assertEquals(130916, parser.getPgn());
        try {
            JSONObject j = parser.getCanBoatJson();
            fail("PGN should be unsupported");
        } catch (PGNParser.PGNDataParseException e) {

        }
    }


    @Test
    public void testPilotHeading() {
        PGNParser parser = new PGNParser(pgnDefs, S9);
        assertEquals(65359, parser.getPgn());
        System.out.println(parser.getCanBoatJson());
        // LOOKUP_INDUSTRY_CODE (",0=Global,1=Highway,2=Agriculture,3=Construction,4=Marine,5=Industrial")
    }


    @Test
    public void testHeading() throws Exception {
        PGNParser parser = new PGNParser(pgnDefs, S2);
        assertEquals(127250, parser.getPgn());
        assertEquals(Instant.parse("2011-11-24T22:42:04.390Z"), parser.getTime());
        JSONObject o = parser.getCanBoatJson();
        System.out.println(o);
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

    @Test
    public void testMultiLine() throws Exception {
        PGNParser parser = new PGNParser(pgnDefs, S5);

        assertTrue(parser.needMore());
        parser.addMore(S6);
        assertEquals(16, parser.getLength());

        assertTrue(parser.needMore());
        parser.addMore(S7);
        assertEquals(24, parser.getLength());

        System.out.println(parser.getCanBoatJson());
    }

    @Test
    public void testAIS() {
        PGNParser parser = new PGNParser(pgnDefs, AIS1[0]);
        parser.setDebug();
        for (int i = 1; i < 4; i++) {
            assertTrue(parser.needMore());
            parser.addMore(AIS1[1]);
        }
        assertFalse(parser.needMore());
        parser.getCanBoatJson();
        System.out.println(parser.getCanBoatJson());

        /*
        "Message ID":1,
        "User ID":247228600,
        "Longitude":10.0283336,
        "Latitude":43.0881652,
        "Position Accuracy":"Low",
        "RAIM":"not in use",
        "Time Stamp":"51",
        "COG":14.0,
        "SOG":8.53,
        "Communication State":"0",
        "AIS Transceiver information":"Channel A VDL reception",
        "Heading":13.0,
        "Rate of Turn":0.00,
        "Nav Status":"Under way using engine",
        "AIS Spare":"6"
         */
    }
}