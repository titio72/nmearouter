package com.aboni.nmea.router;

import com.aboni.nmea.router.n2k.N2KMessage;
import net.sf.marineapi.nmea.sentence.Sentence;
import org.json.JSONObject;

public interface RouterMessageFactory {

    RouterMessage createMessage(Sentence obj, String source, long timestamp);

    RouterMessage createMessage(JSONObject obj, String source, long timestamp);

    RouterMessage createMessage(N2KMessage obj, String source, long timestamp);
}
