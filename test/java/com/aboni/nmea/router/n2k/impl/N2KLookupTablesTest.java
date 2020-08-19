package com.aboni.nmea.router.n2k.impl;

import com.aboni.nmea.router.n2k.N2KLookupTables;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class N2KLookupTablesTest {
  /*
  "GNS": {
    "0": "GPS",
    "1": "GLONASS",
    "2": "GPS+GLONASS",
    "3": "GPS+SBAS/WAAS",
    "4": "GPS+SBAS/WAAS+GLONASS",
    "5": "Chayka",
    "6": "integrated",
    "7": "surveyed",
    "8": "Galileo"
  }
   */

    @Test
    public void testGNS() {
        Map<Integer, String> m = N2KLookupTables.getTable(N2KLookupTables.LOOKUP_MAPS.GNS);
        assertNotNull(m);
        assertEquals(9, m.size());
        assertEquals("GPS", m.get(0));
        assertEquals("GLONASS", m.get(1));
        assertEquals("GPS+GLONASS", m.get(2));
        assertEquals("GPS+SBAS/WAAS", m.get(3));
        assertEquals("GPS+SBAS/WAAS+GLONASS", m.get(4));
        assertEquals("Chayka", m.get(5));
        assertEquals("integrated", m.get(6));
        assertEquals("surveyed", m.get(7));
        assertEquals("Galileo", m.get(8));
    }

}