package com.aboni.nmea.router.impl;

import com.aboni.nmea.router.RouterMessage;
import com.aboni.nmea.router.RouterMessageFactory;
import com.aboni.nmea.router.n2k.N2KMessage;
import net.sf.marineapi.nmea.sentence.Sentence;
import org.json.JSONObject;

public class RouterMessageFactoryImpl implements RouterMessageFactory {

    @Override
    public RouterMessage createMessage(Sentence obj, String source, long timestamp) {
        return new RouterMessageImpl<>(obj, RouterMessageImpl.NMEA, source, timestamp);
    }

    @Override
    public RouterMessage createMessage(JSONObject obj, String source, long timestamp) {
        return new RouterMessageImpl<>(obj, RouterMessageImpl.JSON, source, timestamp);
    }

    @Override
    public RouterMessage createMessage(N2KMessage obj, String source, long timestamp) {
        return new RouterMessageImpl<>(obj, RouterMessageImpl.N2K, source, timestamp);
    }
}
