package com.aboni.nmea.router.track.analytics;

import com.aboni.nmea.router.track.TrackSample;
import com.aboni.sensors.EngineStatus;
import com.aboni.utils.Pair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class TrackAnalytics {

    static class SpeedDistribution {
        double[] buckets = new double[] { 0.5, 1.0, 1.5, 2.5, 3.0, 3.5, 4.0, 4.5, 5.0, 5.5,
                6.0, 6.5, 7.0, 7.5, 8.0, 8.5, 9.0, 9.5, 10.0, 10.5,
                11.0, 11.5, 12.0, 12.5, 13.0, 13.5, 14.0, 14.5, 15.0, 15.5};

        double[] distances = new double[buckets.length];
        long[] times = new long[buckets.length];

        void addSample(long period, double dist, double speed) {
            int i = Math.min(29, (int)(Math.abs(speed) / 0.5));
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

    static class MovingAverageMax {

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
                result.put("t0_" + tag, getMaxSpeedT0());
                result.put("t1_" + tag, getMaxSpeedT1());
                result.put("speed_" + tag, getMaxSpeed());
            }
        }
    }

    public static class StatsLeg {
        private static final DateFormat DF;
        static {
            DF = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            DF.setTimeZone(TimeZone.getTimeZone("UTC"));
        }

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
                new MovingAverageMax(1.0, "1NM"),
                new MovingAverageMax(5.0, "5NM"),
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
            j.put("max01NMSpeed", movingAvgMaxes[0].getMaxSpeed());
            j.put("max01NMSpeedTime0", formatDate(movingAvgMaxes[0].getMaxSpeedT0()));
            j.put("max01NMSpeedTime1", formatDate(movingAvgMaxes[0].getMaxSpeedT1()));
            j.put("max05NMSpeed", movingAvgMaxes[1].getMaxSpeed());
            j.put("max05NMSpeedTime0", formatDate(movingAvgMaxes[1].getMaxSpeedT0()));
            j.put("max05NMSpeedTime1", formatDate(movingAvgMaxes[1].getMaxSpeedT1()));
            j.put("max10NMSpeed", movingAvgMaxes[2].getMaxSpeed());
            j.put("max10NMSpeedTime0", formatDate(movingAvgMaxes[2].getMaxSpeedT0()));
            j.put("max10NMSpeedTime1", formatDate(movingAvgMaxes[2].getMaxSpeedT1()));
            j.put("speedDistribution", speedDistribution.toJSON());
            j.put("speedDistributionSail", speedDistributionSail.toJSON());
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
        protected static String formatDate(long l) {
            return DF.format(new Date(l));
        }
    }

    public class Stats extends StatsLeg {
        List<StatsLeg> legs = new ArrayList<>();
        StatsLeg currentLeg;

        long totalAnchorTime = 0;
        long totalTime = 0;

        @Override
        public JSONObject toJson() {
            JSONObject j = super.toJson();
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
    private TrackSample prevSample;

    public TrackAnalytics() {
        stats = new Stats();
    }

    public Stats getStats() {
        return stats;
    }

    public void processSample(TrackSample sample) {
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
        calcSailAndEngine(stats, sample, prevSample);
        calcTotAndAnchorTime(stats, sample, prevSample);
        calcNavTime(stats, sample, prevSample);
        calcMaxSpeed(stats, sample);
        calcSpeedDistribution(stats, sample, prevSample);
        if (stats.currentLeg != null) {
            stats.currentLeg.samples++;
            stats.currentLeg.addTime(sample.getTs());
            calcMovingAverageSpeed(stats.currentLeg, sample);
            calcSailAndEngine(stats.currentLeg, sample, prevSample);
            calcNavTime(stats.currentLeg, sample, prevSample);
            calcMaxSpeed(stats.currentLeg, sample);
            calcSpeedDistribution(stats.currentLeg, sample, prevSample);
        }
        prevSample = sample;
    }

    private static void calcSpeedDistribution(StatsLeg stats, TrackSample sample, TrackSample prev) {
        long period = (prev==null)?(sample.getPeriod()*1000):(sample.getTs() - prev.getTs());
        if (!sample.isAnchor()) {
            stats.speedDistribution.addSample(period, sample.getDistance(), sample.getSpeed());
            if (sample.getEng()==EngineStatus.OFF) {
                stats.speedDistributionSail.addSample(period, sample.getDistance(), sample.getSpeed());
            }
        }
    }

    private static void calcMaxSpeed(StatsLeg stats, TrackSample sample) {
        if (!sample.isAnchor()) {
            if (sample.getMaxSpeed() > stats.maxSpeed) {
                stats.maxSpeed = sample.getMaxSpeed();
                stats.maxSpeedTime = sample.getTs();
            }
            if (sample.getSpeed() > stats.maxSampleAverageSpeed) {
                stats.maxSampleAverageSpeed = sample.getSpeed();
                stats.maxSampleAverageSpeedTime = sample.getTs();
            }
            double avgSpeed = sample.getDistance() / (sample.getPeriod() / 60.0 / 60.0);
            if (avgSpeed > stats.max30sAverageSpeed) {
                stats.max30sAverageSpeed = avgSpeed;
                stats.max30sAverageSpeedTime = sample.getTs();
            }

        }
    }

    private static void calcTotAndAnchorTime(Stats stats, TrackSample sample, TrackSample prev) {
        long deltaTime = (prev == null) ? 0 : (sample.getTs() - prev.getTs());
        stats.totalTime += deltaTime;
        if (sample.isAnchor())
            stats.totalAnchorTime += deltaTime;
    }

    private static void calcNavTime(StatsLeg stats, TrackSample sample, TrackSample prev) {
        long deltaTime = (prev == null) ? 0 : (sample.getTs() - prev.getTs());
        if (!sample.isAnchor()) {
            stats.totalNavigationTime += deltaTime;
            stats.totalNavigationDistance += sample.getDistance();
        }
    }

    private static void calcSailAndEngine(StatsLeg stats, TrackSample sample, TrackSample prev) {
        if (!sample.isAnchor()) {
            stats.sailEngineDistance[sample.getEng().getValue()] += sample.getDistance();
            stats.sailEngineTime[sample.getEng().getValue()] += (prev == null) ? 0 : (sample.getTs() - prev.getTs());
        }
    }

    private static void calcMovingAverageSpeed(StatsLeg stats, TrackSample sample) {
        Pair<Long, Double> p = new Pair<>(sample.getTs(), sample.getDistance());
        for (MovingAverageMax m : stats.movingAvgMaxes) m.addSample(p);
    }
}
