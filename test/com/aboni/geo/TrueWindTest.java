package com.aboni.geo;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class TrueWindTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void test() {
		TrueWind a = new TrueWind(6.4, 135, 15);
		System.out.println(a.getTrueWindSpeed() + " " + a.getTrueWindDeg());
	}

}
