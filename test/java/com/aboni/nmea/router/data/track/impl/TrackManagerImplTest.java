package com.aboni.nmea.router.data.track.impl;

import com.aboni.geo.GeoPositionT;
import com.aboni.misc.Utils;
import com.aboni.nmea.router.NMEARouterModule;
import com.aboni.nmea.router.data.track.TrackManager;
import com.aboni.nmea.router.data.track.TrackPoint;
import com.aboni.utils.ThingsFactory;
import com.google.inject.Guice;
import com.google.inject.Injector;
import net.sf.marineapi.nmea.util.Position;
import org.junit.Before;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import static org.junit.Assert.*;

public class TrackManagerImplTest {

    static {
        Injector injector = Guice.createInjector(new NMEARouterModule());
        ThingsFactory.setInjector(injector);
    }

    private final int period = 30; // seconds
    private double lat;
    private double lon;
    private long t0;
    private long lastPosted;
    private TrackManager m;

    @Before
    public void setup() {
        m = new TrackManagerImpl();
        m.setPeriod(period * 1000);
        lat = 43.67830115349512;
        lon = 10.266444683074951;
        t0 = System.currentTimeMillis();
    }

	private static final SimpleDateFormat fmt = new SimpleDateFormat("dd HH:mm:ss");
	static {
		fmt.setTimeZone(TimeZone.getTimeZone("UTC"));
	}
	
	private void dump(List<TrackPoint> l) {
		for (TrackPoint p: l) {
			System.out.format("%d %.2f %d %s %n",
                    p.isAnchor() ? 1 : 0,
                    p.getDistance() * 1852.0,
                    p.getPeriod(),
                    fmt.format(new Date((p.getPosition().getTimestamp() - t0))));
		}
		
	}
	
	private TrackPoint postPosition(long ts, double speed) {
        Position p = new Position(lat, lon);
        Position p1 = Utils.calcNewLL(p, -90, ((double) (ts - lastPosted) / 60.0 / 60.0 / 1000.0) /*h*/ * speed /*kn*/);
        lat = p1.getLatitude();
        lon = p1.getLongitude();
        lastPosted = ts;
        return m.processPosition(new GeoPositionT(ts + t0, lat, lon), speed);
    }
	
	private List<TrackPoint> cruise(int seconds, double speed) {
		return cruise(seconds, speed, 0);
	}
	
	private List<TrackPoint> cruise(int seconds, double speed, int waitBeforeCruising) {
		int interval = 1000; // 1 second
		List<TrackPoint> out = new ArrayList<>();
		long start = lastPosted + interval + waitBeforeCruising*1000;
		for (long t = start; t<=(seconds*1000)+start; t+=interval) {
			TrackPoint point = postPosition(t, speed);
			if (point!=null) {
				out.add(point);
			}
		}
		return out;
	}

	
	@Test
	public void testFirstPoint() {
		TrackPoint p = postPosition(1000, 0.0);
		assertNull(p); // first point is null because it is figuring out if it's anchored or not
	}
	
	@Test
	public void testSecondPointStationary() {
		postPosition(1000, 0.0);  // first point is null because it is figuring out if it's anchored or not
		TrackPoint p = postPosition(2000, 0.0);
		assertNotNull(p);
        assertTrue(p.isAnchor());
	}
	
	@Test
	public void testSecondPointMoving() {
		postPosition(1000, 0.0);  // first point is null because it is figuring out if it's anchored or not
		TrackPoint p = postPosition(2000, 5.0);
		assertNotNull(p);
        assertTrue(!p.isAnchor());
        assertEquals(5.0, p.getAverageSpeed(), 0.1);
        assertEquals(0.001389, p.getDistance(), 0.000001);
        assertEquals(43.678301, p.getPosition().getLatitude(), 0.000001);
        assertEquals(10.266413, p.getPosition().getLongitude(), 0.000001);
        assertEquals(2000 + t0, p.getPosition().getTimestamp());
		
	}
	
	@Test
	public void testMaxSpeed() throws Exception {
		cruise(30 /* 30s */, 5.0);
		cruise(10 /* 10s */, 6.0);
		List<TrackPoint> l = cruise(20 /* 20s */, 5.0);
		TrackPoint p = l.get(0);
        assertEquals(6.00, p.getMaxSpeed(), 0.01);
        assertEquals((5.0 * 20 + 6.0 * (period - 20)) / period, p.getAverageSpeed(), 0.01);
	}
	
