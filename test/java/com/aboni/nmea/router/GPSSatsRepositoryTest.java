package com.aboni.nmea.router;

import com.aboni.nmea.router.GPSSat;
import com.aboni.nmea.router.GPSSatsRepository;
import org.junit.Test;

import static org.junit.Assert.*;

public class GPSSatsRepositoryTest {

    @Test
    public void testExist() {
        assertNotNull(GPSSatsRepository.getSat(1));
    }

    @Test
    public void testDONotExist() {
        assertNull(GPSSatsRepository.getSat(12348756));
    }

    @Test
    public void test() {
        //13,43,GPS 2R-2,1997/7/23,MEO,L1C/A,RB
        GPSSat sat = GPSSatsRepository.getSat(13);
        assertEquals(13, sat.getPrn());
        assertEquals("L1C/A", sat.getSignal());
        assertEquals("RB", sat.getClock());
        assertEquals("GPS 2R-2", sat.getName());
        assertEquals("MEO", sat.getOrbit());
        assertEquals(43, sat.getSvn());
        assertEquals("1997/7/23", sat.getDate());
    }

}