package com.aboni.nmea.router.impl;

import com.aboni.nmea.router.RouterMessage;
import com.aboni.nmea.router.RouterMessageFactory;
import com.aboni.nmea.router.message.Message;
import com.aboni.nmea.router.nmea0183.NMEA0183Message;
import net.sf.marineapi.nmea.sentence.Sentence;
import org.json.JSONObject;

public class RouterMessageFactoryImpl implements RouterMessageFactory {

    @Override
    public RouterMessage createMessage(Sentence obj, String source, long timestamp) {
        return new RouterMessageImpl<>(new NMEA0183Message(obj), source, timestamp);
    }

    @Override
    public RouterMessage createMessage(JSONObject obj, String source, long timestamp) {
        return new RouterMessageImpl<>(obj, source, timestamp);
    }

    @Override
    public RouterMessage createMessage(Message obj, String source, long timestamp) {
        return new RouterMessageImpl<>(obj, source, timestamp);
    }
}
