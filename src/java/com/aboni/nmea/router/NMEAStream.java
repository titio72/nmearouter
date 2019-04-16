package com.aboni.nmea.router;

import com.aboni.nmea.router.agent.NMEAAgent;
import net.sf.marineapi.nmea.sentence.Sentence;

public interface NMEAStream {

	/**
	 * Push a sentence into the stream.
	 * @param s	The sentence to be distributed.
	 * @param src The source that generated the sentence.
	 * @see com.aboni.nmea.router.agent.NMEASource 
	 */
	void pushSentence(Sentence s, NMEAAgent src);

	/**
	 * Subscribe to NMEA stream.
	 * The observer will be called back for each sentence on methods annotated with OnSentence.
	 * The argument of the method annotated with OnSentence can be either a JSONObject or a Sentence.
	 * @param observer The object to be registered as observer.
	 * @see com.aboni.nmea.router.OnSentence
	 */
	void subscribe(Object observer);
	
	/**
	 * Remove the subscription to the stream.
	 * @param observer The observer to be removed.
	 * @see NMEAStream#subscribe(Object observer)
	 */
	void unsubscribe(Object observer);
	
}
