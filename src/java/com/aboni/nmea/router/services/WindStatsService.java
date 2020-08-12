package com.aboni.nmea.router.services;

import com.aboni.nmea.router.NMEARouter;
import com.aboni.nmea.router.data.meteo.MeteoManagementException;
import com.aboni.nmea.router.data.meteo.WindStatsReader;
import com.aboni.utils.ServerLog;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.time.Instant;

public class WindStatsService extends JSONWebService {

    @Inject
    public WindStatsService(@NotNull NMEARouter router, @NotNull final WindStatsReader reader) {
        setLoader((ServiceConfig config) -> {
            Instant from = config.getParamAsInstant("from", Instant.now().minusSeconds(86400L), 0);
            Instant to = config.getParamAsInstant("to", Instant.now(), 0);
            String sTicks = config.getParameter("ticks", "36");
            int ticks;
            try {
                ticks = Integer.parseInt(sTicks);
            } catch (Exception e) {
                ticks = 36;
            }
            try {
                return reader.getWindStats(from, to, ticks);
            } catch (MeteoManagementException e) {
                ServerLog.getLogger().errorForceStacktrace("Error extracting wind stats", e);
                return null;
            }
        });
    }
}