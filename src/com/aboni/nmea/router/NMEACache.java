package com.aboni.nmea.router;

import java.util.Collection;

import com.aboni.utils.DataEvent;

import net.sf.marineapi.nmea.sentence.HeadingSentence;
import net.sf.marineapi.nmea.sentence.PositionSentence;
import net.sf.marineapi.nmea.sentence.Sentence;
import net.sf.marineapi.nmea.util.Measurement;

public interface NMEACache {

	DataEvent<HeadingSentence> getLastHeading();

	DataEvent<PositionSentence> getLastPosition();

	DataEvent<Measurement> getSensorData(String sensorName);

	Collection<String> getSensors();

	boolean isHeadingOlderThan(long time, long threshold);

	void onSentence(Sentence s, String src);

	boolean isTimeSynced();

	void setTimeSynced();

}