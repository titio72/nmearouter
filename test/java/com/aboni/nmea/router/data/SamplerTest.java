/*
 * Copyright (c) 2021,  Andrea Boni
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

package com.aboni.nmea.router.data;

import com.aboni.nmea.router.HeadingProvider;
import com.aboni.nmea.router.RouterMessage;
import com.aboni.nmea.router.data.metrics.Metrics;
import com.aboni.nmea.router.impl.RouterMessageImpl;
import com.aboni.nmea.router.message.*;
import com.aboni.nmea.router.message.impl.MsgHeadingImpl;
import com.aboni.nmea.router.message.impl.MsgTemperatureImpl;
import com.aboni.nmea.router.message.impl.MsgWindDataImpl;
import com.aboni.nmea.router.utils.ConsoleLog;
import com.aboni.nmea.router.utils.DataEvent;
import com.aboni.toolkit.ProgrammableTimeStampProvider;
import com.aboni.utils.Pair;
import org.junit.Before;
import org.junit.Test;

import java.time.Instant;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.*;

public class SamplerTest {

    public static final int HEADING_AGE_THRESHOLD_MS = 800;

    private static class MyHeadingProvider implements HeadingProvider {

        DataEvent<MsgHeading> lastHeading;

        @Override
        public DataEvent<MsgHeading> getLastHeading() {
            return lastHeading;
        }
    }

    private static class MyStatsWriter implements StatsWriter {

        List<Pair<Sample, Long>> events = new LinkedList<>();

        @Override
        public void write(Sample s, long ts) {
            events.add(new Pair<>(s, ts));
        }

        @Override
        public void init() {

        }

        @Override
        public void dispose() {

        }

        List<Pair<Sample, Long>> getEvents() {
            return events;
        }
    }

    private Sampler sampler;
    private MyStatsWriter writer;
    private MyHeadingProvider headingProvider;
    private ProgrammableTimeStampProvider timeProvider;
    private final long t0 = Instant.parse("2021-12-25T12:00:00Z").toEpochMilli(); // arbitrary

    private static RouterMessage getTemperature(double t, long ts) {
        return new RouterMessageImpl<>(
                new MsgTemperatureImpl(TemperatureSource.MAIN_CABIN_ROOM, t), "TestSource", ts);
    }

    private static RouterMessage getWind(double speed, double direction, long ts) {
        return new RouterMessageImpl<>(
                new MsgWindDataImpl(speed, direction, false), "TestSource", ts);
    }

    @Before
    public void setup() {
        timeProvider = new ProgrammableTimeStampProvider();
        writer = new MyStatsWriter();
        headingProvider = new MyHeadingProvider();
        sampler = new Sampler(ConsoleLog.getLogger(), timeProvider, writer, "TestTag");
    }

    @Test
    public void testIsStarted() {
        assertFalse(sampler.isStarted());
        sampler.start();
        assertTrue(sampler.isStarted());
    }

    private static void checkSample(StatsSample sample, double value, int samples) {
        assertNotNull(sample);
        assertEquals(samples, sample.getSamples());
        assertEquals(value, sample.getValue(), 0.0001);
    }

    private void initWithTemperature() {
        // make sure timestamp is considered reliable
        timeProvider.setTimestamp(t0);
        timeProvider.setSkew(t0, 100);

        // init metrics
        sampler.initMetric(Metrics.AIR_TEMPERATURE,
                (Message m) -> (m instanceof MsgTemperature),
                (Message m) -> ((MsgTemperature) m).getTemperature(),
                60000L, "AT0", -100, 100);
    }

    private void initWithWind() {
        // make sure timestamp is considered reliable
        timeProvider.setTimestamp(t0);
        timeProvider.setSkew(t0, 100);

        // init metrics
        sampler.initMetric(Metrics.WIND_DIRECTION,
                (Message m) -> {
                    boolean isAWindMessage = m instanceof MsgWindData;
                    boolean isTrueWind = ((MsgWindData) m).isTrue();
                    boolean isHeadingRecent = !headingProvider.isHeadingOlderThan(timeProvider.getNow(), HEADING_AGE_THRESHOLD_MS);
                    return isAWindMessage && isHeadingRecent && isTrueWind;
                },
                (Message m) -> {
                    double a = ((MsgWindData) m).getAngle() + headingProvider.getLastHeading().getData().getHeading();
                    return a;
                },
                60000L, "TWD", -100, 100);
    }

    @Test
    public void testCurrentSampleNotStarted() {

        initWithTemperature();

        // post metric and check that it is NOT collected (because it is not started)
        sampler.onSentence(getTemperature(25.2, t0));
        checkSample(sampler.getCurrent(Metrics.AIR_TEMPERATURE), Double.NaN, 0);
    }

    @Test
    public void testCurrentSampleOk() {

        initWithTemperature();

        sampler.start();

        // post metric and check that it is collected
        sampler.onSentence(getTemperature(25.2, t0));
        checkSample(sampler.getCurrent(Metrics.AIR_TEMPERATURE), 25.2, 1);

        // post metric after 1 second and check that it is collected
        timeProvider.setTimestamp(t0 + 1000);
        sampler.onSentence(getTemperature(25.4, t0 + 1000));
        checkSample(sampler.getCurrent(Metrics.AIR_TEMPERATURE), 25.3, 2);

        // post metric after 1 second and check that it is collected
        timeProvider.setTimestamp(t0 + 2000);
        sampler.onSentence(getTemperature(25.6, t0 + 2000));
        checkSample(sampler.getCurrent(Metrics.AIR_TEMPERATURE), 25.4, 3);
    }

    @Test
    public void testDiscardSecondMeasureOutOfRange() {

        initWithTemperature();

        sampler.start();

        // post metric and check that it is collected
        sampler.onSentence(getTemperature(25.2, t0));

        // post invalid metric after 1 second and check that it is collected
        timeProvider.setTimestamp(t0 + 1000);
        sampler.onSentence(getTemperature(125.4, t0 + 1000));

        // the second measurement has been discarded because out of range
        checkSample(sampler.getCurrent(Metrics.AIR_TEMPERATURE), 25.2, 1);
    }

    @Test
    public void testDiscardFirstMeasureOutOfRange() {

        initWithTemperature();

        sampler.start();

        // post metric and check that it is collected
        sampler.onSentence(getTemperature(125.2, t0));
        checkSample(sampler.getCurrent(Metrics.AIR_TEMPERATURE), Double.NaN, 0);
    }

    @Test
    public void testWriteSample() {
        initWithTemperature();

        sampler.start();

        // post 60 measurements in 1 minute
        for (int i = 0; i < 60; i++) {
            timeProvider.setTimestamp(t0 + i * 1000);
            sampler.onSentence(getTemperature(25.0 + (i % 3), t0 + i * 1000));
        }

        // one more second to let the 1m timeout to expire
        long dumpTime = t0 + 60 * 1000;
        timeProvider.setTimestamp(dumpTime);

        sampler.dumpAndReset();

        assertEquals(1, writer.getEvents().size());
        assertEquals(dumpTime, writer.getEvents().get(0).second.longValue());
        //assertEquals(60, writer.getEvents().get(0).first.getSamples());
        assertEquals(26.0, writer.getEvents().get(0).first.getValue(), 0.0001);
        assertEquals(25.0, writer.getEvents().get(0).first.getMinValue(), 0.0001);
        assertEquals(27.0, writer.getEvents().get(0).first.getMaxValue(), 0.0001);

        // check reset
        checkSample(sampler.getCurrent(Metrics.AIR_TEMPERATURE), Double.NaN, 0);
    }

    @Test
    public void testForceDumpBeforeTimeoutExpire() {
        initWithTemperature();

        sampler.start();

        // post 30 measurements in 30 seconds (timeout for the metric is 1m)
        for (int i = 0; i < 30; i++) {
            timeProvider.setTimestamp(t0 + i * 1000);
            sampler.onSentence(getTemperature(25.0 + (i % 3), t0 + i * 1000));
        }

        sampler.dumpAndReset(true);

        assertEquals(1, writer.getEvents().size());

        // check it reset
        assertEquals(0, sampler.getCurrent(Metrics.AIR_TEMPERATURE).getSamples());
    }

    @Test
    public void testDumpBeforeTimeoutExpire() {
        initWithTemperature();

        sampler.start();

        // post 30 measurements in 30 seconds (timeout for the metric is 1m)
        for (int i = 0; i < 30; i++) {
            timeProvider.setTimestamp(t0 + i * 1000);
            sampler.onSentence(getTemperature(25.0 + (i % 3), t0 + i * 1000));
        }

        sampler.dumpAndReset();

        assertEquals(0, writer.getEvents().size());

        // check it didn't reset
        assertEquals(30, sampler.getCurrent(Metrics.AIR_TEMPERATURE).getSamples());
    }

    @Test
    public void testWindDirectionNoHeading() {
        initWithWind();
        sampler.start();
        sampler.onSentence(getWind(12.2, 93.0, t0));
        assertEquals(0, writer.getEvents().size());
    }

    @Test
    public void testWindDirectionAcceptMeasure() {
        initWithWind();
        sampler.start();
        headingProvider.lastHeading = new DataEvent<>(new MsgHeadingImpl(30.0, true), t0, "TEST_SOURCE");
        sampler.onSentence(getWind(12.2, 93.0, t0 + 250)); //add a small time-skew to verify that the threshold works
        checkSample(sampler.getCurrent(Metrics.WIND_DIRECTION), 123.0, 1);
    }

    @Test
    public void testWindDirectionRejectHeadingTooOld() {
        initWithWind();
        sampler.start();
        headingProvider.lastHeading = new DataEvent<>(new MsgHeadingImpl(30.0, true), t0, "TEST_SOURCE");
        sampler.onSentence(getWind(12.2, 93.0, t0 + HEADING_AGE_THRESHOLD_MS + 150)); //add time-skew to make heading old compared to wind
        assertEquals(0, writer.getEvents().size());
    }
}