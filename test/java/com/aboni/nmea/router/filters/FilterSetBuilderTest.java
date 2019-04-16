package com.aboni.nmea.router.filters;

import com.aboni.nmea.router.filters.NMEAFilterSet.TYPE;
import com.aboni.nmea.sentences.NMEASentenceFilter;
import org.junit.Test;

import java.util.Iterator;

import static org.junit.Assert.*;

public class FilterSetBuilderTest {

	@Test
	public void testExportFilterDefault() {
		NMEAFilterSet fs = new NMEAFilterSet();
		fs.addFilter(new NMEABasicSentenceFilter("MMB"));
		fs.addFilter(new NMEABasicSentenceFilter("MTA"));
		
		String s = new FilterSetBuilder().exportFilter(fs);
		
		assertNotNull(s);
		
		System.out.println(s);
	}

	@Test
	public void testExportFilterWhite() {
		NMEAFilterSet fs = new NMEAFilterSet();
		fs.addFilter(new NMEABasicSentenceFilter("MMB"));
		fs.addFilter(new NMEABasicSentenceFilter("MTA"));
		fs.setType(TYPE.WHITELIST);
		String s = new FilterSetBuilder().exportFilter(fs);
		
		assertNotNull(s);
		
		System.out.println(s);
	}

	@Test
	public void testImportFilter() {
		String s = "{\"filters\":[{\"sentence\":\"MMB\",\"source\":\"\"},{\"sentence\":\"MTA\",\"source\":\"\"}],\"type\":\"whitelist\"}";
		NMEASentenceFilterSet fs = new FilterSetBuilder().importFilter(s);
		assertNotNull(fs);
		assertEquals(TYPE.WHITELIST, ((NMEAFilterSet)fs).getType());
		Iterator<NMEASentenceFilter> i = fs.getFilters();
		assert(i.hasNext());
		NMEABasicSentenceFilter f = (NMEABasicSentenceFilter)i.next();
		assertEquals("", f.getSource());
		assertNull(f.getTalkerId());
		assertEquals("MMB", f.getSentenceId());

		assert(i.hasNext());
		NMEABasicSentenceFilter f1 = (NMEABasicSentenceFilter)i.next();
		assertEquals("", f1.getSource());
		assertNull(f1.getTalkerId());
		assertEquals("MTA", f1.getSentenceId());
		
		assert(!i.hasNext());
			
	}

	@Test
	public void testImportFilter1() {
		String s = 	"{\"filters\":[{\"sentence\":\"STALK:!84\",\"source\":\"\"}],\"type\":\"blacklist\"}";
		NMEASentenceFilterSet fs = new FilterSetBuilder().importFilter(s);
		assertNotNull(fs);
		assertEquals(TYPE.BLACKLIST, ((NMEAFilterSet)fs).getType());
		Iterator<NMEASentenceFilter> i = fs.getFilters();
		assert(i.hasNext());
		STalkFilter f = (STalkFilter)i.next();
		assertTrue(f.isNegate());
		assertEquals("84", f.getCOmmand());
		
		assert(!i.hasNext());
	}		
	
}
