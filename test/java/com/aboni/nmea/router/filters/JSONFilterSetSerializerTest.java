package com.aboni.nmea.router.filters;

import com.aboni.nmea.router.filters.impl.JSONFilterSetSerializer;
import com.aboni.nmea.router.filters.impl.NMEABasicSentenceFilter;
import com.aboni.nmea.router.filters.impl.NMEAFilterSetImpl;
import com.aboni.nmea.router.filters.impl.NMEAFilterSetImpl.TYPE;
import com.aboni.nmea.router.filters.impl.STalkFilter;
import org.junit.Test;

import java.util.Iterator;

import static org.junit.Assert.*;

public class JSONFilterSetSerializerTest {

	@Test
	public void testExportFilterDefault() {
        NMEAFilterSetImpl fs = new NMEAFilterSetImpl(TYPE.BLACKLIST);
        fs.addFilter(new NMEABasicSentenceFilter("MMB"));
        fs.addFilter(new NMEABasicSentenceFilter("MTA"));

        String s = new JSONFilterSetSerializer().exportFilter(fs);

        assertNotNull(s);

        System.out.println(s);
    }

	@Test
	public void testExportFilterWhite() {
        NMEAFilterSetImpl fs = new NMEAFilterSetImpl(TYPE.WHITELIST);
        fs.addFilter(new NMEABasicSentenceFilter("MMB"));
        fs.addFilter(new NMEABasicSentenceFilter("MTA"));
        String s = new JSONFilterSetSerializer().exportFilter(fs);

        assertNotNull(s);

        System.out.println(s);
    }

	@Test
	public void testImportFilter() {
        String s = "{\"filters\":[{\"sentence\":\"MMB\",\"source\":\"\"},{\"sentence\":\"MTA\",\"source\":\"\"}],\"type\":\"whitelist\"}";
        NMEAFilterSet fs = new JSONFilterSetSerializer().importFilter(s);
        assertNotNull(fs);
        assertEquals(TYPE.WHITELIST, ((NMEAFilterSetImpl) fs).getType());
        Iterator<NMEAFilter> i = fs.getFilters();
        assert (i.hasNext());
        NMEABasicSentenceFilter f = (NMEABasicSentenceFilter) i.next();
        assertEquals("", f.getSource());
        assertNull(f.getTalkerId());
        assertEquals("MMB", f.getSentenceId());

        assert (i.hasNext());
        NMEABasicSentenceFilter f1 = (NMEABasicSentenceFilter) i.next();
        assertEquals("", f1.getSource());
        assertNull(f1.getTalkerId());
		assertEquals("MTA", f1.getSentenceId());
		
		assert(!i.hasNext());
			
	}

	@Test
	public void testImportFilter1() {
        String s = "{\"filters\":[{\"sentence\":\"STALK:!84\",\"source\":\"\"}],\"type\":\"blacklist\"}";
        NMEAFilterSet fs = new JSONFilterSetSerializer().importFilter(s);
        assertNotNull(fs);
        assertEquals(TYPE.BLACKLIST, ((NMEAFilterSetImpl) fs).getType());
        Iterator<NMEAFilter> i = fs.getFilters();
        assert (i.hasNext());
        STalkFilter f = (STalkFilter) i.next();
        assertTrue(f.isNegate());
        assertEquals("84", f.getCommand());

        assert (!i.hasNext());
    }
	
}
