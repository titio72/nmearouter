package com.aboni.nmea.router.processors;

import net.sf.marineapi.nmea.sentence.RMCSentence;
import net.sf.marineapi.nmea.util.Date;
import net.sf.marineapi.nmea.util.Time;
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
        RMCSentence s = (RMCSentence)d.getSentence(posJson);
        assertEquals(new Time("100738"), s.getTime());
        assertEquals(new Date("060620"), s.getDate());
        assertEquals(43.6774763, s.getPosition().getLatitude(), 0.0001);
        assertEquals(10.2739919, s.getPosition().getLongitude(), 0.0001);
        assertEquals(5.1, s.getSpeed(), 0.0001);
        assertEquals(90.0, s.getCourse(), 0.0001);
    }
}