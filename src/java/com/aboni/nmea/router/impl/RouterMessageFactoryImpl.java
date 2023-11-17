package com.aboni.nmea.router.impl;

import com.aboni.nmea.message.Message;
import com.aboni.nmea.nmea0183.NMEA0183Message;
import com.aboni.nmea.nmea0183.NMEA0183MessageFactory;
import com.aboni.nmea.router.RouterMessage;
import com.aboni.nmea.router.RouterMessageFactory;
import com.aboni.nmea.router.message.JSONMessage;
import net.sf.marineapi.nmea.sentence.Sentence;
import org.json.JSONObject;

import javax.inject.Inject;

public class RouterMessageFactoryImpl implements RouterMessageFactory {

    @Inject
    NMEA0183MessageFactory nmea0183factory;

    @Override
    public RouterMessage createMessage(Sentence obj, String agentSource, long timestamp) {
        if (nmea0183factory==null)
            return new RouterMessageImpl(new NMEA0183Message(obj), agentSource, timestamp);
        else
            return new RouterMessageImpl(nmea0183factory.getMessage(obj), agentSource, timestamp);
    }

    @Override
    public RouterMessage createMessage(JSONObject obj, String agentSource, long timestamp) {
        return new RouterMessageImpl(new JSONMessage(obj), agentSource, timestamp);
    }

    @Override
    public RouterMessage createMessage(Message obj, String agentSource, long timestamp) {
        return new RouterMessageImpl(obj, agentSource, timestamp);
    }
}
