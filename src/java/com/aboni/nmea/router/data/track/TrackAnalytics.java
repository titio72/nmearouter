/*
(C) 2020, Andrea Boni
This file is part of NMEARouter.
NMEARouter is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.
NMEARouter is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.
You should have received a copy of the GNU General Public License
along with NMEARouter.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.aboni.nmea.router.data.track;

import com.aboni.sensors.EngineStatus;
import com.aboni.utils.Pair;
import com.aboni.utils.Utils;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.inject.Inject;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class TrackAnalytics {

    private LegListener legListener;

    public interface LegListener {
        void onLeg(StatsLeg leg);
    }

    private static String formatDuration(long milliSeconds) {
        long s = milliSeconds / 1000;
        return String.format("%d:%02d:%02d", s / 3600, (s % 3600) / 60, (s % 60));
    }

    private static class SpeedDistribution {
        final double[] buckets = new double[]{0.5, 1.0, 1.5, 2.5, 3.0, 3.5, 4.0, 4.5, 5.0, 5.5,
                6.0, 6.5, 7.0, 7.5, 8.0, 8.5, 9.0, 9.5, 10.0, 10.5,
                11.0, 11.5, 12.0, 12.5, 13.0, 13.5, 14.0, 14.5, 15.0, 15.5};

        final double[] distances = new double[buckets.length];
        final long[] times = new long[buckets.length];

        void addSample(long period, double dist, double speed) {
            int i = Math.min(29, (int) (Math.abs(speed) / 0.5));
            distances[i] += dist;
            times[i] += period;
        }

        JSONArray toJSON() {
            JSONArray res = new JSONArray();
            for (int i = 0; i < buckets.length; i++) {
                double bucket = buckets[i];
                double distance = distances[i];
                long time = times[i];
                JSONObject sample = new JSONObject();
                sample.put("speed", bucket);
                sample.put("distance", distance);
                sample.put("time", time);
                res.put(sample);
            }
            return res;
        }
    }

    static class StartEnd {
        long start = 0L;
        long end = 0L;

        void toJSON(JSONObject j) {
            j.put("start", Utils.formatISOTimestampUTC(start));
            j.put("end", Utils.formatISOTimestampUTC(end));
        }
    }

    static class ValueAndTime {
        long time = 0;
        double value = 0.0;

        void toJSONDistanceAndDuration(String label, JSONObject j) {
            j.put(label + "Time", formatDuration(time));
            j.put(label + "Dist", value);
        }

        void toJSONSpeedAndTime(String label, JSONObject j) {
            j.put(label + "SpeedTime", Utils.formatISOTimestampUTC(time));
            j.put(label + "Speed", value);
        }
    }

    public static class StatsLeg {

        long samples;
        final StartEnd startEnd = new StartEnd();
        final ValueAndTime totalNavigation = new ValueAndTime();
        final ValueAndTime[] sailEngineNavigation = new ValueAndTime[]{new ValueAndTime(), new ValueAndTime(), new ValueAndTime()};
        final ValueAndTime maxSpeed = new ValueAndTime();
        final ValueAndTime max30sSpeed = new ValueAndTime();
        final ValueAndTime maxSampleSpeed = new ValueAndTime();
        final BestMileSpeed[] movingAvgMaxes = new BestMileSpeed[]{
                new BestMileSpeed(1.0, "01NM"),
                new BestMileSpeed(5.0, "05NM"),
                new BestMileSpeed(10.0, "10NM")};
        final SpeedDistribution speedDistribution = new SpeedDistribution();
        final SpeedDistribution speedDistributionSail = new SpeedDistribution();

        public JSONObject toJson() {
            JSONObject j = new JSONObject();
            j.put("samples", samples);
            startEnd.toJSON(j);
            totalNavigation.toJSONDistanceAndDuration("nav", j);
            if (totalNavigation.time > 1.0) {
                j.put("speedAverage", totalNavigation.value / (totalNavigation.time / 1000.0 / 60.0 / 60.0));
                j.put("relativeEngOffTime", sailEngineNavigation[EngineStatus.OFF.getValue()].time / (double) totalNavigation.time);
                j.put("relativeEngOn_Time", sailEngineNavigation[EngineStatus.ON.getValue()].time / (double) totalNavigation.time);
                j.put("relativeEngUnkTime", sailEngineNavigation[EngineStatus.UNKNOWN.getValue()].time / (double) totalNavigation.time);
            }
            sailEngineNavigation[EngineStatus.OFF.getValue()].toJSONDistanceAndDuration("navEngineOff", j);
            sailEngineNavigation[EngineStatus.ON.getValue()].toJSONDistanceAndDuration("navEngineOn_", j);
            sailEngineNavigation[EngineStatus.UNKNOWN.getValue()].toJSONDistanceAndDuration("navEngineUnk", j);
            if (totalNavigation.value > 1.0) {
                j.put("relativeEngOffDist", sailEngineNavigation[EngineStatus.OFF.getValue()].value / totalNavigation.value);
                j.put("relativeEngOn_Dist", sailEngineNavigation[EngineStatus.ON.getValue()].value / totalNavigation.value);
                j.put("relativeEngUnkDist", sailEngineNavigation[EngineStatus.UNKNOWN.getValue()].value / totalNavigation.value);
            }
            if (sailEngineNavigation[EngineStatus.OFF.getValue()].time > 1.0) {
                j.put("speedSailAverage", sailEngineNavigation[EngineStatus.OFF.getValue()].value / (sailEngineNavigation[EngineStatus.OFF.getValue()].time / 1000.0 / 60.0 / 60.0));
            }
            if (sailEngineNavigation[EngineStatus.ON.getValue()].time > 1.0) {
                j.put("speedEngAverage", sailEngineNavigation[EngineStatus.ON.getValue()].value / (sailEngineNavigation[EngineStatus.ON.getValue()].time / 1000.0 / 60.0 / 60.0));
            }
            if (sailEngineNavigation[EngineStatus.UNKNOWN.getValue()].time > 1.0) {
                j.put("speedUnkAverage", sailEngineNavigation[EngineStatus.UNKNOWN.getValue()].value / (sailEngineNavigation[EngineStatus.UNKNOWN.getValue()].time / 1000.0 / 60.0 / 60.0));
            }
            maxSpeed.toJSONSpeedAndTime("max", j);
            max30sSpeed.toJSONSpeedAndTime("maxAvg", j);
            maxSampleSpeed.toJSONSpeedAndTime("maxSampled", j);
            j.put("speedDistribution", speedDistribution.toJSON());
            j.put("speedDistributionSail", speedDistributionSail.toJSON());
            for (BestMileSpeed mm : movingAvgMaxes) fillRes(mm, j);
            return j;
        }

        public Instant getStart() {
            return Instant.ofEpochMilli(startEnd.start);
        }

        public Instant getEnd() {
            return Instant.ofEpochMilli(startEnd.end);
        }

        public long getDuration() {
            return totalNavigation.time;
        }

        public double getDistance() {
            return totalNavigation.value;
        }

        public double getSailDistance() {
            return sailEngineNavigation[0].value;
        }

        public double getMotorDistance() {
            return sailEngineNavigation[1].value;
        }

        void addTime(long l) {
            if (startEnd.start == 0) startEnd.start = l;
            startEnd.end = l;
        }

        protected static void fillRes(BestMileSpeed m, JSONObject result) {
            if (m.getMaxSpeed() > 0.0) {
                result.put("max" + m.getTag() + "Speed", m.getMaxSpeed());
                result.put("max" + m.getTag() + "SpeedTime0", Utils.formatISOTimestampUTC(m.getMaxSpeedT0()));
                result.put("max" + m.getTag() + "SpeedTime1", Utils.formatISOTimestampUTC(m.getMaxSpeedT1()));
            }
        }
    }

    static class Stats extends StatsLeg {
        final List<StatsLeg> legs = new ArrayList<>();
        StatsLeg currentLeg;

        long totalAnchorTime = 0;
        long totalTime = 0;
        final String name;

        Stats(String name) {
            this.name = name;
        }

        @Override
        public JSONObject toJson() {
            JSONObject j = super.toJson();
            j.put("name", name);
            j.put("totalTime", formatDuration(totalTime));
            j.put("anchorTime", formatDuration(totalAnchorTime));

            int legCounter = 0;
            JSONArray jLegs = new JSONArray();
            for (StatsLeg leg : legs) {
                JSONObject jLeg = leg.toJson();
                jLeg.put("leg", ++legCounter);
                jLegs.put(jLeg);
            }
            j.put("legs", jLegs);
            j.put("numLegs", legCounter);
            return j;
        }

        void createLeg() {
            currentLeg = new StatsLeg();
            legs.add(currentLeg);
        }

        void resetLeg() {
            currentLeg = null;
        }

        void clear() {
            legs.clear();
            currentLeg = null;
        }
    }

    private final Stats stats;
    private TrackPoint prevSample;

    public TrackAnalytics(String name) {
        stats = new Stats(name);
    }

    public JSONObject getJSONStats() {
        return stats.toJson();
    }

    Stats getStats() {
        return stats;
    }

    public void setListener(LegListener listener) {
        this.legListener = listener;
    }

    public void processSample(TrackPoint sample) {
        if (stats.currentLeg == null && !sample.isAnchor()) {
            // started moving
            stats.createLeg();
        } else if (stats.currentLeg != null && sample.isAnchor()) {
            // just stopped
            if (legListener!=null) {
                legListener.onLeg(stats.currentLeg);
                stats.clear();
            } else {
                stats.resetLeg();
            }
        }
        stats.samples++;
        stats.addTime(sample.getPosition().getTimestamp());
        calcMovingAverageSpeed(stats, sample);
        calcTimeAndDist(stats, sample, prevSample);
        calcTotAndAnchorTime(stats, sample, prevSample);
        calcMaxSpeed(stats, sample);
        calcSpeedDistribution(stats, sample, prevSample);
        if (stats.currentLeg != null) {
            stats.currentLeg.samples++;
            stats.currentLeg.addTime(sample.getPosition().getTimestamp());
            calcMovingAverageSpeed(stats.currentLeg, sample);
            calcTimeAndDist(stats.currentLeg, sample, prevSample);
            calcMaxSpeed(stats.currentLeg, sample);
            calcSpeedDistribution(stats.currentLeg, sample, prevSample);
        }
        prevSample = sample;
    }

    private static void calcSpeedDistribution(StatsLeg stats, TrackPoint sample, TrackPoint prev) {
        long period = (prev == null) ? (sample.getPeriod() * 1000L) : (sample.getPosition().getTimestamp() - prev.getPosition().getTimestamp());
        if (!sample.isAnchor()) {
            stats.speedDistribution.addSample(period, sample.getDistance(), sample.getAverageSpeed());
            if (sample.getEngine() == EngineStatus.OFF) {
                stats.speedDistributionSail.addSample(period, sample.getDistance(), sample.getAverageSpeed());
            }
        }
    }

    private static void calcMaxSpeed(StatsLeg stats, TrackPoint sample) {
        if (!sample.isAnchor()) {
            if (sample.getMaxSpeed() > stats.maxSpeed.value) {
                stats.maxSpeed.value = sample.getMaxSpeed();
                stats.maxSpeed.time = sample.getPosition().getTimestamp();
            }
            if (sample.getAverageSpeed() > stats.maxSampleSpeed.value) {
                stats.maxSampleSpeed.value = sample.getAverageSpeed();
                stats.maxSampleSpeed.time = sample.getPosition().getTimestamp();
            }
            double avgSpeed = sample.getDistance() / (sample.getPeriod() / 60.0 / 60.0);
            if (avgSpeed > stats.max30sSpeed.value) {
                stats.max30sSpeed.value = avgSpeed;
                stats.max30sSpeed.time = sample.getPosition().getTimestamp();
            }

        }
    }

    private static void calcTotAndAnchorTime(Stats stats, TrackPoint sample, TrackPoint prev) {
        long deltaTime = (prev == null) ? 0 : (sample.getPosition().getTimestamp() - prev.getPosition().getTimestamp());
        stats.totalTime += deltaTime;
        if (sample.isAnchor())
            stats.totalAnchorTime += deltaTime;
    }

    private static void calcTimeAndDist(StatsLeg stats, TrackPoint sample, TrackPoint prev) {
        long deltaTime = (prev == null) ? 0 : (sample.getPosition().getTimestamp() - prev.getPosition().getTimestamp());
        if (!sample.isAnchor()) {
            stats.sailEngineNavigation[sample.getEngine().getValue()].time += deltaTime;
            stats.sailEngineNavigation[sample.getEngine().getValue()].value += sample.getDistance();

            stats.totalNavigation.time += deltaTime;
            stats.totalNavigation.value += sample.getDistance();
        }
    }

    private static void calcMovingAverageSpeed(StatsLeg stats, TrackPoint sample) {
        Pair<Long, Double> p = new Pair<>(sample.getPosition().getTimestamp(), sample.getDistance());
        for (BestMileSpeed m : stats.movingAvgMaxes) m.addSample(p);
    }
}
