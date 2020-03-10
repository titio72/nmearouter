package com.aboni.nmea.router.track;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.io.Writer;

public interface TrackDumper {

    void setTrackName(@NotNull String name);

    String getTrackName();

    String getMime();

    String getExtension();

    void dump(@NotNull TrackQuery query, @NotNull Writer w) throws TrackManagementException, IOException;

}