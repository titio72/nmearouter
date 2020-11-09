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

package com.aboni.nmea.router.data.track;

import com.aboni.utils.Pair;
import org.junit.Test;

import java.time.Instant;

import static org.junit.Assert.*;

public class BestMileSpeedTest {

    static class PP {
        Instant time;
        double distance;

        PP(Instant t, double d) {
            time = t;
            distance = d;
        }

        Pair<Long, Double> toPair() {
            return new Pair<>(time.toEpochMilli(), distance);
        }
    }

    private static final PP[] DATA = new PP[] {
            new PP(Instant.parse("2020-11-08T09:18:44Z"), 0.06039001),
            new PP(Instant.parse("2020-11-08T09:19:14Z"), 0.0594673),
            new PP(Instant.parse("2020-11-08T09:19:44Z"), 0.06145718),
            new PP(Instant.parse("2020-11-08T09:20:14Z"), 0.0596784),
            new PP(Instant.parse("2020-11-08T09:20:44Z"), 0.05856826),
            new PP(Instant.parse("2020-11-08T09:21:14Z"), 0.0562569),
            new PP(Instant.parse("2020-11-08T09:21:44Z"), 0.05468468),
            new PP(Instant.parse("2020-11-08T09:22:14Z"), 0.05519489),
            new PP(Instant.parse("2020-11-08T09:22:44Z"), 0.05661999),
            new PP(Instant.parse("2020-11-08T09:23:14Z"), 0.05503194),
            new PP(Instant.parse("2020-11-08T09:23:44Z"), 0.05648279),
            new PP(Instant.parse("2020-11-08T09:24:14Z"), 0.05574437),
            new PP(Instant.parse("2020-11-08T09:24:44Z"), 0.05652161),
            new PP(Instant.parse("2020-11-08T09:25:14Z"), 0.05627223),
            new PP(Instant.parse("2020-11-08T09:25:44Z"), 0.05776882),
            new PP(Instant.parse("2020-11-08T09:26:14Z"), 0.05529856),
            new PP(Instant.parse("2020-11-08T09:26:44Z"), 0.0598497),
            new PP(Instant.parse("2020-11-08T09:27:14Z"), 0.06056248),
            new PP(Instant.parse("2020-11-08T09:27:44Z"), 0.05770147),
            new PP(Instant.parse("2020-11-08T09:28:14Z"), 0.05744347),
            new PP(Instant.parse("2020-11-08T09:28:44Z"), 0.0605373),
            new PP(Instant.parse("2020-11-08T09:29:14Z"), 0.06116372),
            new PP(Instant.parse("2020-11-08T09:29:44Z"), 0.05886018),
            new PP(Instant.parse("2020-11-08T09:30:14Z"), 0.05762187),
            new PP(Instant.parse("2020-11-08T09:30:44Z"), 0.0559369),
            new PP(Instant.parse("2020-11-08T09:31:14Z"), 0.05691011),
            new PP(Instant.parse("2020-11-08T09:31:44Z"), 0.05721802),
            new PP(Instant.parse("2020-11-08T09:32:14Z"), 0.05699666),
            new PP(Instant.parse("2020-11-08T09:32:44Z"), 0.05600966),
            new PP(Instant.parse("2020-11-08T09:33:14Z"), 0.05686649),
            new PP(Instant.parse("2020-11-08T09:33:44Z"), 0.05642896),
            new PP(Instant.parse("2020-11-08T09:34:14Z"), 0.05674563),
            new PP(Instant.parse("2020-11-08T09:34:44Z"), 0.05704446),
            new PP(Instant.parse("2020-11-08T09:35:14Z"), 0.05641372),
            new PP(Instant.parse("2020-11-08T09:35:44Z"), 0.05648553),
            new PP(Instant.parse("2020-11-08T09:36:14Z"), 0.05702814),
            new PP(Instant.parse("2020-11-08T09:36:44Z"), 0.05633905),
            new PP(Instant.parse("2020-11-08T09:37:14Z"), 0.05730919),
            new PP(Instant.parse("2020-11-08T09:37:44Z"), 0.05674008),
            new PP(Instant.parse("2020-11-08T09:38:14Z"), 0.05221749),
            new PP(Instant.parse("2020-11-08T09:38:44Z"), 0.0533201),
            new PP(Instant.parse("2020-11-08T09:39:14Z"), 0.05400773),
            new PP(Instant.parse("2020-11-08T09:39:44Z"), 0.05308287),
            new PP(Instant.parse("2020-11-08T09:40:14Z"), 0.05330335),
            new PP(Instant.parse("2020-11-08T09:40:44Z"), 0.05300768),
            new PP(Instant.parse("2020-11-08T09:41:14Z"), 0.05003054),
            new PP(Instant.parse("2020-11-08T09:41:44Z"), 0.05123548),
            new PP(Instant.parse("2020-11-08T09:42:14Z"), 0.0528049),
            new PP(Instant.parse("2020-11-08T09:42:44Z"), 0.05305926),
            new PP(Instant.parse("2020-11-08T09:43:14Z"), 0.0517871),
            new PP(Instant.parse("2020-11-08T09:43:44Z"), 0.05039307),
            new PP(Instant.parse("2020-11-08T09:44:14Z"), 0.05038673),
            new PP(Instant.parse("2020-11-08T09:44:44Z"), 0.04975214),
            new PP(Instant.parse("2020-11-08T09:45:14Z"), 0.04785935),
            new PP(Instant.parse("2020-11-08T09:45:44Z"), 0.04342087),
            new PP(Instant.parse("2020-11-08T09:46:14Z"), 0.04092055),
            new PP(Instant.parse("2020-11-08T09:46:44Z"), 0.03827665),
            new PP(Instant.parse("2020-11-08T09:47:14Z"), 0.03556943),
            new PP(Instant.parse("2020-11-08T09:47:44Z"), 0.03377864),
            new PP(Instant.parse("2020-11-08T09:48:14Z"), 0.03382955),
            new PP(Instant.parse("2020-11-08T09:48:44Z"), 0.03375439),
            new PP(Instant.parse("2020-11-08T09:49:14Z"), 0.03231424),
            new PP(Instant.parse("2020-11-08T10:05:28Z"), 1.78144767),
            new PP(Instant.parse("2020-11-08T10:05:58Z"), 0.05631331)
    };


    @Test
    public void test5MilesOk() {
        BestMileSpeed best = new BestMileSpeed(5.0, "5NM");
        for (PP p: DATA) best.addSample(p.toPair());
        assertEquals(Instant.parse("2020-11-08T10:05:28Z").toEpochMilli(), best.getMaxSpeedT1());
        assertEquals(Instant.parse("2020-11-08T09:19:14Z").toEpochMilli(), best.getMaxSpeedT0());
        assertEquals(6.449052, best.getMaxSpeed(), 0.000001);
        assertTrue(best.hasMax());
    }

    @Test
    public void testNotLongEnough() {
        BestMileSpeed best = new BestMileSpeed(10.0, "5NM");
        for (PP p: DATA) best.addSample(p.toPair());
        assertFalse(best.hasMax());
    }
}