package com.aboni.nmea.router.track;

import com.aboni.sensors.EngineStatus;
import com.aboni.utils.Pair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class TrackAnalytics {

    private static class SpeedDistribution {
        double[] buckets = new double[]{0.5, 1.0, 1.5, 2.5, 3.0, 3.5, 4.0, 4.5, 5.0, 5.5,
                6.0, 6.5, 7.0, 7.5, 8.0, 8.5, 9.0, 9.5, 10.0, 10.5,
                11.0, 11.5, 12.0, 12.5, 13.0, 13.5, 14.0, 14.5, 15.0, 15.5};

        double[] distances = new double[buckets.length];
        long[] times = new long[buckets.length];

        void addSample(long period, double dist, double speed) {
            int i = Math.min(29, (int) (Math.abs(speed) / 0.5));
            distances[i] += dist;
            times[i] += period;
        }

        JSONArray toJSON() {
            JSONArray res  = new JSONArray();
            for (int i = 0; i<buckets.length; i++) {
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

    private static class MovingAverageMax {

        private double d;
        private String tag;
        private List<Pair<Long, Double>> sss = new LinkedList<>();
        private double distance = 0;

        private long t0Max = 0;
        private long t1Max = 0;
        private double speedMax = 0.0;

        MovingAverageMax(double distance, String tag) {
            d = distance;
            this.tag = tag;
        }

        void addSample(Pair<Long, Double> p) {
            sss.add(p);
            distance += p.second;
            if (distance > d) {
                // pull the first item from the rolling window
                Pair<Long, Double> p0 = sss.get(0);
                while (distance > d && !sss.isEmpty()) {
                    distance -= p0.second;
                    sss.remove(0);
                    p0 = sss.get(0);
                }

                long dT = p.first - p0.first;
                double speed = distance / ((double) dT / 3600000d);
                if (speed > speedMax) {
                    t0Max = p0.first;
                    t1Max = p.first;
                    speedMax = speed;
                }
            }
        }

        double getMaxSpeed() {
            return speedMax;
        }

        long getMaxSpeedT0() {
            return t0Max;
        }

        long getMaxSpeedT1() {
            return t1Max;
        }

        void fillRes(JSONObject result) {
            if (getMaxSpeed() > 0.0) {
                result.put("max" + tag + "Speed", getMaxSpeed());
                result.put("max" + tag + "SpeedTime0", formatDate(getMaxSpeedT0()));
                result.put("max" + tag + "SpeedTime1", formatDate(getMaxSpeedT1()));
            }
        }
    }

    static class StatsLeg {
        long samples;
        long start;
        long end;
        long totalNavigationTime = 0;
        double totalNavigationDistance = 0;
        long[] sailEngineTime = new long[]{0, 0, 0};
        double[] sailEngineDistance = new double[]{0.0, 0.0, 0.0};
        double maxSpeed = 0.0;
        long maxSpeedTime = 0;
        double max30sAverageSpeed = 0.0;
        long max30sAverageSpeedTime = 0;
        double maxSampleAverageSpeed = 0.0;
        long maxSampleAverageSpeedTime = 0;
        MovingAverageMax[] movingAvgMaxes = new MovingAverageMax[]{
                new MovingAverageMax(1.0, "01NM"),
                new MovingAverageMax(5.0, "05NM"),
                new MovingAverageMax(10.0, "10NM")};
        SpeedDistribution speedDistribution = new SpeedDistribution();
        SpeedDistribution speedDistributionSail = new SpeedDistribution();

        public JSONObject toJson() {
            JSONObject j = new JSONObject();
            j.put("samples", samples);
            j.put("start", formatDate(start));
            j.put("end", formatDate(end));
            j.put("navTime", formatDuration(totalNavigationTime));
            j.put("navDist", totalNavigationDistance);
            if (totalNavigationTime>1.0)
                j.put("speedAverage", totalNavigationDistance / (totalNavigationTime/1000.0/60.0/60.0));
            j.put("navEngineOffTime", formatDuration(sailEngineTime[EngineStatus.OFF.getValue()]));
            j.put("navEngineOn_Time", formatDuration(sailEngineTime[EngineStatus.ON.getValue()]));
            j.put("navEngineUnkTime", formatDuration(sailEngineTime[EngineStatus.UNKNOWN.getValue()]));
            j.put("navEngineOffDist", sailEngineDistance[EngineStatus.OFF.getValue()]);
            j.put("navEngineOn_Dist", sailEngineDistance[EngineStatus.ON.getValue()]);
            j.put("navEngineUnkDist", sailEngineDistance[EngineStatus.UNKNOWN.getValue()]);
            if (sailEngineTime[EngineStatus.OFF.getValue()]>1.0)
                j.put("speedSailAverage", sailEngineDistance[EngineStatus.OFF.getValue()] / (sailEngineTime[EngineStatus.OFF.getValue()]/1000.0/60.0/60.0));
            j.put("maxSpeed", maxSpeed);
            j.put("maxSpeedTime", formatDate(maxSpeedTime));
            j.put("maxAvgSpeed", max30sAverageSpeed);
            j.put("maxAvgSpeedTime", formatDate(max30sAverageSpeedTime));
            j.put("maxSampledSpeed", maxSampleAverageSpeed);
            j.put("maxSampledSpeedTime", formatDate(maxSampleAverageSpeedTime));
            j.put("speedDistribution", speedDistribution.toJSON());
            j.put("speedDistributionSail", speedDistributionSail.toJSON());
            for (MovingAverageMax mm : movingAvgMaxes) mm.fillRes(j);
            return j;
        }

        void addTime(long l) {
            if (start == 0) start = l;
            end = l;
        }

        protected static String formatDuration(long l) {
            long s = l / 1000;
            return String.format("%d:%02d:%02d", s / 3600, (s % 3600) / 60, (s % 60));
        }
    }

    class Stats extends StatsLeg {
        List<StatsLeg> legs = new ArrayList<>();
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

    private static final DateFormat DF_ISO;

    static {
        DF_ISO = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        DF_ISO.setTimeZone(TimeZone.getTimeZone("UTC"));
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
        stats.addTime(sample.getTs());
        calcMovingAverageSpeed(stats, sample);
        calcSailEngineTimeAndDist(stats, sample, prevSample);
        calcTotAndAnchorTime(stats, sample, prevSample);
        calcNavTimeAndDist(stats, sample, prevSample);
        calcMaxSpeed(stats, sample);
        calcSpeedDistribution(stats, sample, prevSample);
        if (stats.currentLeg != null) {
            stats.currentLeg.samples++;
            stats.currentLeg.addTime(sample.getTs());
            calcMovingAverageSpeed(stats.currentLeg, sample);
            calcSailEngineTimeAndDist(stats.currentLeg, sample, prevSample);
            calcNavTimeAndDist(stats.currentLeg, sample, prevSample);
            calcMaxSpeed(stats.currentLeg, sample);
            calcSpeedDistribution(stats.currentLeg, sample, prevSample);
        }
        prevSample = sample;
    }

    private static void calcSpeedDistribution(StatsLeg stats, TrackPoint sample, TrackPoint prev) {
        long period = (prev == null) ? (sample.getPeriod() * 1000) : (sample.getTs() - prev.getTs());
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
                stats.maxSpeedTime = sample.getTs();
            }
            if (sample.getAverageSpeed() > stats.maxSampleAverageSpeed) {
                stats.maxSampleAverageSpeed = sample.getAverageSpeed();
                stats.maxSampleAverageSpeedTime = sample.getTs();
            }
            double avgSpeed = sample.getDistance() / (sample.getPeriod() / 60.0 / 60.0);
            if (avgSpeed > stats.max30sAverageSpeed) {
                stats.max30sAverageSpeed = avgSpeed;
                stats.max30sAverageSpeedTime = sample.getTs();
            }

        }
    }

    private static void calcTotAndAnchorTime(Stats stats, TrackPoint sample, TrackPoint prev) {
        long deltaTime = (prev == null) ? 0 : (sample.getTs() - prev.getTs());
        stats.totalTime += deltaTime;
        if (sample.isAnchor())
            stats.totalAnchorTime += deltaTime;
    }

    private static void calcNavTimeAndDist(StatsLeg stats, TrackPoint sample, TrackPoint prev) {
        long deltaTime = (prev == null) ? 0 : (sample.getTs() - prev.getTs());
        if (!sample.isAnchor()) {
            stats.totalNavigationTime += deltaTime;
            stats.totalNavigationDistance += sample.getDistance();
        }
    }

    private static void calcSailEngineTimeAndDist(StatsLeg stats, TrackPoint sample, TrackPoint prev) {
        if (!sample.isAnchor()) {
            stats.sailEngineDistance[sample.getEngine().getValue()] += sample.getDistance();
            stats.sailEngineTime[sample.getEngine().getValue()] += (prev == null) ? 0 : (sample.getTs() - prev.getTs());
        }
    }

    private static void calcMovingAverageSpeed(StatsLeg stats, TrackPoint sample) {
        Pair<Long, Double> p = new Pair<>(sample.getTs(), sample.getDistance());
        for (MovingAverageMax m : stats.movingAvgMaxes) m.addSample(p);
    }

    private static String formatDate(long l) {
        return DF_ISO.format(new Date(l));
    }
}
