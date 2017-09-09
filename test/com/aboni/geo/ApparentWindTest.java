package com.aboni.geo;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class ApparentWindTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void test() {
		ApparentWind a = new ApparentWind(6, 120, 6);
		assertEquals(6, a.getApparentWindSpeed(), 0.001);
		assertEquals(60, a.getApparentWindDeg(), 0.001);
	}

	@Test
	public void testRun() {
		ApparentWind a = new ApparentWind(5, 180, 6);
		assertEquals(1.0, a.getApparentWindSpeed(), 0.001);
		assertEquals(180, a.getApparentWindDeg(), 0.001);
	}

	@Test
	public void testStarboard() {
		ApparentWind a = new ApparentWind(6, 90, 6);
		assertEquals(6.0 * Math.sqrt(2.0), a.getApparentWindSpeed(), 0.001);
		assertEquals(45, a.getApparentWindDeg(), 0.001);
	}

	@Test
	public void testPort() {
		ApparentWind a = new ApparentWind(6, -90, 6);
		assertEquals(6.0 * Math.sqrt(2.0), a.getApparentWindSpeed(), 0.001);
		assertEquals(315, a.getApparentWindDeg(), 0.001);
	}

}
