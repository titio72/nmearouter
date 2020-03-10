package com.aboni.nmea.router.meteo;

import javax.validation.constraints.NotNull;
import java.time.Instant;

public interface MeteoReader {

    interface MeteoReaderListener {
        void onRead(MeteoSample sample);
    }

    void readMeteo(@NotNull Instant from, @NotNull Instant to, @NotNull MeteoReader.MeteoReaderListener target) throws MeteoManagementException;
}
