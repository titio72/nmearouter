package com.aboni.nmea.router.data.track;

import com.aboni.utils.Query;

import javax.validation.constraints.NotNull;

public interface TrackReader {

    interface TrackReaderListener {
        void onRead(TrackPoint sample);
    }

    void readTrack(@NotNull Query query, @NotNull TrackReaderListener target) throws TrackManagementException;
}
