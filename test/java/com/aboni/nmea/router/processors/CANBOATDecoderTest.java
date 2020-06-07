package com.aboni.nmea.router.processors;

import net.sf.marineapi.nmea.parser.DataNotAvailableException;
import net.sf.marineapi.nmea.sentence.*;
import net.sf.marineapi.nmea.util.Date;
import net.sf.marineapi.nmea.util.Side;
import net.sf.marineapi.nmea.util.Time;
import net.sf.marineapi.nmea.util.Units;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class CANBOATDecoderTest {

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void getSentenceRMC() {
        // {"timestamp":"2020-06-05-20:50:55.400","prio":3,"src":2,"dst":255,"pgn":126992,"description":"System Time","fields":{"Date":"2020.06.06", "Time": "10:07:39.07940"}}

        JSONObject timeJson = new JSONObject("{\"timestamp\":\"2020-06-05-20:50:55.400\",\"prio\":3,\"src\":2,\"dst\":255,\"pgn\":126992,\"description\":\"System Time\",\"fields\":{\"Date\":\"2020.06.06\", \"Time\": \"10:07:39.07940\"}}");
        JSONObject sogJson = new JSONObject("{\"timestamp\":\"2020-06-05-20:36:53.057\",\"prio\":2,\"src\":2,\"dst\":255,\"pgn\":129026,\"description\":\"COG & SOG, Rapid Update\",\"fields\":{\"COG Reference\":\"True\",\"COG\":90.0,\"SOG\":5.1}}");
        JSONObject posJson = new JSONObject("{\"timestamp\":\"2020-06-05-20:36:32.435\",\"prio\":2,\"src\":2,\"dst\":255,\"pgn\":129025,\"description\":\"Position, Rapid Update\",\"fields\":{\"Latitude\":43.6774763,\"Longitude\":10.2739919}}");

        CANBOATDecoder d = new CANBOATDecoder();
        d.getSentence(timeJson);
        d.getSentence(sogJson);
        RMCSentence s = (RMCSentence) d.getSentence(posJson);
        assertEquals(new Time("100739"), s.getTime());
        assertEquals(new Date("060620"), s.getDate());
        assertEquals(43.6774763, s.getPosition().getLatitude(), 0.0001);
        assertEquals(10.2739919, s.getPosition().getLongitude(), 0.0001);
        assertEquals(5.1, s.getSpeed(), 0.0001);
        assertEquals(90.0, s.getCourse(), 0.0001);
    }

    @Test
    public void testRudder() {
        // {"timestamp":"2020-06-05-23:56:39.582","prio":2,"src":204,"dst":255,"pgn":127245,"description":"Rudder","fields":{"Instance":0,"Position":4.7}}
        JSONObject rudderJson = new JSONObject("{\"timestamp\":\"2020-06-05-23:56:39.582\",\"prio\":2,\"src\":204,\"dst\":255,\"pgn\":127245,\"description\":\"Rudder\",\"fields\":{\"Instance\":0,\"Position\":4.7}}");
        CANBOATDecoder d = new CANBOATDecoder();
        RSASentence rsa = (RSASentence) d.getSentence(rudderJson);
        assertEquals(4.7, rsa.getRudderAngle(Side.STARBOARD), 0.000);
    }

    @Test
    public void testHeading() {
        // {"timestamp":"2020-06-05-18:03:25.680","prio":2,"src":204,"dst":255,"pgn":127250,"description":"Vessel Heading","fields":{"Heading":279.5,"Reference":"Magnetic"}}
        JSONObject headJson = new JSONObject("{\"timestamp\":\"2020-06-05-18:03:25.680\",\"prio\":2,\"src\":204,\"dst\":255,\"pgn\":127250,\"description\":\"Vessel Heading\",\"fields\":{\"Heading\":279.5,\"Reference\":\"Magnetic\"}}");
        HDMSentence hdm = (HDMSentence) new CANBOATDecoder().getSentence(headJson);
        assertEquals(279.5, hdm.getHeading(), 0.0001);
    }

    @Test
    public void testSpeedNoHeading() {
        // {"timestamp":"2020-06-05-18:03:25.627","prio":2,"src":105,"dst":255,"pgn":128259,"description":"Speed","fields":{"SID":0,"Speed Water Referenced":0.00,"Speed Water Referenced Type":"Paddle wheel"}}
        JSONObject speedJson = new JSONObject("{\"timestamp\":\"2020-06-05-18:03:25.627\",\"prio\":2,\"src\":105,\"dst\":255,\"pgn\":128259,\"description\":\"Speed\",\"fields\":{\"SID\":0,\"Speed Water Referenced\":5.20,\"Speed Water Referenced Type\":\"Paddle wheel\"}}");
        VHWSentence vhw = (VHWSentence) new CANBOATDecoder().getSentence(speedJson);
        assertEquals(5.2, vhw.getSpeedKnots(), 0.0001);
        try {
            vhw.getMagneticHeading();
            fail("Heading should not be available");
        } catch (DataNotAvailableException ignored) {
        }
    }

    @Test
    public void testSpeedWithHeading() {
        // {"timestamp":"2020-06-05-18:03:25.627","prio":2,"src":105,"dst":255,"pgn":128259,"description":"Speed","fields":{"SID":0,"Speed Water Referenced":0.00,"Speed Water Referenced Type":"Paddle wheel"}}
        JSONObject speedJson = new JSONObject("{\"timestamp\":\"2020-06-05-18:03:25.627\",\"prio\":2,\"src\":105,\"dst\":255,\"pgn\":128259,\"description\":\"Speed\",\"fields\":{\"SID\":0,\"Speed Water Referenced\":5.20,\"Speed Water Referenced Type\":\"Paddle wheel\"}}");
        JSONObject headJson = new JSONObject("{\"timestamp\":\"2020-06-05-18:03:25.680\",\"prio\":2,\"src\":204,\"dst\":255,\"pgn\":127250,\"description\":\"Vessel Heading\",\"fields\":{\"Heading\":279.5,\"Reference\":\"Magnetic\"}}");
        CANBOATDecoder d = new CANBOATDecoder();
        d.getSentence(headJson);
        VHWSentence vhw = (VHWSentence) d.getSentence(speedJson);
        assertEquals(5.2, vhw.getSpeedKnots(), 0.0001);
        assertEquals(279.5, vhw.getMagneticHeading(), 0.0001);
    }

    @Test
    public void testDepth() {
        // {"timestamp":"2020-06-05-18:03:25.626","prio":3,"src":105,"dst":255,"pgn":128267,"description":"Water Depth","fields":{"SID":0,"Depth":3.42,"Offset":0.200}}
        JSONObject depthJson = new JSONObject("{\"timestamp\":\"2020-06-05-18:03:25.626\",\"prio\":3,\"src\":105,\"dst\":255,\"pgn\":128267,\"description\":\"Water Depth\",\"fields\":{\"SID\":0,\"Depth\":3.42,\"Offset\":0.200}}");
        DPTSentence dpt = (DPTSentence) new CANBOATDecoder().getSentence(depthJson);
        assertEquals(3.4, dpt.getDepth(), 0.0001);
        assertEquals(0.2, dpt.getOffset(), 0.0001);
    }

    @Test
    public void testWind() {
        // {"timestamp":"2020-06-05-18:03:25.626","prio":2,"src":105,"dst":255,"pgn":130306,"description":"Wind Data",
        // "fields":{"SID":0,"Wind Speed":4.01,"Wind Angle":347.4,"Reference":"Apparent"}}
        JSONObject windJson = new JSONObject("{\"timestamp\":\"2020-06-05-18:03:25.626\",\"prio\":2,\"src\":105,\"dst\":255,\"pgn\":130306,\"description\":\"Wind Data\",\"fields\":{\"SID\":0,\"Wind Speed\":4.01,\"Wind Angle\":347.4,\"Reference\":\"Apparent\"}}");
        MWVSentence w = (MWVSentence) new CANBOATDecoder().getSentence(windJson);
        assertEquals(4.0, w.getSpeed(), 0.0001);
        assertEquals(347.4, w.getAngle(), 0.0001);
        assertFalse(w.isTrue());
        assertEquals(Units.KNOT, w.getSpeedUnit());
    }
}