	@Test
	public void testCruiseAt5Kn() throws Exception {
		List<TrackPoint> l = cruise(60 * 60 /* 1h */, 5.0);
		//System.out.println("Reported " + l.size() + "points");
		assertEquals(60/*m*/ * 60 / period, l.size());
		int counter = 0;
		for (TrackPoint p: l) {
            //System.out.println(p.getPeriod() + " " + p.averageSpeed);
            assertTrue(!p.isAnchor());
            assertEquals(5.0, p.getAverageSpeed(), 0.1);
            assertEquals((counter == 0) ? 1 : period, p.getPeriod());
			counter++;
		}
	}
	
	@Test
	public void testSetAnchor() throws Exception {
		cruise(10 * 60 /* 1m */, 5.0);
		List<TrackPoint> l = cruise(60 * 60 /* 1h */, 0.0);
		for (TrackPoint p: l) {
            System.out.println(p.getPeriod() + " " + p.isAnchor() + " " + p.getAverageSpeed());
        }
        assertTrue(l.get(l.size() - 1).isAnchor());
        assertEquals(0.0, l.get(l.size() - 1).getAverageSpeed(), 0.1);
        assertEquals(m.getStaticPeriod() / 1000, l.get(l.size() - 1).getPeriod());
	}
	
	
	@Test
	public void testLeaveAnchor() throws Exception {
		double s = 1.0;
		cruise(10 * 60 /* 1m */, 5.0);
		cruise(47 * 60 /* 1h */, 0.0); // set anchor
		List<TrackPoint> l = cruise(10 * 60 /* 10m */, s);
		//dump(l);
		int counter = 0;
		for (TrackPoint p: l) {
            assertTrue(!p.isAnchor());
			if (counter>0) {
                assertEquals(s, p.getAverageSpeed(), 0.1);
                assertEquals(period, p.getPeriod()); // first depends on how long it has been anchored
			}
			counter++;
		}
		assertTrue(counter>0);
	}

	@Test
	public void testSlowDown() throws Exception {
		cruise(10 * 60 /* 10m */, 5.0);
		List<TrackPoint> l = cruise(10 * 60 /* 10m */, 0.0); //slow down for less than 15m
        assertTrue(!l.get(l.size() - 1).isAnchor());
        assertEquals(period, l.get(l.size() - 1).getPeriod());
	}

	@Test
	public void testSlowDownAndAccelerate() throws Exception {
		cruise(10 * 60 /* 10m */, 5.0);
		cruise(10 * 60 /* 10m */, 0.0); //slow down for less than 15m
		List<TrackPoint> l = cruise(60 * 60 /* 1h */, 5.0); //accelerate again
		for (TrackPoint p: l) {
            assertTrue(!p.isAnchor());
            assertEquals(5.0, p.getAverageSpeed(), 0.1);
            assertEquals(period, p.getPeriod()); // first depends on how long it has been anchored
		}
	}
	
	@Test
	public void testSwingAtAnchor() throws Exception {
		cruise(20 * 60 /* 20m */, 0.0); //solid anchor
		for (int i = 0; i<5*60; i++) { //swing for a few minutes with a period of 30s
			TrackPoint p = postPosition(lastPosted + 1000, 1.0 * Math.sin(2 * Math.PI * (i / 30)));
			if (p!=null) {
                assertTrue(p.isAnchor());
			}
		}
	}
	
	/**
	 * Test the case where the boat reaches the anchor point, set anchor then the GPS switches off for the night and resume after 8h before leaving
	 * @throws Exception
	 */
	@Test
	public void testFeedInterruption() throws Exception {
		// first cruise and set anchor
		cruise(10 * 60 /* 1m */, 5.0);
		cruise(2 * 60 * 60 /* 2h */, 0.0);
		// stop sending for 8h
		//System.out.println("----");
		// switch GPS on and cruise for 10minutes
		List<TrackPoint> l = cruise(10 * 60 /* 10m */, 5.0, 8 * 3600);
		//dump(l);
		assertTrue(l.size()>1);
        assertTrue(!l.get(1).isAnchor());
	}

	/**
	 * Test the case where the boat reaches the anchor point, set anchor then the GPS switches off for the night and resume after 8h before leaving
	 * @throws Exception
	 */
	@Test
	public void testFeedInterruption2() throws Exception {
		// first cruise and set anchor
		dump(cruise(10 * 60 /* 1m */, 5.0));
		dump(cruise(2 * 60 * 60 /* 2h */, 0.0));
		// stop sending for 8h
		System.out.println("----");
		// switch on GPS and remain at anchor
		List<TrackPoint> l = cruise(60 * 60 /* 1h */, 0.0, 8 * 3600);
		dump(l);
		assertTrue(l.size()>1);
        assertTrue(l.get(1).isAnchor());
	}
}
