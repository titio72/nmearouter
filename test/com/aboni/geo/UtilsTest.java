package com.aboni.geo;

import static org.junit.Assert.*;

import org.junit.Test;

public class UtilsTest {

	@Test
	public void testNormalizeDegrees0_360() {
		assertEquals(30.0, Utils.normalizeDegrees0_360(30.0), 0.0001);
		assertEquals(330.0, Utils.normalizeDegrees0_360(-30.0), 0.0001);
		assertEquals(180.0, Utils.normalizeDegrees0_360(180.0), 0.0001);
		assertEquals(180.0, Utils.normalizeDegrees0_360(-180.0), 0.0001);
		assertEquals(270.0, Utils.normalizeDegrees0_360(270.0), 0.0001);
		assertEquals(270.0, Utils.normalizeDegrees0_360(-90.0), 0.0001);
	}

	@Test
	public void testNormalizeDegrees180_180() {
		assertEquals( 30.0, Utils.normalizeDegrees180_180(30.0), 0.0001);
		assertEquals(-30.0, Utils.normalizeDegrees180_180(330.0), 0.0001);
	}

	@Test
	public void testGetLatitudeEmisphere() {
		assertEquals("E", Utils.getLongitudeEmisphere(11.0));
		assertEquals("W", Utils.getLongitudeEmisphere(-11.0));
	}

	@Test
	public void testGetLongitudeEmisphere() {
		assertEquals("N", Utils.getLatitudeEmisphere(43.0));
		assertEquals("S", Utils.getLatitudeEmisphere(-43.0));
	}

	@Test
	public void testGetSignedLatitude() {
		assertEquals( 43.0, Utils.getSignedLatitude(43.0, 'N'), 0.00001);
		assertEquals(-43.0, Utils.getSignedLatitude(43.0, 'S'), 0.00001);
	}

	@Test
	public void testGetSignedLongitude() {
		assertEquals( 11.0,  Utils.getSignedLongitude(11.0, 'E'), 0.00001);
		assertEquals(-11.0,  Utils.getSignedLongitude(11.0, 'W'), 0.00001);
	}

	@Test
	public void testGetNormal360Ref() {
		assertEquals(31.0, Utils.getNormal(30.0, 31.0), 0.00001);
		assertEquals( 1.0, Utils.getNormal(0.0,  1.0), 0.00001);
		assertEquals(-1.0, Utils.getNormal(0.0, -1.0), 0.00001);
		assertEquals(-1.0, Utils.getNormal(0.0, 359.0), 0.00001);
		assertEquals(-1.0, Utils.getNormal(10.0, 359.0), 0.00001);
		assertEquals(-150.0, Utils.getNormal(0.0, 210.0), 0.00001);
		assertEquals(-90.0, Utils.getNormal(-90.0, 270.0), 0.00001);
		assertEquals(2.0, Utils.getNormal(-1.0, 2.0), 0.00001);
		assertEquals(392.0, Utils.getNormal(359.0, 32.0), 0.00001);
	}
	

}
