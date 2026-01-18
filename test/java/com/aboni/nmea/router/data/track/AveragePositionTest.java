package com.aboni.nmea.router.data.track;

import com.aboni.geo.GeoPositionT;
import net.sf.marineapi.nmea.util.Position;
import org.junit.Test;

import static org.junit.Assert.*;

public class AveragePositionTest {
    @Test
    public void testSinglePosition() {
        AveragePosition avg = new AveragePosition();
        GeoPositionT pos = new GeoPositionT(1000L, 43.05, 9.84);
        avg.addPosition(pos);
        Position result = avg.getAveragePosition();
        assertEquals(43.05, result.getLatitude(), 1e-9);
        assertEquals(9.84, result.getLongitude(), 1e-9);
    }

    @Test
    public void testMultiplePositionsWithinPeriod() {
        AveragePosition avg = new AveragePosition();
        avg.addPosition(new GeoPositionT(1000L, 43.05, 9.84));
        avg.addPosition(new GeoPositionT(2000L, 43.06, 9.85));
        avg.addPosition(new GeoPositionT(3000L, 43.07, 9.86));
        Position result = avg.getAveragePosition();
        assertEquals((43.05+43.06+43.07)/3, result.getLatitude(), 1e-9);
        assertEquals((9.84+9.85+9.86)/3, result.getLongitude(), 1e-9);
    }

    @Test
    public void testOldPositionsAreRemoved() {
        AveragePosition avg = new AveragePosition();
        long base = 1_000_000L;
        // Add a position far in the past
        avg.addPosition(new GeoPositionT(base, 43.05, 9.84));
        // Add positions within 5 minutes
        avg.addPosition(new GeoPositionT(base + 1, 43.06, 9.85));
        avg.addPosition(new GeoPositionT(base + 2, 43.07, 9.86));
        // Add a position more than 5 minutes after the first
        long afterPeriod = base + 5*60000L + 1;
        avg.addPosition(new GeoPositionT(afterPeriod, 43.08, 9.87));
        Position result = avg.getAveragePosition();
        // The first position should be removed from the average
        double expectedLat = 43.07;
        double expectedLon = 9.86;
        assertEquals(expectedLat, result.getLatitude(), 1e-9);
        assertEquals(expectedLon, result.getLongitude(), 1e-9);
    }
}
