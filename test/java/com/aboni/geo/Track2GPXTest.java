package com.aboni.geo;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.Test;
import org.w3c.dom.Document;

public class Track2GPXTest {

	
	private PositionHistory createPositionHistory() {
		PositionHistory h = new PositionHistory();
		h.addPosition(new GeoPositionT(10001000, 43.10000000, 10.08000000));
		h.addPosition(new GeoPositionT(10061000, 43.10000100, 10.08000100));
		h.addPosition(new GeoPositionT(10121000, 43.10000200, 10.08000200));
		h.addPosition(new GeoPositionT(10181000, 43.10000300, 10.08000300));
		return h;
	}
	
	@Test
	public void testTrackDefaultName() throws Exception {
		TrackDumper g = new Track2GPX();
		g.setTrack(createPositionHistory());
		StringWriter w = new StringWriter();
		g.dump(w);
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document d = builder.parse(new ByteArrayInputStream(w.toString().getBytes()));
		
		assertEquals(Track2GPX.DEFAULT_TRACK_NAME, d.getElementsByTagName("name").item(0).getTextContent());
	}

	@Test
	public void testTrackName() throws Exception {
		TrackDumper g = new Track2GPX();
		g.setTrack(createPositionHistory());
		g.setTrackName("pippo");
		StringWriter w = new StringWriter();
		g.dump(w);
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document d = builder.parse(new ByteArrayInputStream(w.toString().getBytes()));
		
		assertEquals("pippo", d.getElementsByTagName("name").item(0).getTextContent());
	}
}