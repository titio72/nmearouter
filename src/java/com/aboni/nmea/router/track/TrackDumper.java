package com.aboni.nmea.router.track;

import java.io.IOException;
import java.io.Writer;

public interface TrackDumper {

	void setTrack(TrackReader track);

	void dump(Writer w) throws IOException;

	void setTrackName(String name);

	String getTrackName();

	String getMime();

	String getExtension();


}