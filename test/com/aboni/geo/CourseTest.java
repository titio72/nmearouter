package com.aboni.geo;

import static org.junit.Assert.*;

import org.junit.Test;

public class CourseTest {

	@Test
	public void testGoEast() {
		long t0 = System.currentTimeMillis();
		long t1 = t0 + 60000 /* 1m */;
		GeoPositionT p0 = new GeoPositionT(t0, 43.67,  9.84);
		GeoPositionT p1 = new GeoPositionT(t1, 43.67, 9.84 + 0.001913583 /* 154.3m == 5Kn*/);
		Course c = new Course(p0, p1);
		
		//System.out.format("d %.3f - h %.1f° - s %.3f - dt %d%n", c.getDistance(), c.getCOG(), c.getSpeed(), c.getInterval());
		assertEquals(90.0, c.getCOG(), 0.01);
		assertEquals(60000, c.getInterval());
		assertEquals(5.0, c.getSpeed(), 0.01);
		assertEquals(0.083333333, c.getDistance(), 0.00001);
	}

	@Test
	public void testGoWest() {
		long t0 = System.currentTimeMillis();
		long t1 = t0 + 60000 /* 1m */;
		GeoPositionT p0 = new GeoPositionT(t0, 43.67,  9.84);
		GeoPositionT p1 = new GeoPositionT(t1, 43.67, 9.84 - 0.001913583 /* 154.3m == 5Kn*/);
		Course c = new Course(p0, p1);
		
		//System.out.format("d %.3f - h %.1f° - s %.3f - dt %d%n", c.getDistance(), c.getCOG(), c.getSpeed(), c.getInterval());
		assertEquals(270.0, c.getCOG(), 0.01);
		assertEquals(60000, c.getInterval());
		assertEquals(5.0, c.getSpeed(), 0.01);
		assertEquals(0.083333333, c.getDistance(), 0.00001);
	}

	@Test
	public void testGoNorth() {
		long t0 = System.currentTimeMillis();
		long t1 = t0 + 60000 /* 1m */;
		GeoPositionT p0 = new GeoPositionT(t0, 43.67, 9.84);
		GeoPositionT p1 = new GeoPositionT(t1, 43.67 + 0.0013889, 9.84);
		Course c = new Course(p0, p1);
		
		//System.out.format("d %.3f - h %.1f° - s %.3f - dt %d%n", c.getDistance(), c.getCOG(), c.getSpeed(), c.getInterval());
		assertEquals(0.0, c.getCOG(), 0.01);
		assertEquals(60000, c.getInterval());
		assertEquals(5.0, c.getSpeed(), 0.01);
		assertEquals(0.083333333, c.getDistance(), 0.00001);
	}

	@Test
	public void testGoSouth() {
		long t0 = System.currentTimeMillis();
		long t1 = t0 + 60000 /* 1m */;
		GeoPositionT p0 = new GeoPositionT(t0, 43.67, 9.84);
		GeoPositionT p1 = new GeoPositionT(t1, 43.67 - 0.0013889, 9.84);
		Course c = new Course(p0, p1);
		
		//System.out.format("d %.3f - h %.1f° - s %.3f - dt %d%n", c.getDistance(), c.getCOG(), c.getSpeed(), c.getInterval());
		assertEquals(180.0, c.getCOG(), 0.01);
		assertEquals(60000, c.getInterval());
		assertEquals(5.0, c.getSpeed(), 0.01);
		assertEquals(0.083333333, c.getDistance(), 0.00001);
	}

	@Test
	public void testGoNE() {
		long t0 = System.currentTimeMillis();
		long t1 = t0 + 60000 /* 1m */;
		GeoPositionT p0 = new GeoPositionT(t0, 43.67, 9.84);
		GeoPositionT p1 = new GeoPositionT(t1, 43.67 + 0.0013889, 9.84 + 0.001913583);
		Course c = new Course(p0, p1);
		
		//System.out.format("d %.3f - h %.1f° - s %.3f - dt %d%n", c.getDistance(), c.getCOG(), c.getSpeed(), c.getInterval());
		assertEquals(45.0, c.getCOG(), 0.01);
		assertEquals(60000, c.getInterval());
		assertEquals(7.07, c.getSpeed(), 0.01);
		assertEquals(0.11785, c.getDistance(), 0.00001);
	}

	@Test
	public void testGoSE() {
		long t0 = System.currentTimeMillis();
		long t1 = t0 + 60000 /* 1m */;
		GeoPositionT p0 = new GeoPositionT(t0, 43.67, 9.84);
		GeoPositionT p1 = new GeoPositionT(t1, 43.67 - 0.0013889, 9.84 + 0.001913583);
		Course c = new Course(p0, p1);
		
		//System.out.format("d %.3f - h %.1f° - s %.3f - dt %d%n", c.getDistance(), c.getCOG(), c.getSpeed(), c.getInterval());
		assertEquals(135.0, c.getCOG(), 0.01);
		assertEquals(60000, c.getInterval());
		assertEquals(7.07, c.getSpeed(), 0.01);
		assertEquals(0.11785, c.getDistance(), 0.00001);
	}

	@Test
	public void testGoSW() {
		long t0 = System.currentTimeMillis();
		long t1 = t0 + 60000 /* 1m */;
		GeoPositionT p0 = new GeoPositionT(t0, 43.67, 9.84);
		GeoPositionT p1 = new GeoPositionT(t1, 43.67 - 0.0013889, 9.84 - 0.001913583);
		Course c = new Course(p0, p1);
		
		//System.out.format("d %.3f - h %.1f° - s %.3f - dt %d%n", c.getDistance(), c.getCOG(), c.getSpeed(), c.getInterval());
		assertEquals(225.0, c.getCOG(), 0.01);
		assertEquals(60000, c.getInterval());
		assertEquals(7.07, c.getSpeed(), 0.01);
		assertEquals(0.11785, c.getDistance(), 0.00001);
	}

	@Test
	public void testGoNW() {
		long t0 = System.currentTimeMillis();
		long t1 = t0 + 60000 /* 1m */;
		GeoPositionT p0 = new GeoPositionT(t0, 43.67, 9.84);
		GeoPositionT p1 = new GeoPositionT(t1, 43.67 + 0.0013889, 9.84 - 0.001913583);
		Course c = new Course(p0, p1);
		
		//System.out.format("d %.3f - h %.1f° - s %.3f - dt %d%n", c.getDistance(), c.getCOG(), c.getSpeed(), c.getInterval());
		assertEquals(315.0, c.getCOG(), 0.01);
		assertEquals(60000, c.getInterval());
		assertEquals(7.07, c.getSpeed(), 0.01);
		assertEquals(0.11785, c.getDistance(), 0.00001);
	}



}
