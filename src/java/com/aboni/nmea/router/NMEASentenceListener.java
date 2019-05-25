package com.aboni.nmea.router;

import com.aboni.nmea.router.agent.NMEAAgent;

import net.sf.marineapi.nmea.sentence.Sentence;
import org.json.JSONObject;

public interface NMEASentenceListener {

	void onSentence(RouterMessage message, NMEAAgent src);

}
