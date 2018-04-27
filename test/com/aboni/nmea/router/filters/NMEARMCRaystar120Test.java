package com.aboni.nmea.router.filters;

import static org.junit.Assert.*;

import java.util.Calendar;
import java.util.TimeZone;

import org.junit.Before;
import org.junit.Test;

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


	@Test
	public void test() {
		NMEARMCRaystar120 r120 = new NMEARMCRaystar120();
		
		
		
		Calendar start = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		
		// run for 1 minute
		for (int i = 0; i<60; i++) {
			
			
			RMCSentence r = (RMCSentence)SentenceFactory.getInstance().createParser(TalkerId.GP, SentenceId.RMC);
			r.setCourse(180.0);
			r.setMode(FaaMode.AUTOMATIC);
			r.setSpeed(5.6);
			r.setStatus(DataStatus.ACTIVE);
			r.setPosition(new Position(43.3780717, 10.1760560));

			Calendar c = (Calendar)start.clone();
			c.setTimeInMillis(start.getTimeInMillis() + ((i/10)*10) * 1000);
			Time newTime = new Time(c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), c.get(Calendar.SECOND), 0, 0);
			Date newDate = new Date(c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH));
			r.setTime(newTime);
			r.setDate(newDate);

			r120.setOverrideTime(start.getTimeInMillis() + i * 1000);
			
			System.out.println(r);
			r120.process(r, "X");
			System.out.println(r);
			System.out.println("---");
						
		}
		
		
		
		
		


	
	}

}
