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

package com.aboni.nmea.router.agent.impl.system;

import com.aboni.nmea.router.NMEACache;
import com.aboni.nmea.router.NMEARouterStatuses;
import com.aboni.nmea.sentences.NMEATimestampExtractor;
import com.aboni.nmea.sentences.NMEATimestampExtractor.GPSTimeException;
import com.aboni.utils.ServerLog;
import net.sf.marineapi.nmea.sentence.Sentence;
import net.sf.marineapi.nmea.sentence.TimeSentence;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class SystemTimeChecker {

    private boolean synced;
    private long timeSkew;
    public static final long TOLERANCE_MS = 5000;
    private final NMEACache cache;
    private final SystemTimeChanger changer;

    public interface SystemTimeChanger {
        void doChangeTime(OffsetDateTime timestamp);
    }

    public SystemTimeChecker(NMEACache cache) {
        this.cache = cache;
        this.changer = SystemTimeChecker::doChangeTime;
    }

    public SystemTimeChecker(NMEACache cache, SystemTimeChanger changer) {
        this.cache = cache;
        this.changer = changer;
    }

    public void checkAndSetTime(Sentence s) {
        try {
            if (s instanceof TimeSentence) {
                OffsetDateTime gpsTime = NMEATimestampExtractor.extractTimestamp(s);
                if (gpsTime != null && !checkAndSetTimeSkew(cache.getNow(), gpsTime)) {
                    // time skew from GPS is too high - reset time stamp
                    ServerLog.getLogger().info("Changing system time to {" + gpsTime + "}");
                    if (changer != null) {
                        changer.doChangeTime(gpsTime);
                    }
                    checkAndSetTimeSkew(cache.getNow(), gpsTime);
                }
            }
        } catch (GPSTimeException e) {
            ServerLog.getLogger().warning("Caught invalid GPS time: " + e.getMessage());
        }
    }

    private boolean checkAndSetTimeSkew(long now, OffsetDateTime gpsTime) {
        timeSkew = Math.abs(now - gpsTime.toInstant().toEpochMilli());
        synced = (timeSkew < TOLERANCE_MS);
        cache.setStatus(NMEARouterStatuses.GPS_TIME_SYNC, synced);
        cache.setStatus(NMEARouterStatuses.GPS_TIME_SKEW, timeSkew);
        return synced;
    }

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm:ss");

    private static void doChangeTime(OffsetDateTime c) {
        try {
            String sUTC = DATE_TIME_FORMATTER.format(c.withOffsetSameInstant(ZoneOffset.UTC));
            ServerLog.getLogger().info("Running {./setGPSTime '" + sUTC + "'}");
            ProcessBuilder b = new ProcessBuilder("./setGPSTime", sUTC);
            Process process = b.start();
            int retCode = process.waitFor();
            ServerLog.getLogger().info("SetTime Return code {" + retCode + "}");
        } catch (Exception e) {
            ServerLog.getLogger().error("Cannot set GPS time", e);
        }
    }

    public boolean isSynced() {
        return synced;
    }

    /**
     * Time skew between the system time and the GPS
     *
     * @return The skew in milliseconds
     */
    public long getTimeSkew() {
        return timeSkew;
    }
}
