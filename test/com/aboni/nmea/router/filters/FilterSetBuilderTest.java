package com.aboni.nmea.router.filters;

import static org.junit.Assert.*;

import java.util.Iterator;

import org.junit.Test;

import com.aboni.nmea.router.filters.NMEAFilterSet.TYPE;

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

}