package com.aboni.nmea.router;

import com.aboni.utils.DataEvent;
import net.sf.marineapi.nmea.sentence.HeadingSentence;
import net.sf.marineapi.nmea.sentence.PositionSentence;
import net.sf.marineapi.nmea.sentence.Sentence;

public interface NMEACache {

	DataEvent<HeadingSentence> getLastHeading();

	DataEvent<PositionSentence> getLastPosition();

	boolean isHeadingOlderThan(long time, long threshold);

	void onSentence(Sentence s, String src);

	boolean isTimeSynced();

	void setTimeSynced();

	long getTimeSkew();

	<T> void setStatus(String statusKey, T status);

	<T> T getStatus(String statusKey);

}