package com.aboni.nmea.router.agent.impl.track;

import com.aboni.geo.GeoPositionT;
import com.aboni.utils.db.DBHelper;
import org.junit.Test;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.*;

public class TrackMediaDBTest {

	@Test
	public void testInit() {
		DBTrackWriter trackDb = new DBTrackWriter();
		assertTrue(trackDb.init());
		trackDb.dispose();
	}
	
	private List<TrackPoint> get(long t) throws Exception {
		List<TrackPoint> res = new LinkedList<>();
		DBHelper db = new DBHelper(true);
		PreparedStatement st = db.getConnection().prepareStatement("select * from track where TS=?");
		st.setTimestamp(1, new Timestamp(t));
		ResultSet rs = st.executeQuery();
		while (rs.next()) {
			TrackPoint p = new TrackPoint(
					new GeoPositionT(rs.getTimestamp("TS").getTime(), rs.getDouble("lat"), rs.getDouble("lon") ),
					rs.getBoolean("anchor"),
					rs.getDouble("dist"),
					rs.getDouble("speed"),
					rs.getDouble("maxSpeed"),
					rs.getInt("dTime"));
			res.add(p);
		}
		db.close();
		return res;
	}
	
	private void cleanUp(long t) throws Exception {
		DBHelper db = new DBHelper(true);
		PreparedStatement st = db.getConnection().prepareStatement("delete from track where TS=?");
		st.setTimestamp(1, new Timestamp(t));
		st.executeUpdate();
		db.close();
	}
	
	private long getNow() {
		long t = System.currentTimeMillis();
		t = (t / 1000) * 1000;
		return t;
	}
	
	@Test
	public void testWritePointNoAnchor() throws Exception {
		DBTrackWriter trackDb = new DBTrackWriter();
		trackDb.init();
		long t = getNow();
		trackDb.write(new GeoPositionT(t, 43.23, 10.54), false, 0.12, 6.2, 7.4, 60);
		List<TrackPoint> res = get(t);
		cleanUp(t);

		assertEquals(1, res.size());
		TrackPoint p = res.get(0);
		assertEquals(t, p.position.getTimestamp());
		assertEquals(43.23, p.position.getLatitude(), 0.000001);
		assertEquals(10.54, p.position.getLongitude(), 0.000001);
		assertEquals(6.2, p.averageSpeed, 0.000001);
		assertEquals(7.4, p.maxSpeed, 0.000001);
		assertEquals(0.12, p.distance, 0.000001);
		assertEquals(60, p.period);
        assertFalse(p.anchor);
		trackDb.dispose();
	}

	@Test
	public void testWritePointAnchor() throws Exception {
		DBTrackWriter trackDb = new DBTrackWriter();
		trackDb.init();
		long t = getNow();
		trackDb.write(new GeoPositionT(t, 43.23, 10.54), true, 0.12, 6.2, 7.4, 60);
		List<TrackPoint> res = get(t);
		cleanUp(t);

		assertEquals(1, res.size());
		TrackPoint p = res.get(0);
		assertEquals(t, p.position.getTimestamp());
		assertEquals(43.23, p.position.getLatitude(), 0.000001);
		assertEquals(10.54, p.position.getLongitude(), 0.000001);
		assertEquals(6.2, p.averageSpeed, 0.000001);
		assertEquals(7.4, p.maxSpeed, 0.000001);
		assertEquals(0.12, p.distance, 0.000001);
		assertEquals(60, p.period);
		assertTrue(p.anchor);
	}

}
