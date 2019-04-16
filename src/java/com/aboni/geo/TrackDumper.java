package com.aboni.geo;

import java.io.IOException;
import java.io.Writer;

public interface TrackDumper {

	void setTrack(PositionHistory track);

	void dump(Writer w) throws IOException;

	void setTrackName(String trackName);

}