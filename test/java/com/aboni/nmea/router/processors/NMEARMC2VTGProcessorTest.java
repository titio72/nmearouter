/*
 * Copyright (c) 2020,  Andrea Boni
 * This file is part of NMEARouter.
 * NMEARouter is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * NMEARouter is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with NMEARouter.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.aboni.nmea.router.processors;

import com.aboni.nmea.router.NMEARouterModule;
import com.aboni.nmea.router.message.Message;
import com.aboni.nmea.router.nmea0183.NMEA0183Message;
import com.aboni.utils.ThingsFactory;
import com.google.inject.Guice;
import com.google.inject.Injector;
import net.sf.marineapi.nmea.parser.SentenceFactory;
import net.sf.marineapi.nmea.sentence.Sentence;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class NMEARMC2VTGProcessorTest {

	@Before
	public void setUp() {
		Injector injector = Guice.createInjector(new NMEARouterModule());
		ThingsFactory.setInjector(injector);
	}

	@Test
	public void test() throws NMEARouterProcessorException {
		NMEARMC2VTGProcessor p = new NMEARMC2VTGProcessor(2016);
		Sentence rmc = SentenceFactory.getInstance().createParser("$GPRMC,074128.000,A,4337.837,N,01017.600,E,5.9,345.9,220117,000.0,W,S*03");
		Sentence vtg = SentenceFactory.getInstance().createParser("$GPVTG,345.9,T,343.4,M,5.90,N,10.93,K,A*1F");
		Message[] res = p.process(new NMEA0183Message(rmc), "SRC").second;
		assertEquals(1, res.length);
		assertEquals(vtg, ((NMEA0183Message)res[0]).getSentence());
	}

	@Test
	public void testNeg() throws NMEARouterProcessorException {
		NMEARMC2VTGProcessor p = new NMEARMC2VTGProcessor(2016);
		Message x = NMEA0183Message.get(SentenceFactory.getInstance().createParser("$GPMTW,28.50,C*3B"));
		Message[] res = p.process(x, "SRC").second;
		assertNull(res);
	}

}
