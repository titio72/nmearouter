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

package com.aboni.nmea.router.data.track.impl;

import com.aboni.nmea.router.NMEARouterModule;
import com.aboni.nmea.router.data.track.*;
import com.aboni.nmea.router.data.Query;
import com.aboni.nmea.router.data.QueryByDate;
import com.aboni.nmea.router.utils.ThingsFactory;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.time.Instant;

import static org.junit.Assert.*;

public class DBTrackReaderTest {

    private final static String[] TEST_TRACK_DATA = new String[]{
            "43.6775263,10.2739236,2022-11-27T10:53:26.000Z,778766,1,1,0,0.01,0.23,0",
            "43.6775186,10.2739231,2022-11-27T11:03:26.000Z,778767,1,600,0.00104431,0.01,0.21,0",
            "43.6775117,10.273933,2022-11-27T11:13:26.000Z,778768,1,600,0.00109294,0.01,0.19,1",
            "43.6778069,10.273737,2022-11-27T11:16:53.000Z,778769,0,207,0.01897288,0.33,2.37,1",
            "43.6781425,10.2734995,2022-11-27T11:17:23.000Z,778770,0,30,0.0226382,2.72,3.05,1",
            "43.6783104,10.2729969,2022-11-27T11:17:53.000Z,778771,0,30,0.02408882,2.89,3.19,1",
            "43.678299,10.2725172,2022-11-27T11:18:23.000Z,778772,0,30,0.02089791,2.51,3.21,1",
            "43.678299,10.2720919,2022-11-27T11:18:53.000Z,778773,0,30,0.01851976,2.22,2.35,1",
            "43.6782684,10.2714663,2022-11-27T11:19:23.000Z,778774,0,30,0.02730129,3.28,3.69,1",
            "43.678215,10.2708225,2022-11-27T11:19:53.000Z,778775,0,30,0.02821133,3.39,3.79,1",
            "43.6781464,10.2700577,2022-11-27T11:20:23.000Z,778776,0,30,0.03355621,4.03,4.51,1",
            "43.6777191,10.2693481,2022-11-27T11:20:53.000Z,778777,0,30,0.04014253,4.82,5.17,1",
            "43.6771507,10.2687864,2022-11-27T11:21:23.000Z,778778,0,30,0.0419636,5.04,5.19,1",
            "43.6765594,10.2682791,2022-11-27T11:21:53.000Z,778779,0,30,0.04178881,5.01,5.23,1",
            "43.6760216,10.2677202,2022-11-27T11:22:23.000Z,778780,0,30,0.04041511,4.85,5.05,1",
            "43.6755867,10.2670069,2022-11-27T11:22:53.000Z,778781,0,30,0.04056419,4.87,5.23,1",
            "43.6751213,10.2666674,2022-11-27T11:23:23.000Z,778782,0,30,0.03159232,3.79,5.15,1",
            "43.6751175,10.2670784,2022-11-27T11:23:53.000Z,778783,0,30,0.0178993,2.15,2.9,1",
            "43.6751137,10.2673168,2022-11-27T11:24:23.000Z,778784,0,30,0.0103841,1.25,1.69,1",
            "43.6750984,10.2674389,2022-11-27T11:24:53.000Z,778785,0,30,0.00539362,0.65,1.09,1",
            "43.6750183,10.2674036,2022-11-27T11:25:23.000Z,778786,0,30,0.00504554,0.61,1.42,1",
            "43.6749229,10.2670174,2022-11-27T11:25:53.000Z,778787,0,30,0.01776472,2.13,2.78,1",
            "43.6745453,10.2668419,2022-11-27T11:26:23.000Z,778788,0,30,0.02391018,2.87,3.36,1",
            "43.6742134,10.2665348,2022-11-27T11:26:53.000Z,778789,0,30,0.02398367,2.88,3.17,1",
            "43.6738815,10.2662449,2022-11-27T11:27:23.000Z,778790,0,30,0.02357513,2.83,3.19,1",
            "43.6735611,10.2659607,2022-11-27T11:27:53.000Z,778791,0,30,0.02286245,2.74,3.11,1",
            "43.6731949,10.2656469,2022-11-27T11:28:23.000Z,778792,0,30,0.02587161,3.1,3.44,1",
            "43.672863,10.265357,2022-11-27T11:28:53.000Z,778793,0,30,0.02357524,2.83,3.09,1",
            "43.6725464,10.2650852,2022-11-27T11:29:23.000Z,778794,0,30,0.02238035,2.69,2.86,1",
            "43.6722298,10.2648153,2022-11-27T11:29:53.000Z,778795,0,30,0.02233657,2.68,2.94,1",
            "43.6719017,10.2645226,2022-11-27T11:30:23.000Z,778796,0,30,0.02344989,2.81,3.07,1",
            "43.671566,10.2642221,2022-11-27T11:30:53.000Z,778797,0,30,0.02401472,2.88,2.97,1",
            "43.6710777,10.2638149,2022-11-27T11:31:23.000Z,778798,0,30,0.0342424,4.11,5.58,0",
            "43.6703339,10.2632246,2022-11-27T11:31:53.000Z,778799,0,30,0.05150084,6.18,6.67,0",
            "43.6696205,10.2625799,2022-11-27T11:32:23.000Z,778800,0,30,0.05118212,6.14,6.9,0",
            "43.6688995,10.2617884,2022-11-27T11:32:53.000Z,778801,0,30,0.05530848,6.64,7,0",
            "43.6681938,10.2611637,2022-11-27T11:33:23.000Z,778802,0,30,0.05032365,6.04,6.32,0",
            "43.667614,10.2606363,2022-11-27T11:33:53.000Z,778803,0,30,0.04168346,5,5.7,0",
            "43.6671448,10.2602215,2022-11-27T11:34:23.000Z,778804,0,30,0.03344769,4.01,4.35,0",
            "43.6666183,10.2598381,2022-11-27T11:34:53.000Z,778805,0,30,0.03572327,4.29,4.49,0",
            "43.6661034,10.2594643,2022-11-27T11:35:23.000Z,778806,0,30,0.03492229,4.19,4.35,0",
            "43.6655235,10.2590494,2022-11-27T11:35:53.000Z,778807,0,30,0.03919748,4.7,5.02,0",
            "43.6649323,10.2586098,2022-11-27T11:36:23.000Z,778808,0,30,0.04030968,4.84,5.02,0"};

