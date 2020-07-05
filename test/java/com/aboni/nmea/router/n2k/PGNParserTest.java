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

    private static final String AIS_STATIC_DATA_CLASS_B =
            "2020-06-21-08:14:50.400,6,129809,0,255,19," +
                    "18,64,6f,be,0e,53,45,4e," +
                    "5a,41,46,55,52,49,41,20," +
                    "20,20,20,20,20,20,20,20," +
                    "20,ff,ff";

    private static final String[] AIS_STATIC_DATA_CLASS_B_FAST = new String[]{
            "2020-06-21-08:14:50.400,6,129809,0,255,8,80,19,18,64,6f,be,0e,53",
            "2020-06-21-08:14:50.407,6,129809,0,255,8,81,45,4e,5a,41,46,55,52",
            "2020-06-21-08:14:50.407,6,129809,0,255,8,82,49,41,20,20,20,20,20",
            "2020-06-21-08:14:50.407,6,129809,0,255,8,83,20,20,20,20,20,ff,ff"};

    private static final String[] AIS_FAST = new String[]{
            "2020-06-21-08:14:44.664,4,129039,0,255,8,00,1a,12,2e,f1,bd,0e,7e",
            "2020-06-21-08:14:44.664,4,129039,0,255,8,01,6c,ec,05,44,ba,af,19",
            "2020-06-21-08:14:44.665,4,129039,0,255,8,02,ab,10,2a,c9,01,00,00",
            "2020-06-21-08:14:44.665,4,129039,0,255,8,03,00,96,29,00,fc,ff,ff"};

    private static final String AIS_EXT = "2020-07-01-19:35:54.000,4,129039,15,255,26,12,2e,f1,bd,0e,7e,6c,ec,05,44,ba,af,19,ab,11,2a,c9,01,ff,ff,ff,97,29,ff,fc,ff";

    private static final String ENV_TEMP = "2020-07-02-05:48:17.000,5,130311,15,255,8,01,c2,b7,75,ff,7f,ff,ff";
    private static final String ENV_TEMP_HUM_PRESS = "2020-07-02-05:52:23.000,5,130311,15,255,8,01,02,b7,75,b0,36,f6,03";

    private PGNs pgnDefs;

    public PGNParserTest() throws Exception {
        pgnDefs = new PGNs(Constants.CONF_DIR + "/pgns.json", null);
    }

    @Test
    public void testAISStaticCLassB() throws PGNParser.PGNDataParseException {
        PGNParser parser = new PGNParser(pgnDefs, AIS_STATIC_DATA_CLASS_B_FAST[0]);
        for (int i = 1; i < AIS_STATIC_DATA_CLASS_B_FAST.length; i++) parser.addMore(AIS_STATIC_DATA_CLASS_B_FAST[i]);
        assertEquals(129809, parser.getPgn());
        JSONObject res = parser.getCanBoatJson().getJSONObject("fields");
        assertEquals(24, res.getInt("Message ID"));
        assertEquals("Initial", res.getString("Repeat indicator"));
        assertEquals(247361380L, res.getLong("User ID"));
        assertEquals("SENZAFURIA", res.getString("Name"));
    }

    @Test
    public void testAISStaticCLassBExt_tolerant() throws PGNParser.PGNDataParseException {
        PGNParser parser = new PGNParser(pgnDefs, AIS_STATIC_DATA_CLASS_B);
        assertEquals(129809, parser.getPgn());
        JSONObject res = parser.getCanBoatJson().getJSONObject("fields");
        assertEquals(24, res.getInt("Message ID"));
        assertEquals("Initial", res.getString("Repeat indicator"));
        assertEquals(247361380L, res.getLong("User ID"));
        assertEquals("SENZAFURIA", res.getString("Name"));
    }


    @Test
    public void testEnvironmentInfo() throws PGNParser.PGNDataParseException {
        PGNParser parser = new PGNParser(pgnDefs, ENV_TEMP);
        assertEquals(130311, parser.getPgn());
        JSONObject j = parser.getCanBoatJson().getJSONObject("fields");
        assertEquals(28.2, j.getDouble("Temperature"), 0.0001);
        assertEquals("Inside Temperature", j.getString("Temperature Source"));
        assertFalse(j.has("Atmospheric Pressure"));
        assertFalse(j.has("Humidity"));
        assertFalse(j.has("Humidity Source"));
    }

    @Test
    public void testEnvironmentInfoWithPressure() throws PGNParser.PGNDataParseException {
        PGNParser parser = new PGNParser(pgnDefs, ENV_TEMP_HUM_PRESS);
        assertEquals(130311, parser.getPgn());
        JSONObject j = parser.getCanBoatJson().getJSONObject("fields");
        assertEquals(28.2, j.getDouble("Temperature"), 0.0001);
        assertEquals("Inside Temperature", j.getString("Temperature Source"));
        assertEquals(56.0, j.getDouble("Humidity"), 0.0001);
        assertEquals("Inside", j.getString("Humidity Source"));
        assertEquals(101400, j.getDouble("Atmospheric Pressure"), 0.0001);
    }

    @Test
    public void test() throws PGNParser.PGNDataParseException {
        PGNParser parser = new PGNParser(pgnDefs, S11);
        assertEquals(126992, parser.getPgn());
        assertNotNull(parser.getCanBoatJson());
    }

    @Test
    public void testUnsupported() throws PGNParser.PGNDataParseException {
        PGNParser parser = new PGNParser(pgnDefs, S10);
        assertEquals(130916, parser.getPgn());
        try {
            JSONObject j = parser.getCanBoatJson();
            fail("PGN should be unsupported");
        } catch (PGNParser.PGNDataParseException e) {

        }
    }

    @Test
    public void testPilotHeading() throws PGNParser.PGNDataParseException {
        PGNParser.setExperimental();
        PGNParser parser = new PGNParser(pgnDefs, S9);
        assertEquals(65359, parser.getPgn());
        System.out.println(parser.getCanBoatJson());
        assertEquals("Marine", parser.getCanBoatJson().getJSONObject("fields").getString("Industry Code"));
        assertEquals(1851, parser.getCanBoatJson().getJSONObject("fields").getInt("Manufacturer Code"));
        assertEquals(28.9, parser.getCanBoatJson().getJSONObject("fields").getDouble("Heading Magnetic"), 0.0001);
    }

    @Test
    public void testHeading() throws Exception {
        PGNParser parser = new PGNParser(pgnDefs, S2);
        assertEquals(127250, parser.getPgn());
        assertEquals(Instant.parse("2011-11-24T22:42:04.390Z"), parser.getTime());
        JSONObject o = parser.getCanBoatJson();
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
    public void testAISPositionReportClassB_EXT() throws PGNParser.PGNDataParseException {
        PGNParser parser = new PGNParser(pgnDefs, AIS_EXT);
        parser.setDebug();
        JSONObject res = parser.getCanBoatJson().getJSONObject("fields");
        assertEquals(18, res.getInt("Message ID"));
        assertEquals("Initial", res.getString("Repeat Indicator"));
        assertEquals(247329070, res.getLong("User ID"));
        assertEquals(9.938035, res.getDouble("Longitude"), 0.0001);
        assertEquals(43.0946884, res.getDouble("Latitude"), 0.0001);
        assertEquals("High", res.getString("Position Accuracy"));
        assertEquals("in use", res.getString("RAIM"));
        assertEquals(42, res.getInt("Time Stamp"));
        assertEquals(61.7, res.getDouble("COG"), 0.0001);
        assertEquals(4.57, res.getDouble("SOG"), 0.0001);

        // comm state and transceiver info are not set in the generation of the message - check if they have been skipped
        assertFalse(res.has("Communication State"));
        assertFalse(res.has("AIS Transceiver information"));

        assertEquals(61.0, res.getDouble("Heading"), 0.00001);
        assertEquals(0, res.getInt("Regional Application"));
        assertEquals(0, res.getInt("Regional Application"));
        assertEquals("CS", res.getString("Unit type"));
        assertEquals("Yes", res.getString("Integrated Display"));
        assertEquals("Yes", res.getString("DSC"));
        assertEquals("entire marine band", res.getString("Band"));
        assertEquals("Yes", res.getString("Can handle Msg 22"));
        assertEquals("Assigned", res.getString("AIS mode"));
        assertEquals("ITDMA", res.getString("AIS communication state"));
    }

    @Test
    public void testAISPositionReportClassB_FAST() throws PGNParser.PGNDataParseException {
        PGNParser parser = new PGNParser(pgnDefs, AIS_FAST[0]);
        parser.setDebug();
        for (int i = 1; i < 4; i++) {
            assertTrue(parser.needMore());
            parser.addMore(AIS_FAST[i]);
        }
        assertFalse(parser.needMore());
        JSONObject res = parser.getCanBoatJson().getJSONObject("fields");

        assertEquals(18, res.getInt("Message ID"));
        assertEquals("Initial", res.getString("Repeat Indicator"));
        assertEquals(247329070, res.getLong("User ID"));
        assertEquals(9.938035, res.getDouble("Longitude"), 0.0001);
        assertEquals(43.0946884, res.getDouble("Latitude"), 0.0001);
        assertEquals("High", res.getString("Position Accuracy"));
        assertEquals("in use", res.getString("RAIM"));
        assertEquals(42, res.getInt("Time Stamp"));
        assertEquals(61.7, res.getDouble("COG"), 0.0001);
        assertEquals(4.57, res.getDouble("SOG"), 0.0001);
        assertEquals(0, res.getInt("Communication State"));
        assertEquals("Channel A VDL reception", res.getString("AIS Transceiver information"));
        assertEquals(61.0, res.getDouble("Heading"), 0.00001);
        assertEquals(0, res.getInt("Regional Application"));
        assertEquals(0, res.getInt("Regional Application"));
        assertEquals("CS", res.getString("Unit type"));
        assertEquals("Yes", res.getString("Integrated Display"));
        assertEquals("Yes", res.getString("DSC"));
        assertEquals("entire marine band", res.getString("Band"));
        assertEquals("Yes", res.getString("Can handle Msg 22"));
        assertEquals("Assigned", res.getString("AIS mode"));
        assertEquals("ITDMA", res.getString("AIS communication state"));
    }
}