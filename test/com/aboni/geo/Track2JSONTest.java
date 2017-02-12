package com.aboni.geo;

import static org.junit.Assert.*;

import java.io.StringWriter;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

public class Track2JSONTest {

	public Track2JSONTest() {
	}
	
	private PositionHistory createPositionHistory() {
		PositionHistory h = new PositionHistory();
		h.addPosition(new GeoPositionT(10001000, 43.10000000, 10.08000000));
		h.addPosition(new GeoPositionT(10061000, 43.10000100, 10.08000100));
		h.addPosition(new GeoPositionT(10121000, 43.10000200, 10.08000200));
		h.addPosition(new GeoPositionT(10181000, 43.10000300, 10.08000300));
		return h;
	}
	
	@Test
	public void testPath() throws Exception {
		Track2JSON j = new Track2JSON();
		j.setTrack(createPositionHistory());
		StringWriter w = new StringWriter();
		j.dump(w);
		JSONObject jTrack = new JSONObject(w.toString());
		JSONArray path = jTrack.getJSONObject("track").getJSONArray("path");

		assertEquals(4, path.length());

		assertEquals(43.10000000, ((JSONObject)path.get(0)).getDouble("lat"), 0.0000001);
		assertEquals(10.08000000, ((JSONObject)path.get(0)).getDouble("lng"), 0.0000001);
		
		assertEquals(43.10000100, ((JSONObject)path.get(1)).getDouble("lat"), 0.0000001);
		assertEquals(10.08000100, ((JSONObject)path.get(1)).getDouble("lng"), 0.0000001);
		
		assertEquals(43.10000200, ((JSONObject)path.get(2)).getDouble("lat"), 0.0000001);
		assertEquals(10.08000200, ((JSONObject)path.get(2)).getDouble("lng"), 0.0000001);
		
		assertEquals(43.10000300, ((JSONObject)path.get(3)).getDouble("lat"), 0.0000001);
		assertEquals(10.08000300, ((JSONObject)path.get(3)).getDouble("lng"), 0.0000001);
		
	}
	
	@Test
	public void testName() throws Exception {
		Track2JSON j = new Track2JSON();
		j.setTrack(createPositionHistory());
		j.setTrackName("pippo");
		StringWriter w = new StringWriter();
		j.dump(w);
		JSONObject jTrack = new JSONObject(w.toString());
		assertEquals("pippo", jTrack.getJSONObject("track").getString("name"));
	}
	
	
	@Test
	public void testDefaultName() throws Exception {
		Track2JSON j = new Track2JSON();
		j.setTrack(createPositionHistory());
		StringWriter w = new StringWriter();
		j.dump(w);
		JSONObject jTrack = new JSONObject(w.toString());
		assertEquals(Track2JSON.DEFAULT_TRACK_NAME, jTrack.getJSONObject("track").getString("name"));
	}
}
