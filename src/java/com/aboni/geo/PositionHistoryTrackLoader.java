package com.aboni.geo;

import java.util.Calendar;

public interface PositionHistoryTrackLoader {

	boolean load(Calendar from, Calendar to);

	PositionHistory getTrack();

}