    @Before
    public void setUp() throws Exception {
        Injector injector = Guice.createInjector(new NMEARouterModule());
        ThingsFactory.setInjector(injector);
        TrackTestTableManager.setUp();
        //TrackTestTableManager.loadTrack("track_test.csv");
        TrackTestTableManager.loadTrackCSV(TEST_TRACK_DATA);
        scannedCount = 0;
        referenceIndex = -1;
        lastId = 0;
    }

    @After
    public void tearDown() throws Exception {
        TrackTestTableManager.tearDown();
    }

    private int referenceIndex = -1;

    private int scannedCount = 0;
    private int lastId = 0;

    @Test
    public void testAll() throws TrackManagementException {
        TrackReader ts = new DBTrackReader(TrackTestTableManager.TRACK_TABLE_NAME);
        Instant d0 = Instant.parse("2022-11-27T10:53:26.000Z");
        Instant d1 = Instant.parse("2022-11-27T11:36:23.000Z");
        Query q = new QueryByDate(d0, d1);
        ts.readTrack(q, (int id, TrackPoint point) -> {
            scannedCount++;
            referenceIndex = getNext(d0, d1, referenceIndex);
            checkPoint(d0, d1, id, lastId, point, referenceIndex);
            //System.out.printf("%d %s %f %f %f%n", id, point.getPosition().getInstant(), point.getPosition().getLatitude(), point.getPosition().getLongitude(), point.getAverageSpeed());
            referenceIndex++;
            lastId = id;
        });
        assertEquals(43, scannedCount);
    }

    private static void checkPoint(Instant fromTime, Instant toTime, int id, int last, TrackPoint point, int trackIndex) {
        if (last != 0) assertEquals(last + 1, id); // check that they are in sequence
        assertNotNull(point);
        assertTrue(point.getPosition().getTimestamp() >= fromTime.toEpochMilli());
        assertTrue(point.getPosition().getTimestamp() <= toTime.toEpochMilli());
        assertNotEquals(-1, trackIndex);
        Instant refTime = Instant.parse(TEST_TRACK_DATA[trackIndex].split(",")[2]);
        assertEquals(refTime, point.getPosition().getInstant());
    }

    @Test
    public void testPartial() throws TrackManagementException {
        TrackReader ts = new DBTrackReader(TrackTestTableManager.TRACK_TABLE_NAME);
        Instant d0 = Instant.parse("2022-11-27T11:16:53.000Z");
        Instant d1 = Instant.parse("2022-11-27T11:17:53.000Z");
        Query q = new QueryByDate(d0, d1);
        ts.readTrack(q, (int id, TrackPoint point) -> {
            scannedCount++;
            referenceIndex = getNext(d0, d1, referenceIndex);
            checkPoint(d0, d1, id, lastId, point, referenceIndex);
            //System.out.printf("%d %s %f %f %f%n", id, point.getPosition().getInstant(), point.getPosition().getLatitude(), point.getPosition().getLongitude(), point.getAverageSpeed());
            referenceIndex++;
            lastId = id;
        });
        assertEquals(3, scannedCount);
    }

    @Test
    public void testNoResult() throws TrackManagementException {
        TrackReader ts = new DBTrackReader(TrackTestTableManager.TRACK_TABLE_NAME);
        Instant d0 = Instant.parse("2022-11-28T11:16:53.000Z");
        Instant d1 = Instant.parse("2022-11-28T11:17:53.000Z");
        Query q = new QueryByDate(d0, d1);
        ts.readTrack(q, (int id, TrackPoint point) -> {
            scannedCount++;
        });
        assertEquals(0, scannedCount);
    }

    private int getNext(Instant timeFrom, Instant timeTo, int fromLine) {
        int from = Math.max(fromLine, 0);
        for (int k = from; k< DBTrackReaderTest.TEST_TRACK_DATA.length; k++) {
            String[] csv = DBTrackReaderTest.TEST_TRACK_DATA[k].split(",");
            Instant d = Instant.parse(csv[2]);
            if (!d.isBefore(timeFrom) && !d.isAfter(timeTo)) return k;
        }
        return -1;
    }

}