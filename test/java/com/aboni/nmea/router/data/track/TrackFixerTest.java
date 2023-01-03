/*
 * Copyright (c) 2022,  Andrea Boni
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

package com.aboni.nmea.router.data.track;

import com.aboni.geo.GeoPositionT;
import com.aboni.nmea.router.data.track.impl.TrackPointBuilderImpl;
import com.aboni.sensors.EngineStatus;
import org.junit.Test;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class TrackFixerTest {

    private static final TrackPointBuilder builder = new TrackPointBuilderImpl();
    private final List<TrackPoint> track = new ArrayList<>();

    private static TrackPoint newPoint(double lat, double lon, String ts, int id, int anchor, int dTime, double dist, double speed, double maxSpeed, int engine) {
        OffsetDateTime d = OffsetDateTime.parse(ts + "+01:00");
        return builder.
                withPosition(new GeoPositionT(d.toInstant().toEpochMilli(), lat, lon)).
                withAnchor(anchor == 1).
                withDistance(dist).
                withPeriod(dTime).
                withSpeed(speed, maxSpeed).
                withEngine(EngineStatus.valueOf(engine)).
                getPoint();
    }

    void load() {
        /* 00 */
        track.add(newPoint(43.5690536, 10.1874104, "2022-12-19T12:19:49", 781142, 0, 30, 0.05489435, 6.59, 7.02, 0));
        /* 01 */
        track.add(newPoint(43.5699234, 10.1878052, "2022-12-19T12:20:19", 781143, 0, 30, 0.05494621, 6.59, 6.98, 0));
        /* 02 */
        track.add(newPoint(43.5707893, 10.1882162, "2022-12-19T12:20:49", 781144, 0, 30, 0.0549555, 6.59, 6.82, 0));
        /* 03 */
        track.add(newPoint(43.5716286, 10.1886072, "2022-12-19T12:21:19", 781145, 0, 30, 0.05315701, 6.38, 6.59, 0));
        /* 04 */
        track.add(newPoint(43.5724602, 10.1889915, "2022-12-19T12:21:49", 781146, 0, 30, 0.05263017, 6.32, 6.67, 0));
        /* 05 */
        track.add(newPoint(43.5732994, 10.1893959, "2022-12-19T12:22:19", 781147, 0, 30, 0.05334656, 6.4, 6.69, 0));
        /* 06 */
        track.add(newPoint(43.5741196, 10.1897936, "2022-12-19T12:22:49", 781148, 0, 30, 0.05217043, 6.26, 6.67, 0));
        /* 07 */
        track.add(newPoint(43.5749588, 10.1901684, "2022-12-19T12:23:19", 781149, 0, 30, 0.05293411, 6.35, 6.67, 0));
        /* bad */
        /* 08 */
        track.add(newPoint(43.6251106, 10.2195053, "2022-12-19T12:53:04", 781150, 0, 1, 0.00201066, 7.24, 7.24, 0));
        /* 09 */
        track.add(newPoint(43.6260033, 10.2200737, "2022-12-19T12:53:34", 781151, 0, 30, 0.05900232, 7.08, 7.44, 0));
        /* 10 */
        track.add(newPoint(43.6269302, 10.2206182, "2022-12-19T12:54:04", 781152, 0, 30, 0.06046239, 7.26, 7.64, 0));
        /* 11 */
        track.add(newPoint(43.627861, 10.2211895, "2022-12-19T12:54:34", 781153, 0, 30, 0.06113732, 7.34, 7.64, 0));
        /* 12 */
        track.add(newPoint(43.6287842, 10.2217894, "2022-12-19T12:55:04", 781154, 0, 30, 0.06124112, 7.35, 7.54, 0));
        /* bad */
        /* 13 */
        track.add(newPoint(43.6579971, 10.2439146, "2022-12-19T13:12:15", 781155, 0, 1, 0.00191109, 6.88, 6.88, 0));
        /* 14 */
        track.add(newPoint(43.6587219, 10.2447338, "2022-12-19T13:12:45", 781156, 0, 30, 0.05624777, 6.75, 7.02, 0));
        /* 15 */
        track.add(newPoint(43.6594887, 10.2455521, "2022-12-19T13:13:15", 781157, 0, 30, 0.0581900, 6.98, 7.35, 0));
        /* 16 */
        track.add(newPoint(43.660347, 10.246273, "2022-12-19T13:13:45", 781158, 0, 30, 0.06031147, 7.24, 7.56, 0));
        /* 17 */
        track.add(newPoint(43.6611481, 10.2469025, "2022-12-19T13:14:15", 781159, 0, 30, 0.05532799, 6.64, 6.84, 0));
        /* 18 */
        track.add(newPoint(43.6619339, 10.2476845, "2022-12-19T13:14:45", 781160, 0, 30, 0.05815968, 6.98, 7.31, 0));
        /* 19 */
        track.add(newPoint(43.6626701, 10.2485037, "2022-12-19T13:15:15", 781161, 0, 30, 0.05677873, 6.81, 7.04, 0));
        /* 20 */
        track.add(newPoint(43.6633682, 10.2493544, "2022-12-19T13:15:45", 781162, 0, 30, 0.0559156, 6.71, 7.04, 0));
        /* 21 */
        track.add(newPoint(43.6641426, 10.2501745, "2022-12-19T13:16:15", 781163, 0, 30, 0.05860144, 7.03, 7.29, 0));
        /* 22 */
        track.add(newPoint(43.6649323, 10.2510023, "2022-12-19T13:16:45", 781164, 0, 30, 0.05953012, 7.14, 7.29, 0));
        /* 23 */
        track.add(newPoint(43.6657181, 10.2517376, "2022-12-19T13:17:15", 781165, 0, 30, 0.05699058, 6.84, 6.94, 0));
        /* 24 */
        track.add(newPoint(43.666481, 10.2525396, "2022-12-19T13:17:45", 781166, 0, 30, 0.05757571, 6.91, 7.19, 0));
        /* 25 */
        track.add(newPoint(43.6672096, 10.253336, "2022-12-19T13:18:15", 781167, 0, 30, 0.05579682, 6.7, 6.82, 0));
        /* 26 */
        track.add(newPoint(43.6678963, 10.2542143, "2022-12-19T13:18:45", 781168, 0, 30, 0.05621391, 6.75, 7.02, 0));
        /* 27 */
        track.add(newPoint(43.668602, 10.2550812, "2022-12-19T13:19:15", 781169, 0, 30, 0.05672455, 6.81, 6.98, 0));
        /* 28 */
        track.add(newPoint(43.6693077, 10.2559576, "2022-12-19T13:19:45", 781170, 0, 30, 0.0570015, 6.84, 7.1, 0));
        /* 29 */
        track.add(newPoint(43.6700058, 10.2568407, "2022-12-19T13:20:15", 781171, 0, 30, 0.05685829, 6.82, 6.96, 0));
        /* 30 */
        track.add(newPoint(43.6707611, 10.2577343, "2022-12-19T13:20:45", 781172, 0, 30, 0.05972846, 7.17, 7.37, 0));
        /* 31 */
        track.add(newPoint(43.6715279, 10.2585888, "2022-12-19T13:21:15", 781173, 0, 30, 0.05916527, 7.1, 7.35, 0));
        /* 32 */
        track.add(newPoint(43.6722641, 10.2593908, "2022-12-19T13:21:45", 781174, 0, 30, 0.05630855, 6.76, 7.06, 0));
    }

    public TrackFixerTest() {
        load();
    }

    @Test
    public void testFirst() {
        TrackFixer fixer = new TrackFixer();
        assertNull(fixer.onTrackPoint(builder, track.get(0))); // no need to change the point
        assertEquals(0, fixer.getChangedPoints());
        assertEquals(1, fixer.getPoints());
        assertEquals(track.get(0).getDistance(), fixer.getTotDist(), 0.0000001);
    }

    @Test
    public void testGoodPoint() {
        TrackFixer fixer = new TrackFixer();
        fixer.onTrackPoint(builder, track.get(0)); // no need to change the point
        assertNull(fixer.onTrackPoint(builder, track.get(1))); // no need to change the point
        assertEquals(0, fixer.getChangedPoints());
        assertEquals(2, fixer.getPoints());
        assertEquals(track.get(0).getDistance() + track.get(1).getDistance(), fixer.getTotDist(), 0.0000001);
    }

    @Test
    public void testBadPointWithGoodMaxSpeed() {
        TrackFixer fixer = new TrackFixer();
        for (int i = 0; i < 8; i++) fixer.onTrackPoint(builder, track.get(i)); // no need to change the point
        TrackPoint newPoint = fixer.onTrackPoint(builder, track.get(8));
        assertNotNull(newPoint);
        assertEquals(3.26926, newPoint.getDistance(), 0.00001);
        assertEquals(1785, newPoint.getPeriod());
        assertEquals(6.593, newPoint.getAverageSpeed(), 0.001);
        assertEquals(7.240, newPoint.getMaxSpeed(), 0.001);
        assertEquals(track.get(8).getPosition().getInstant(), newPoint.getPosition().getInstant());
        assertEquals(track.get(8).getPosition().getLatitude(), newPoint.getPosition().getLatitude(), 0.000001);
        assertEquals(track.get(8).getPosition().getLongitude(), newPoint.getPosition().getLongitude(), 0.000001);
        assertEquals(track.get(8).getEngine(), newPoint.getEngine());
        assertEquals(track.get(8).isAnchor(), newPoint.isAnchor());
        assertEquals(9, fixer.getPoints());
        assertEquals(1, fixer.getChangedPoints());
        assertEquals(0.42903 + 3.26926, fixer.getTotDist(), 0.00001);
    }

    @Test
    public void testBadPointWithBadMaxSpeed() {
        TrackFixer fixer = new TrackFixer();
        for (int i = 0; i < 8; i++) fixer.onTrackPoint(builder, track.get(i)); // no need to change the point
        TrackPoint newPointBadMaxSpeed = newPoint(43.6251106, 10.2195053, "2022-12-19T12:53:04", 781150, 0, 1, 0.00201066, 0.00, 0.00, 0);
        TrackPoint newPoint = fixer.onTrackPoint(builder, newPointBadMaxSpeed);
        assertNotNull(newPoint);
        assertEquals(3.26926, newPoint.getDistance(), 0.00001);
        assertEquals(1785, newPoint.getPeriod());
        assertEquals(6.593, newPoint.getAverageSpeed(), 0.001);
        assertEquals(6.593, newPoint.getMaxSpeed(), 0.001); //expected maxSpeed to be replaced with the average
        assertEquals(track.get(8).getPosition().getInstant(), newPoint.getPosition().getInstant());
        assertEquals(track.get(8).getPosition().getLatitude(), newPoint.getPosition().getLatitude(), 0.000001);
        assertEquals(track.get(8).getPosition().getLongitude(), newPoint.getPosition().getLongitude(), 0.000001);
        assertEquals(track.get(8).getEngine(), newPoint.getEngine());
        assertEquals(track.get(8).isAnchor(), newPoint.isAnchor());
        assertEquals(9, fixer.getPoints());
        assertEquals(1, fixer.getChangedPoints());
        assertEquals(3.69829, fixer.getTotDist(), 0.00001);
    }

    @Test
    public void testAddGoodPointAfterBadOne() {
        TrackFixer fixer = new TrackFixer();
        for (int i = 0; i < 9; i++) fixer.onTrackPoint(builder, track.get(i)); // the 8th is a bad one
        TrackPoint newPoint = fixer.onTrackPoint(builder, track.get(9));
        assertNull(newPoint);
        assertEquals(10, fixer.getPoints());
        assertEquals(1, fixer.getChangedPoints()); // only the 8th was bad
        assertEquals(3.75730, fixer.getTotDist(), 0.00001);
    }
}