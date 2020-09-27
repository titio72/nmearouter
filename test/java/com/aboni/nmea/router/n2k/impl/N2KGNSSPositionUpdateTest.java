package com.aboni.nmea.router.n2k.impl;

import com.aboni.nmea.router.n2k.messages.N2KGNSSPositionUpdate;
import com.aboni.nmea.router.n2k.messages.impl.N2KGNSSPositionUpdateImpl;
import org.junit.Test;

import java.time.Instant;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class N2KGNSSPositionUpdateTest {

    @Test
    public void testGNSSUpdateOk() {
        N2KGNSSPositionUpdate s = new N2KGNSSPositionUpdateImpl(new byte[]{
                (byte) 0x2a, (byte) 0x02, (byte) 0x48, (byte) 0xc0, (byte) 0x45, (byte) 0xbb,
                (byte) 0x11, (byte) 0x00, (byte) 0xb0, (byte) 0x6d, (byte) 0xa6, (byte) 0x1c, (byte) 0xb1,
                (byte) 0xf9, (byte) 0x05, (byte) 0x00, (byte) 0x96, (byte) 0x59, (byte) 0xf2, (byte) 0xc5,
                (byte) 0xa8, (byte) 0x5d, (byte) 0x01, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
                (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0x7f, (byte) 0x13, (byte) 0xfc, (byte) 0x08,
                (byte) 0xc9, (byte) 0x01, (byte) 0xff, (byte) 0x7f, (byte) 0xff, (byte) 0xff, (byte) 0xff,
                (byte) 0x7f, (byte) 0x00, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff});

        /*
        "SID":42,
        "Date":"2020.06.21",
        "Time": "08:15:48.05760",
        "Latitude":43.0569976,
        "Longitude": 9.8420335,
        "GNSS type":"GPS+SBAS/WAAS",
        "Method":"GNSS fix",
        "Integrity":"No integrity checking",
        "Number of SVs":8,
        "HDOP":4.57,
        "Reference Stations":0}
         */

        assertEquals(42, s.getSID());
        assertEquals("GPS+SBAS/WAAS", s.getGnssType());
        assertEquals("GNSS fix", s.getMethod());
        assertEquals(Instant.parse("2020-06-21T08:15:48.576Z"), s.getTimestamp());
        assertEquals(43.0569976, s.getPosition().getLatitude(), 0.00000001);
        assertEquals(9.8420335, s.getPosition().getLongitude(), 0.00000001);
        assertEquals(8, s.getNSatellites());
        assertEquals(4.57, s.getHDOP(), 0.001);
        assertNaN(s.getAltitude());
        assertNaN(s.getPDOP());
        assertNaN(s.getGeoidalSeparation());
        assertNaN(s.getAgeOfDgnssCorrections());
        assertEquals(0, s.getReferenceStations());
    }

    void assertNaN(double d) {
        assertTrue(Double.isNaN(d));
    }
}