package com.aboni.nmea.router.agent.impl;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import com.aboni.misc.Utils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.aboni.geo.Course;
import com.aboni.geo.GeoPositionT;
import com.aboni.nmea.router.NMEACache;

import com.aboni.nmea.router.agent.NMEAAgent;
import com.aboni.nmea.router.agent.QOS;
import com.aboni.utils.ServerLog;

import net.sf.marineapi.nmea.parser.SentenceFactory;
import net.sf.marineapi.nmea.sentence.RMCSentence;
import net.sf.marineapi.nmea.sentence.Sentence;
import net.sf.marineapi.nmea.sentence.SentenceId;
import net.sf.marineapi.nmea.sentence.TalkerId;
import net.sf.marineapi.nmea.util.CompassPoint;
import net.sf.marineapi.nmea.util.FaaMode;


public class NMEAGPXPlayerAgent extends NMEAAgentImpl {

	private final SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
	private Document d;
	private GeoPositionT prevPos;
	private long t0Play;
	private long t0;
	private final String file;
	private boolean stop;
	
	public NMEAGPXPlayerAgent(NMEACache cache, String name, String file, QOS q) {
		super(cache, name, q);
		fmt.setTimeZone(TimeZone.getTimeZone("UTC"));
		this.file = file;
	}
	
	@Override
	public String getDescription() {
		return (file!=null)?("File " + file):"";
	}

	@Override
	protected boolean onActivate() {
		synchronized (this) {
			stop = false;
		}
		return play();
	}
	
	@Override
	protected void onDeactivate() {
		synchronized (this) {
			stop = true;
		}
	}
	
	public boolean play() {
		try {
			d = null;
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			d = dBuilder.parse(file);
			d.getDocumentElement().normalize();
		} catch (Exception e) {
			ServerLog.getLogger().Error("Cannot parse GPX file " + file, e);
			return false;
		}
	
		Thread t = new Thread(() -> {
			Element gpx = d.getDocumentElement();
			NodeList tracks = gpx.getElementsByTagName("trk");
			Element track = (Element)tracks.item(0);
			NodeList segments = track.getElementsByTagName("trkseg");
			for (int i = 0; i<segments.getLength(); i++) {
				if (isStop()) break;
				Element segment = (Element)segments.item(i);
				NodeList points = segment.getElementsByTagName("trkpt");
				for (int j = 1; j<points.getLength(); j++) {
					try {
						Element p = (Element)points.item(j);
						Node t1 = p.getElementsByTagName("time").item(0);
						String sTime = t1.getTextContent();
						Date d = fmt.parse(sTime);
						GeoPositionT pos = new GeoPositionT(d.getTime(), Double.parseDouble(p.getAttribute("lat")), Double.parseDouble(p.getAttribute("lon")));
						doIt(pos);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			if (!isStop()) {
				stop();
			}
		});
		t.setDaemon(true);
		t.start();

		return true;
	}
	

	protected boolean isStop() {
		synchronized (this) {
			return stop;
		}
	}

	private void doIt(GeoPositionT pos) {
		if (t0==0) {
			t0 = System.currentTimeMillis();
			t0Play = pos.getTimestamp();
		}
		if (prevPos!=null) {
			long dt = pos.getTimestamp() - t0Play;
			long elapsed = System.currentTimeMillis() - t0;
			Utils.pause((int)(dt - elapsed));
			Course c = new Course(prevPos, pos);
			RMCSentence s = (RMCSentence)SentenceFactory.getInstance().createParser(TalkerId.GP, SentenceId.RMC);
			s.setCourse(c.getCOG());
			Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
			cal.setTimeInMillis(pos.getTimestamp());
			s.setDate(new net.sf.marineapi.nmea.util.Date(
					cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH)));
			s.setTime(new net.sf.marineapi.nmea.util.Time(
					cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), cal.get(Calendar.SECOND)));
			s.setMode(FaaMode.AUTOMATIC);
			s.setSpeed(c.getSpeed());
			s.setPosition(pos);
			s.setVariation(0.0);
			s.setDirectionOfVariation(CompassPoint.EAST);
			notify(s);
		}
		prevPos = pos;
	}

	@Override
	protected void doWithSentence(Sentence s, NMEAAgent source) {
	}
}
