package com.aboni.nmea.router.track;

import com.aboni.sensors.EngineStatus;
import com.aboni.utils.Pair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class TrackAnalytics {

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

    static class StatsLeg {

        private static final DateFormat DF_ISO;

        static {
            DF_ISO = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            DF_ISO.setTimeZone(TimeZone.getTimeZone("UTC"));
        }

        long samples;
        long start;
        long end;
        long totalNavigationTime = 0;
        double totalNavigationDistance = 0;
        final long[] sailEngineTime = new long[]{0, 0, 0};
        final double[] sailEngineDistance = new double[]{0.0, 0.0, 0.0};
        double maxSpeed = 0.0;
        long maxSpeedTime = 0;
        double max30sAverageSpeed = 0.0;
        long max30sAverageSpeedTime = 0;
        double maxSampleAverageSpeed = 0.0;
        long maxSampleAverageSpeedTime = 0;
        final BestMileSpeed[] movingAvgMaxes = new BestMileSpeed[]{
                new BestMileSpeed(1.0, "01NM"),
                new BestMileSpeed(5.0, "05NM"),
                new BestMileSpeed(10.0, "10NM")};
        final SpeedDistribution speedDistribution = new SpeedDistribution();
        final SpeedDistribution speedDistributionSail = new SpeedDistribution();

        public JSONObject toJson() {
            JSONObject j = new JSONObject();
            j.put("samples", samples);
            j.put("start", formatDate(start));
            j.put("end", formatDate(end));
            j.put("navTime", formatDuration(totalNavigationTime));
            j.put("navDist", totalNavigationDistance);
            if (totalNavigationTime > 1.0) {
                j.put("speedAverage", totalNavigationDistance / (totalNavigationTime / 1000.0 / 60.0 / 60.0));
                j.put("relativeEngOffTime", sailEngineTime[EngineStatus.OFF.getValue()] / (double) totalNavigationTime);
                j.put("relativeEngOn_Time", sailEngineTime[EngineStatus.ON.getValue()] / (double) totalNavigationTime);
                j.put("relativeEngUnkTime", sailEngineTime[EngineStatus.UNKNOWN.getValue()] / (double) totalNavigationTime);
            }
            j.put("navEngineOffTime", formatDuration(sailEngineTime[EngineStatus.OFF.getValue()]));
            j.put("navEngineOn_Time", formatDuration(sailEngineTime[EngineStatus.ON.getValue()]));
            j.put("navEngineUnkTime", formatDuration(sailEngineTime[EngineStatus.UNKNOWN.getValue()]));
            if (totalNavigationDistance > 1.0) {
                j.put("relativeEngOffDist", sailEngineDistance[EngineStatus.OFF.getValue()] / totalNavigationDistance);
                j.put("relativeEngOn_Dist", sailEngineDistance[EngineStatus.ON.getValue()] / totalNavigationDistance);
                j.put("relativeEngUnkDist", sailEngineDistance[EngineStatus.UNKNOWN.getValue()] / totalNavigationDistance);
            }
            j.put("navEngineOffDist", sailEngineDistance[EngineStatus.OFF.getValue()]);
            j.put("navEngineOn_Dist", sailEngineDistance[EngineStatus.ON.getValue()]);
            j.put("navEngineUnkDist", sailEngineDistance[EngineStatus.UNKNOWN.getValue()]);
            if (sailEngineTime[EngineStatus.OFF.getValue()] > 1.0) {
                j.put("speedSailAverage", sailEngineDistance[EngineStatus.OFF.getValue()] / (sailEngineTime[EngineStatus.OFF.getValue()] / 1000.0 / 60.0 / 60.0));
            }
            if (sailEngineTime[EngineStatus.ON.getValue()] > 1.0) {
                j.put("speedEngAverage", sailEngineDistance[EngineStatus.ON.getValue()] / (sailEngineTime[EngineStatus.ON.getValue()] / 1000.0 / 60.0 / 60.0));
            }
            if (sailEngineTime[EngineStatus.UNKNOWN.getValue()] > 1.0) {
                j.put("speedUnkAverage", sailEngineDistance[EngineStatus.UNKNOWN.getValue()] / (sailEngineTime[EngineStatus.UNKNOWN.getValue()] / 1000.0 / 60.0 / 60.0));
            }
            j.put("maxSpeed", maxSpeed);
            j.put("maxSpeedTime", formatDate(maxSpeedTime));
            j.put("maxAvgSpeed", max30sAverageSpeed);
            j.put("maxAvgSpeedTime", formatDate(max30sAverageSpeedTime));
            j.put("maxSampledSpeed", maxSampleAverageSpeed);
            j.put("maxSampledSpeedTime", formatDate(maxSampleAverageSpeedTime));
            j.put("speedDistribution", speedDistribution.toJSON());
            j.put("speedDistributionSail", speedDistributionSail.toJSON());
            for (BestMileSpeed mm : movingAvgMaxes) fillRes(mm, j);
            return j;
        }

        void addTime(long l) {
            if (start == 0) start = l;
            end = l;
        }

        protected static String formatDuration(long milliSeconds) {
            long s = milliSeconds / 1000;
            return String.format("%d:%02d:%02d", s / 3600, (s % 3600) / 60, (s % 60));
        }

        private static String formatDate(long l) {
            return DF_ISO.format(new Date(l));
        }

        protected static void fillRes(BestMileSpeed m, JSONObject result) {
            if (m.getMaxSpeed() > 0.0) {
                result.put("max" + m.getTag() + "Speed", m.getMaxSpeed());
                result.put("max" + m.getTag() + "SpeedTime0", formatDate(m.getMaxSpeedT0()));
                result.put("max" + m.getTag() + "SpeedTime1", formatDate(m.getMaxSpeedT1()));
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

    public void processSample(TrackPoint sample) {
        if (stats.currentLeg == null && !sample.isAnchor()) {
            // started moving
            stats.createLeg();
        } else if (stats.currentLeg != null && sample.isAnchor()) {
            // just stopped
            stats.resetLeg();
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
        long period = (prev == null) ? (sample.getPeriod() * 1000) : (sample.getPosition().getTimestamp() - prev.getPosition().getTimestamp());
        if (!sample.isAnchor()) {
            stats.speedDistribution.addSample(period, sample.getDistance(), sample.getAverageSpeed());
            if (sample.getEngine() == EngineStatus.OFF) {
                stats.speedDistributionSail.addSample(period, sample.getDistance(), sample.getAverageSpeed());
            }
        }
    }

    private static void calcMaxSpeed(StatsLeg stats, TrackPoint sample) {
        if (!sample.isAnchor()) {
            if (sample.getMaxSpeed() > stats.maxSpeed) {
                stats.maxSpeed = sample.getMaxSpeed();
                stats.maxSpeedTime = sample.getPosition().getTimestamp();
            }
            if (sample.getAverageSpeed() > stats.maxSampleAverageSpeed) {
                stats.maxSampleAverageSpeed = sample.getAverageSpeed();
                stats.maxSampleAverageSpeedTime = sample.getPosition().getTimestamp();
            }
            double avgSpeed = sample.getDistance() / (sample.getPeriod() / 60.0 / 60.0);
            if (avgSpeed > stats.max30sAverageSpeed) {
                stats.max30sAverageSpeed = avgSpeed;
                stats.max30sAverageSpeedTime = sample.getPosition().getTimestamp();
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
            stats.sailEngineTime[sample.getEngine().getValue()] += deltaTime;
            stats.totalNavigationTime += deltaTime;
            stats.sailEngineDistance[sample.getEngine().getValue()] += sample.getDistance();
            stats.totalNavigationDistance += sample.getDistance();
        }
    }

    private static void calcMovingAverageSpeed(StatsLeg stats, TrackPoint sample) {
        Pair<Long, Double> p = new Pair<>(sample.getPosition().getTimestamp(), sample.getDistance());
        for (BestMileSpeed m : stats.movingAvgMaxes) m.addSample(p);
    }
}
