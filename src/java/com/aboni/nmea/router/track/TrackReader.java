package com.aboni.nmea.router.track;

import javax.validation.constraints.NotNull;

public interface TrackReader {

    interface TrackReaderListener {
        void onRead(TrackPoint sample);
    }

    void readTrack(@NotNull TrackReaderListener target) throws TrackManagementException;
}
