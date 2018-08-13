package com.aboni.nmea.router.filters;

import static org.junit.Assert.*;

import java.util.Calendar;
import java.util.TimeZone;

import org.junit.Before;
import org.junit.Test;

import com.aboni.misc.Utils;
import com.aboni.nmea.router.processors.NMEARMCRaystar120;
import com.aboni.nmea.sentences.NMEAUtils;

import net.sf.marineapi.nmea.parser.SentenceFactory;
import net.sf.marineapi.nmea.sentence.RMCSentence;
import net.sf.marineapi.nmea.sentence.Sentence;
import net.sf.marineapi.nmea.sentence.SentenceId;
import net.sf.marineapi.nmea.sentence.TalkerId;
import net.sf.marineapi.nmea.util.DataStatus;
import net.sf.marineapi.nmea.util.Date;
import net.sf.marineapi.nmea.util.FaaMode;
import net.sf.marineapi.nmea.util.Position;
import net.sf.marineapi.nmea.util.Time;

public class NMEARMCRaystar120Test {

	@Before
	public void setUp() throws Exception {
	}
	
	//[1519974080105][  ] $GNRMC,070120.00,A,4337.80717,N,01017.60560,E,0.134,,020318,,,D*69

	private RMCSentence getRMC(long baseTimeMs, int timeOffsetS, double lat, double lon) {
		RMCSentence r = (RMCSentence)SentenceFactory.getInstance().createParser(TalkerId.GP, SentenceId.RMC);
		r.setCourse(180.0);
		r.setMode(FaaMode.AUTOMATIC);
		r.setSpeed(5.6);
		r.setStatus(DataStatus.ACTIVE);
		r.setPosition(new Position(lat, lon));
		Calendar c = Calendar.getInstance();
		int _r120_offset = (timeOffsetS / 10) * 10;
		c.setTimeInMillis(baseTimeMs + _r120_offset * 1000);
		Time newTime = new Time(c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), c.get(Calendar.SECOND), 0, 0);
		Date newDate = new Date(c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH));
		r.setTime(newTime);
		r.setDate(newDate);
		return r;
	}
	
	@Test
	public void testSyncTime() {
		NMEARMCRaystar120 r120 = new NMEARMCRaystar120();
		Calendar start = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		long _start = (start.getTimeInMillis()/1000)*1000; // round to the seconds, just to reduce complexity
		// run for 1 minute
		for (int i = 0; i<60; i++) {
			int _r120_offset = (i / 10) * 10;
			int _real_offset = i;
			RMCSentence r = getRMC(_start, _r120_offset, 43.3780717, 10.1760560);
			r120.process(r, "X");
			if (i>15) /* allow enough time to sync up */ {
				Calendar timestamp = NMEAUtils.getTimestamp(r.getTime(), r.getDate());
				assertEquals(DataStatus.ACTIVE, r.getStatus());
				assertEquals(timestamp.getTimeInMillis() - _start, _real_offset * 1000);
			}
		}
	}
	

	@Test
	public void testDiscardOddValues() {
		NMEARMCRaystar120 r120 = new NMEARMCRaystar120();
		Calendar start = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		long _start = (start.getTimeInMillis()/1000)*1000; // round to the seconds, just to reduce complexity

		int _N = 15;
		
		// run for a few seconds regularly
		for (int i = 0; i<_N; i++) {
			RMCSentence r = getRMC(_start,  i, 43.3780717, 10.1760560);
			r120.process(r, "X");
		}
		
		// send down a "wrong" position
		RMCSentence r = getRMC(_start,  _N, 43.3780717, -10.1760560 /* way off */);
		r120.process(r, "X");

		assertEquals(DataStatus.VOID, r.getStatus());
	}

	@Test
	public void testAcceptGoodValues() {
		NMEARMCRaystar120 r120 = new NMEARMCRaystar120();
		Calendar start = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		long _start = (start.getTimeInMillis()/1000)*1000; // round to the seconds, just to reduce complexity

		int _N = 15;
		
		// run for a few seconds regularly
		for (int i = 0; i<_N; i++) {
			RMCSentence r = getRMC(_start,  i, 43.3780717, 10.1760560);
			r120.process(r, "X");
		}
		
		// send down a "wrong" position
		RMCSentence r = getRMC(_start,  _N, 43.3780717, 10.1760560);
		r120.process(r, "X");

		assertEquals(DataStatus.ACTIVE, r.getStatus());
	}

	
}
