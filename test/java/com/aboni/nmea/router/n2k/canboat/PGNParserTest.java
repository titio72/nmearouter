package com.aboni.nmea.router.n2k.canboat;

import com.aboni.nmea.router.Constants;
import com.aboni.nmea.router.n2k.PGNDataParseException;
import com.aboni.nmea.router.n2k.SeatalkPilotMode;
import org.json.JSONObject;
import org.junit.Test;

import java.time.Instant;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class PGNParserTest {
    private static final String S_RATE_OF_TURN = "2011-11-24-22:42:04.388,2,127251,36,255,8,7d,0b,7d,02,00,ff,ff,ff";
    private static final String S2 = "2011-11-24-22:42:04.390,2,127250,36,255,8,00,5a,7c,00,00,00,00,fd";
    private static final String S3 = "2011-11-24-22:42:04.437,2,130306,36,255,8,b1,5c,00,ee,f0,fa,ff,ff";
    private static final String S4 = "2011-11-24-22:42:04.437,2,129025,22,255,8,AF,6D,1,1A,A6,A8,22,6";

    private static final String[] HEADING_TRACK_CONTROL_FAST = new String[]{
            "2020-06-21-08:23:51.549,2,127237,172,255,8,00,15,3c,c2,1f,fe,00,ff",
            "2020-06-21-08:23:51.550,2,127237,172,255,8,01,ff,ff,ff,ff,ff,ff,ff",
            "2020-06-21-08:23:51.555,2,127237,172,255,8,02,ff,7f,ff,7f,ff,7f,ff",
            "2020-06-21-08:23:51.558,2,127237,172,255,8,03,ff,ff,ff,ff,ff,ff,ff"
    };

    private static final String S9 = "2020-06-21-08:24:09.457,7,65359,204,255,8,3b,9f,ff,ff,ff,af,13,ff";

    private static final String SYSTEM_TIME = "2020-06-21-08:24:08.122,3,126992,22,255,8,01,f0,02,48,80,7a,07,12";

    private static final String AIS_STATIC_DATA_CLASS_B_PART_A =
            "2020-06-21-08:14:50.400,6,129809,0,255,19," +
                    "18,64,6f,be,0e,53,45,4e," +
                    "5a,41,46,55,52,49,41,20," +
                    "20,20,20,20,20,20,20,20," +
                    "20,ff,ff";


    private static final String AIS_STATIC_DATA_CLASS_B_PART_B =
            "2020-06-21-08:15:26.962,6,129810,0,255,33," +
                    "18,c8,53,bc,0e,24,cf,c8," +
                    "18,94,0c,00,b8,49,51,39," +
                    "38,38,36,40,96,00,32,00," +
                    "28,00,8c,00,00,00,00,00," +
                    "03,ff";

    private static final String[] AIS_STATIC_DATA_CLASS_B_PART_A_FAST = new String[]{
            "2020-06-21-08:14:50.400,6,129809,0,255,8,80,19,18,64,6f,be,0e,53",
            "2020-06-21-08:14:50.407,6,129809,0,255,8,81,45,4e,5a,41,46,55,52",
            "2020-06-21-08:14:50.407,6,129809,0,255,8,82,49,41,20,20,20,20,20",
            "2020-06-21-08:14:50.407,6,129809,0,255,8,83,20,20,20,20,20,ff,ff" };

    private static final String[] AIS_STATIC_DATA_CLASS_B_PART_B_FAST = new String[]{
            "2020-06-21-08:15:26.962,6,129810,0,255,8,20,21,18,c8,53,bc,0e,24",
            "2020-06-21-08:15:26.964,6,129810,0,255,8,21,cf,c8,18,94,0c,00,b8",
            "2020-06-21-08:15:26.967,6,129810,0,255,8,22,49,51,39,38,38,36,40",
            "2020-06-21-08:15:26.967,6,129810,0,255,8,23,96,00,32,00,28,00,8c",
            "2020-06-21-08:15:26.968,6,129810,0,255,8,24,00,00,00,00,00,03,ff" };

    private static final String[] AIS_POS_REPORT_CLASS_B_FAST = new String[]{
            "2020-06-21-08:14:44.664,4,129039,0,255,8,00,1a,12,2e,f1,bd,0e,7e",
            "2020-06-21-08:14:44.664,4,129039,0,255,8,01,6c,ec,05,44,ba,af,19",
            "2020-06-21-08:14:44.665,4,129039,0,255,8,02,ab,10,2a,c9,01,00,00",
            "2020-06-21-08:14:44.665,4,129039,0,255,8,03,00,96,29,00,fc,ff,ff" };

    private static final String AIS_POS_REPORT_CLASS_B_EXT = "2020-07-01-19:35:54.000,4,129039,15,255,26,12,2e,f1,bd,0e,7e,6c,ec,05,44,ba,af,19,ab,11,2a,c9,01,ff,ff,ff,97,29,ff,fc,ff";

    private static final String[] AIS_STATIC_CLASS_A_FAST = new String[]{
            "2020-06-21-08:23:52.281,6,129794,0,255,8,e0,4b,05,b8,68,bc,0e,36",
            "2020-06-21-08:23:52.284,6,129794,0,255,8,e1,bf,7e,00,49,43,49,55",
            "2020-06-21-08:23:52.285,6,129794,0,255,8,e2,20,20,20,4d,45,47,41",
            "2020-06-21-08:23:52.285,6,129794,0,255,8,e3,20,53,4d,45,52,41,4c",
            "2020-06-21-08:23:52.285,6,129794,0,255,8,e4,44,41,20,20,20,20,20",
            "2020-06-21-08:23:52.286,6,129794,0,255,8,e5,20,20,3c,ae,06,54,01",
            "2020-06-21-08:23:52.286,6,129794,0,255,8,e6,ff,ff,ff,ff,02,48,40",
            "2020-06-21-08:23:52.288,6,129794,0,255,8,e7,99,b5,16,80,02,49,54",
            "2020-06-21-08:23:52.288,6,129794,0,255,8,e8,4c,49,56,20,20,20,20",
            "2020-06-21-08:23:52.288,6,129794,0,255,8,e9,20,20,20,20,20,20,20",
            "2020-06-21-08:23:52.288,6,129794,0,255,8,ea,20,20,20,20,04,e1,ff",
    };

    private static final String[] AIS_POS_REPORT_CLASS_A_FAST = new String[]{
            "2020-06-21-08:15:42.381,4,129038,0,255,8,80,1b,c1,80,0d,c3,0e,3c",
            "2020-06-21-08:15:42.383,4,129038,0,255,8,81,82,f8,05,10,a4,b0,19",
            "2020-06-21-08:15:42.383,4,129038,0,255,8,82,8d,e0,87,27,03,00,00",
            "2020-06-21-08:15:42.383,4,129038,0,255,8,83,00,fd,86,00,00,f0,fe" };

    private static final String[] AIS_POS_REPORT_CLASS_A_FAST_2 = new String[]{
            "2020-06-21-08:15:42.595,4,129038,0,255,8,a0,1b,c1,b8,68,bc,0e,31",
            "2020-06-21-08:15:42.595,4,129038,0,255,8,a1,95,f7,05,de,5d,a9,19",
            "2020-06-21-08:15:42.595,4,129038,0,255,8,a2,98,16,13,60,03,00,00",
            "2020-06-21-08:15:42.596,4,129038,0,255,8,a3,00,68,12,00,00,f0,fe" };

    private static final String ENV_TEMP = "2020-07-02-05:48:17.000,5,130311,15,255,8,01,c2,b7,75,ff,7f,ff,ff";
    private static final String ENV_TEMP_HUM_PRESS = "2020-07-02-05:52:23.000,5,130311,15,255,8,01,02,b7,75,b0,36,f6,03";

    private static final String[] DIRECTION_DATA_FAST = new String[]{
            "2020-06-21-08:15:44.340,3,130577,2,255,8,20,0e,c0,2a,e8,0d,ed,00",
            "2020-06-21-08:15:44.343,3,130577,2,255,8,21,ff,ff,ff,ff,7c,0a,41",
            "2020-06-21-08:15:44.344,3,130577,2,255,8,22,00,ff,ff,ff,ff,ff,ff"
    };

    private final PGNs pgnDefs;

    public PGNParserTest() throws Exception {
        pgnDefs = new PGNs(Constants.CONF_DIR + "/pgns.json", null);
    }

    @Test
    public void testSeatalkPilotLockedHeading() throws PGNDataParseException {
        String lockedHeading = "2020-06-21-08:15:46.295,7,65360,204,255,8,3b,9f,ff,ff,ff,7f,0c,ff";
        PGNParser parser = new PGNParser(pgnDefs, lockedHeading);
        assertEquals(65360L, parser.getPgn());
        JSONObject j = parser.getCanBoatJson().getJSONObject("fields");
        assertEquals(18.3, j.getDouble("Target Heading Magnetic"), 0.0001);
        assertFalse(j.has("Target Heading True"));
    }

    @Test
    public void testSeatalkPilotMode() throws PGNDataParseException {
        String standBy = "2020-06-21-08:15:25.792,7,65379,204,255,8,3B,9F,00,00,00,00,02,FF";
        String auto = "2020-06-21-08:15:25.792,7,65379,204,255,8,3B,9F,40,00,00,00,02,FF";
        String vane = "2020-06-21-08:15:25.792,7,65379,204,255,8,3B,9F,00,01,00,00,02,FF";
        String track = "2020-06-21-08:15:25.792,7,65379,204,255,8,3B,9F,80,01,00,00,02,FF";
        String trackAndManual = "2020-06-21-08:15:25.792,7,65379,204,255,8,3B,9F,81,01,00,00,02,FF";
        checkPilotMode(standBy, SeatalkPilotMode.Mode.STANDBY);
        checkPilotMode(auto, SeatalkPilotMode.Mode.AUTO);
        checkPilotMode(vane, SeatalkPilotMode.Mode.VANE);
        checkPilotMode(track, SeatalkPilotMode.Mode.TRACK);
        checkPilotMode(trackAndManual, SeatalkPilotMode.Mode.TRACK_DEV);
    }

    void checkPilotMode(String s, SeatalkPilotMode.Mode m) throws PGNDataParseException {
        PGNParser p = new PGNParser(pgnDefs, s);
        JSONObject j = p.getCanBoatJson().getJSONObject("fields");
        //System.out.printf("Mode %d %d\n",j.getInt("Pilot Mode"), j.getInt("Sub Mode"));
        //System.out.println(j);
        assertEquals(m, new SeatalkPilotMode(j.getInt("Pilot Mode"), j.getInt("Sub Mode")).getPilotMode());
    }

    @Test
    public void testHeadingTrackControl() throws PGNDataParseException {
        PGNParser.setExperimental();
        PGNParser parser = parse(HEADING_TRACK_CONTROL_FAST);
        assertEquals(127237, parser.getPgn());
        JSONObject j = parser.getCanBoatJson().getJSONObject("fields");
        assertEquals("No", j.getString("Rudder Limit Exceeded"));
        assertFalse(j.has("Off-Heading Limit Exceeded"));
        assertFalse(j.has("Off-Track Limit Exceeded"));
        assertFalse(j.has("Heading Reference"));
        assertEquals("No", j.getString("Override"));
        assertEquals("2", j.getString("Steering Mode"));
        assertEquals("Rudder Limit controlled", j.getString("Turn Mode"));
        assertEquals("No Order", j.getString("Commanded Rudder Direction"));
        assertEquals(1.5, j.getDouble("Commanded Rudder Angle"), 0.0001);
    }

    @Test
    public void testRateOfTurn() throws PGNDataParseException {
        PGNParser parser = new PGNParser(pgnDefs, S_RATE_OF_TURN);
        assertEquals(127251, parser.getPgn());
        assertEquals(0.292, parser.getCanBoatJson().getJSONObject("fields").getDouble("Rate"), 0.00001);
    }

    @Test
    public void testDirectionDataFast() throws PGNDataParseException {
        PGNParser parser = parse(DIRECTION_DATA_FAST);
        assertEquals(130577L, parser.getPgn());
        System.out.println(parser.getCanBoatJson());
        /*
        @TODO
         */
    }

    @Test
    public void testAISStaticCLassB() throws PGNDataParseException {
        PGNParser parser = parse(AIS_STATIC_DATA_CLASS_B_PART_A_FAST);
        assertEquals(129809, parser.getPgn());
        JSONObject res = parser.getCanBoatJson().getJSONObject("fields");
        assertEquals(24, res.getInt("Message ID"));
        assertEquals("Initial", res.getString("Repeat indicator"));
        assertEquals(247361380L, res.getLong("User ID"));
        assertEquals("SENZAFURIA", res.getString("Name"));
    }

    @Test
    public void testAISStaticClassBExt_Part_A_tolerant() throws PGNDataParseException {
        PGNParser parser = new PGNParser(pgnDefs, AIS_STATIC_DATA_CLASS_B_PART_A);
        assertEquals(129809, parser.getPgn());
        JSONObject res = parser.getCanBoatJson().getJSONObject("fields");
        assertEquals(24, res.getInt("Message ID"));
        assertEquals("Initial", res.getString("Repeat indicator"));
        assertEquals(247361380L, res.getLong("User ID"));
        assertEquals("SENZAFURIA", res.getString("Name"));
    }

    @Test
    public void testAISStaticClassBExt_Part_B_Fast() throws PGNDataParseException {
        PGNParser parser = parse(AIS_STATIC_DATA_CLASS_B_PART_B_FAST);
        assertEquals(129810, parser.getPgn());
        JSONObject res = parser.getCanBoatJson().getJSONObject("fields");
        assertEquals(24, res.getInt("Message ID"));
        assertEquals("Initial", res.getString("Repeat indicator"));
        assertEquals(247223240L, res.getLong("User ID"));
        assertEquals(15.0, res.getLong("Length"), 0.001);
        assertEquals(5.0, res.getLong("Beam"), 0.001);
        assertEquals("Sailing", res.getString("Type of ship"));
        assertEquals("IQ9886", res.getString("Callsign"));
    }

    @Test
    public void testAISStaticClassBExt_Part_B() throws PGNDataParseException {
        PGNParser parser = new PGNParser(pgnDefs, AIS_STATIC_DATA_CLASS_B_PART_B);
        assertEquals(129810, parser.getPgn());
        JSONObject res = parser.getCanBoatJson().getJSONObject("fields");
        assertEquals(24, res.getInt("Message ID"));
        assertEquals("Initial", res.getString("Repeat indicator"));
        assertEquals(247223240L, res.getLong("User ID"));
        assertEquals(15.0, res.getLong("Length"), 0.001);
        assertEquals(5.0, res.getLong("Beam"), 0.001);
        assertEquals("Sailing", res.getString("Type of ship"));
        assertEquals("IQ9886", res.getString("Callsign"));
    }

    @Test
    public void testEnvironmentInfo() throws PGNDataParseException {
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
    public void testEnvironmentInfoWithPressure() throws PGNDataParseException {
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
    public void testHeader() throws PGNDataParseException {
        PGNParser parser = new PGNParser(pgnDefs, SYSTEM_TIME);
        assertEquals(126992, parser.getPgn());
        assertEquals(3, parser.getPriority());
        assertEquals(22, parser.getSource());
        assertEquals(255, parser.getDest());
        assertEquals(8, parser.getLength());
        assertEquals(8, parser.getData().length);
        assertEquals(Instant.parse("2020-06-21T08:24:08.122Z"), parser.getTime());
    }

    @Test
    public void testPilotHeading() throws PGNDataParseException {
        PGNParser.setExperimental();
        PGNParser parser = new PGNParser(pgnDefs, S9);
        assertEquals(65359, parser.getPgn());
        assertEquals("Marine", parser.getCanBoatJson().getJSONObject("fields").getString("Industry Code"));
        assertEquals("Raymarine", parser.getCanBoatJson().getJSONObject("fields").getString("Manufacturer Code"));
        assertEquals(28.9, parser.getCanBoatJson().getJSONObject("fields").getDouble("Heading Magnetic"), 0.0001);
    }

    @Test
    public void testHeading() throws PGNDataParseException {
        PGNParser parser = new PGNParser(pgnDefs, S2);
        assertEquals(127250, parser.getPgn());
        assertEquals(Instant.parse("2011-11-24T22:42:04.390Z"), parser.getTime());
        assertEquals(36, parser.getSource());
        assertEquals("Magnetic", parser.getCanBoatJson().getJSONObject("fields").getString("Reference"));
        assertEquals(0.0, parser.getCanBoatJson().getJSONObject("fields").getDouble("Variation"), 0.001);
        assertEquals(182.4, parser.getCanBoatJson().getJSONObject("fields").getDouble("Heading"), 0.001);
    }

    @Test
    public void testWind() throws PGNDataParseException {
        PGNParser parser = new PGNParser(pgnDefs, S3);
        assertEquals(130306, parser.getPgn());
        assertEquals(36, parser.getSource());
        assertEquals("Apparent", parser.getCanBoatJson().getJSONObject("fields").getString("Reference"));
        assertEquals(0.92, parser.getCanBoatJson().getJSONObject("fields").getDouble("Wind Speed"), 0.001);
        assertEquals(353.4, parser.getCanBoatJson().getJSONObject("fields").getDouble("Wind Angle"), 0.001);
    }

    @Test
    public void testPosition() throws PGNDataParseException {
        PGNParser parser = new PGNParser(pgnDefs, S4);
        assertEquals(129025, parser.getPgn());
        assertEquals(22, parser.getSource());
        assertEquals(43.6301231, parser.getCanBoatJson().getJSONObject("fields").getDouble("Latitude"), 0.001);
        assertEquals(10.2934694, parser.getCanBoatJson().getJSONObject("fields").getDouble("Longitude"), 0.001);
    }


    @Test
    public void testAISPositionReportClassB_EXT() throws PGNDataParseException {
        PGNParser parser = new PGNParser(pgnDefs, AIS_POS_REPORT_CLASS_B_EXT);
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
    public void testAISPositionReportClassB_FAST() throws PGNDataParseException {
        PGNParser parser = parse(AIS_POS_REPORT_CLASS_B_FAST);
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


    @Test
    public void testAISStaticInfoClassA_FAST() throws PGNDataParseException {
        PGNParser parser = parse(AIS_STATIC_CLASS_A_FAST);
        JSONObject res = parser.getCanBoatJson().getJSONObject("fields");
        assertEquals(5, res.getInt("Message ID"));
        assertEquals("Initial", res.getString("Repeat Indicator"));
        assertEquals(247228600, res.getLong("User ID"));
        assertEquals(8306486L, res.getLong("IMO number"));
        assertEquals("ICIU", res.getString("Callsign"));
        assertEquals("MEGA SMERALDA", res.getString("Name"));
        assertEquals("Passenger ship", res.getString("Type of ship"));
        assertEquals(171.0, res.getDouble("Length"), 0.0001);
        assertEquals(34.0, res.getDouble("Beam"), 0.0001);
        assertEquals("2020.06.21", res.getString("ETA Date"));
        assertEquals("10:35:00", res.getString("ETA Time"));
        assertEquals(6.40, res.getDouble("Draft"), 0.0001);
        assertEquals("ITLIV", res.getString("Destination"));
        assertEquals("ITU-R M.1371-1", res.getString("AIS version indicator"));
        assertEquals("GPS", res.getString("GNSS type"));
        assertEquals("available", res.getString("DTE"));
        assertEquals("Channel B VDL reception", res.getString("AIS Transceiver information"));
    }

    @Test
    public void testAISPositionReportClassA_FAST() throws PGNDataParseException {
        PGNParser parser = parse(AIS_POS_REPORT_CLASS_A_FAST);
        assertEquals(129038, parser.getPgn());
        JSONObject res = parser.getCanBoatJson().getJSONObject("fields");
        assertEquals(1, res.getInt("Message ID"));
        assertEquals(247664000L, res.getLong("User ID"));
        assertEquals(10.0172348, res.getDouble("Longitude"), 0.0001);
        assertEquals(43.1006736, res.getDouble("Latitude"), 0.0001);
        assertEquals("High", res.getString("Position Accuracy"));
        assertEquals("not in use", res.getString("RAIM"));
        assertEquals(35, res.getInt("Time Stamp"));
        assertEquals(199.3, res.getDouble("COG"), 0.0001);
        assertEquals(8.07, res.getDouble("SOG"), 0.0001);
        assertEquals(0, res.getInt("Communication State"));
        assertEquals("Channel A VDL reception", res.getString("AIS Transceiver information"));
        assertEquals(198.0, res.getDouble("Heading"), 0.00001);
        assertEquals(0, res.getDouble("Rate of Turn"), 0.001);
        assertEquals("Under way using engine", res.getString("Nav Status"));
        assertEquals(6, res.getInt("AIS Spare"));
    }

    @Test
    public void testAISPositionReportClassA_FAST_2() throws PGNDataParseException {
        PGNParser parser = parse(AIS_POS_REPORT_CLASS_A_FAST_2);
        assertEquals(129038, parser.getPgn());
        JSONObject res = parser.getCanBoatJson().getJSONObject("fields");
        assertEquals(1, res.getInt("Message ID"));
        assertEquals(247228600L, res.getLong("User ID"));
        assertEquals(10.0111665, res.getDouble("Longitude"), 0.0001);
        assertEquals(43.0530014, res.getDouble("Latitude"), 0.0001);
        assertEquals("Low", res.getString("Position Accuracy"));
        assertEquals("not in use", res.getString("RAIM"));
        assertEquals(38, res.getInt("Time Stamp"));
        assertEquals(28.0, res.getDouble("COG"), 0.0001);
        assertEquals(8.64, res.getDouble("SOG"), 0.0001);
        assertEquals(0, res.getInt("Communication State"));
        assertEquals("Channel A VDL reception", res.getString("AIS Transceiver information"));
        assertEquals(27.0, res.getDouble("Heading"), 0.00001);
        assertEquals(0, res.getDouble("Rate of Turn"), 0.001);
        assertEquals("Under way using engine", res.getString("Nav Status"));
        assertEquals(6, res.getInt("AIS Spare"));
    }

    PGNParser parse(String[] s) throws PGNDataParseException {
        PGNParser p = new PGNParser(pgnDefs, s[0]);
        int i = 1;
        while (p.needMore()) {
            if (i >= s.length) throw new PGNDataParseException(p.getPgn());
            p.addMore(s[i]);
            i++;
        }
        return p;
    }
}