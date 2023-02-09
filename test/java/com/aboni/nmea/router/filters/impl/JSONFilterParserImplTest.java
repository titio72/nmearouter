package com.aboni.nmea.router.filters.impl;

import com.aboni.nmea.router.filters.DummyFilter;
import com.aboni.nmea.router.filters.NMEAFilter;
import com.aboni.nmea.router.filters.NMEAFilterSet;
import org.json.JSONObject;
import org.junit.Test;

import static org.junit.Assert.*;

public class JSONFilterParserImplTest {

	@Test
	public void testParseFilterSet() {
        String s = "{" +
                "\"filter\": {  " +
                "    \"type\": \"set\",  " +
                "    \"logic\": \"blacklist\",  " +
                "    \"filters\": [  " +
                "      {  " +
                "        \"filter\": {  " +
                "          \"type\": \"dummy\",  " +
                "          \"data\": \"my data!!!\"  " +
                "        }  " +
                "      }  " +
                "    ]  " +
                "  }  " +
                "}";
        NMEAFilter parsedF = new JSONFilterParserImpl().getFilter(new JSONObject(s));
        assertNotNull(parsedF);
        assertTrue(parsedF instanceof NMEAFilterSet);
	}

    @Test
    public void testParseNMEABasic() {
        String s =
                "      {  " +
                        "        \"filter\": {  " +
                        "          \"type\": \"nmea\",  " +
                        "          \"sentence\": \"GLL\"  " +
                        "        }  " +
                        "      }  ";
        NMEAFilter parsedF = new JSONFilterParserImpl().getFilter(new JSONObject(s));
        assertNotNull(parsedF);
        assertTrue(parsedF instanceof NMEABasicSentenceFilter);
    }

    @Test
    public void testParseDummy() {
        String s =
                "      {  " +
                        "        \"filter\": {  " +
                        "          \"type\": \"dummy\",  " +
                        "          \"data\": \"ohhhh!!!!!\"  " +
                        "        }  " +
                        "      }  ";
        NMEAFilter parsedF = new JSONFilterParserImpl().getFilter(new JSONObject(s));
        assertNotNull(parsedF);
        assertTrue(parsedF instanceof DummyFilter);
    }

    @Test
    public void testParseNotExists() {
        String s =
                "      {  " +
                        "        \"filter\": {  " +
                        "          \"type\": \"NOT_EXISTING\",  " +
                        "          \"sentence\": \"ohhhh!!!!!\"  " +
                        "        }  " +
                        "      }  ";
        assertThrows(IllegalArgumentException.class, () -> new JSONFilterParserImpl().getFilter(new JSONObject(s)));
    }
}
