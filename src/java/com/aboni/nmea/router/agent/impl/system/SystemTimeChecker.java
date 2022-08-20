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

import com.aboni.nmea.router.TimestampProvider;
import com.aboni.utils.Log;
import com.aboni.utils.LogStringBuilder;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

public class SystemTimeChecker {

    public static final String SYSTEM_TIME_CHECKER_CATEGORY = "SystemTimeChecker";
    //private boolean synced;
    //private long timeSkew;
    public static final long TOLERANCE_MS = 5000;
    private final TimestampProvider timestampProvider;
    private final Log log;
    private final SystemTimeChanger changer;

    public interface SystemTimeChanger {
        void doChangeTime(Instant timestamp);
    }

    @Inject
    public SystemTimeChecker(@NotNull TimestampProvider tp, @NotNull Log log) {
        this.timestampProvider = tp;
        this.log = log;
        this.changer = this::doChangeTime;
    }

    public SystemTimeChecker(@NotNull TimestampProvider timestampProvider, SystemTimeChanger changer, @NotNull Log log) {
        this.timestampProvider = timestampProvider;
        this.changer = changer;
        this.log = log;
    }

    public void checkAndSetTime(Instant gpsTime) {
        try {
            if (gpsTime != null && !checkAndSetTimeSkew(timestampProvider.getInstant(), gpsTime)) {
                // time skew from GPS is too high - reset time stamp
                log.info(LogStringBuilder.start(SYSTEM_TIME_CHECKER_CATEGORY).wO("changing system time").wV("new time", gpsTime).toString());
                if (changer != null) {
                    changer.doChangeTime(gpsTime);
                }
                checkAndSetTimeSkew(timestampProvider.getInstant(), gpsTime);
            }
        } catch (Exception e) {
            log.errorForceStacktrace(LogStringBuilder.start(SYSTEM_TIME_CHECKER_CATEGORY).wO("changing system time").toString(), e);
        }
    }

    private boolean checkAndSetTimeSkew(Instant now, Instant gpsTime) {
        timestampProvider.setSkew(gpsTime.toEpochMilli(), TOLERANCE_MS);
        return timestampProvider.isSynced();
    }

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm:ss");

    private void doChangeTime(Instant c) {
        try {
            String sUTC = DATE_TIME_FORMATTER.format(c);
            log.info(LogStringBuilder.start(SYSTEM_TIME_CHECKER_CATEGORY).wO("exec").wV("script", "./setGPSTime '" + sUTC + "'").toString());
            ProcessBuilder b = new ProcessBuilder("./setGPSTime", sUTC);
            Process process = b.start();
            int retCode = process.waitFor();
            log.info(LogStringBuilder.start(SYSTEM_TIME_CHECKER_CATEGORY).wO("exec").wV("return code", retCode).toString());
        } catch (Exception e) {
            log.errorForceStacktrace(LogStringBuilder.start(SYSTEM_TIME_CHECKER_CATEGORY).wO("exec").toString(), e);
        }
    }

    public boolean isSynced() {
        return timestampProvider.isSynced();
    }

    /**
     * Time skew between the system time and the GPS
     *
     * @return The skew in milliseconds
     */
    public long getTimeSkew() {
        return timestampProvider.getSkew();
    }
}
