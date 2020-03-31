package com.aboni.geo;

import com.aboni.geo.impl.DeviationManagerImpl;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DeviationManagerTest {

	private DeviationManagerImpl m;
	
	@Before
	public void setUp() {
		m = new DeviationManagerImpl(); 
	}

	@Test
	public void testNorth() {
		loadMock(m);
		assertEquals(5.0,  m.getMagnetic(0.0), 0.01);
	}

	@Test
	public void testSouth() {
		loadMock(m);
		assertEquals(180.0,  m.getMagnetic(180.0), 0.01);
	}

	@Test
	public void testEast() {
		loadMock(m);
		assertEquals(92.5,  m.getMagnetic(90.0), 0.01);
	}

	@Test
	public void testWest() {
		loadMock(m);
		assertEquals(272.5,  m.getMagnetic(270.0), 0.01);
	}

	@Test
	public void test001() {
		loadMock(m);
		assertEquals(5.9, m.getMagnetic(1.0), 0.1);
	}

	@Test
	public void test359() {
		loadMock(m);
		assertEquals(3.9, m.getMagnetic(359.0), 0.1);
	}

	@Test
	public void testInvalidKey() {
		loadMock(m);
		m.add(-31, 330);
		assertEquals(3.9, m.getMagnetic(359.0), 0.1);
	}

	@Test
	public void testMinus1() {
		loadMock(m);
		m.getMagnetic(-1.0);
		assertEquals(3.9, m.getMagnetic(-1.0), 0.1);
	}

	@Test
	public void testConsistency() {
		loadMock1();
		//double d = m.getMagnetic(221.0);
		assertTrue(m.getMagnetic(221.0)>0);

	}

	@Test
	public void testAdd() throws IOException {
		m.add(90.0, 91.3);
		assertEquals(1, m.size());
		System.out.println(dump());
	}

	@Test
	public void testAddLast() throws IOException {
		m.add(90.0, 91.3);
		m.add(94.0, 95.3);
		assertEquals(2, m.size());
		System.out.println(dump());
	}

	@Test
	public void testReplace() throws IOException {
		m.add(90.0, 91.3);
		m.add(90.0, 90.3);
		assertEquals(1, m.size());
		System.out.println(dump());
	}

	@Test
	public void testAddFirst() throws IOException {
		m.add(94.0, 95.3);
		m.add(90.0, 91.3);
		assertEquals(2, m.size());
		System.out.println(dump());
	}

	@Test
	public void testReset() {
		m.load(new ByteArrayInputStream("0,5.0\r\n90,92.5\r\n180,180.0\r\n270,272.5\r\n".getBytes()));
		m.reset();
		assertEquals(0, m.size());
	}

	private void loadMock(DeviationManagerImpl m) {
		// +5 for 0 and decreases to 0 for 180
		for (int i = 0; i < 360; i += 90) {
			double a = (double) i + (180.0 - (i < 180 ? (double) i : (360 - (double) i))) / 180.0 * 5.0;
			m.add(i, a);
		}
	}

	private void loadMock1() {
		String s =
				"0,40\r\n" +
						"90,135\r\n" +
						"180,220\r\n" +
						"270,10";

		m.load(new ByteArrayInputStream(s.getBytes()));
	}

	private String dump() throws IOException {
		ByteArrayOutputStream o = new ByteArrayOutputStream();
		m.dump(o);
		return o.toString();
	}
}
