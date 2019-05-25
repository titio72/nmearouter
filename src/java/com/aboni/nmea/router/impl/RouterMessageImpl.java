package com.aboni.nmea.router.impl;

import com.aboni.nmea.router.RouterMessage;
import net.sf.marineapi.nmea.sentence.Sentence;
import org.json.JSONObject;

public class RouterMessageImpl<T extends Object> implements RouterMessage {

    private final long timestamp;
    private final T message;
    private final String source;

    public static RouterMessage createMessage(Sentence obj, String source) {
        return new RouterMessageImpl<>(obj, source, System.currentTimeMillis());
    }

    public static RouterMessage createMessage(JSONObject obj, String source) {
        return new RouterMessageImpl<>(obj, source, System.currentTimeMillis());
    }

    public static RouterMessage clone(RouterMessage m) {
        return new RouterMessageImpl<>(m.getPayload(), m.getSource(), m.getTimestamp());
    }

    private RouterMessageImpl(T msg, String source, long timestamp){
        this.timestamp = timestamp;
        this.message = msg;
        this.source = source;
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public Object getPayload() {
        return getMessage();
    }

    @Override
    public String getSource() {
        return source;
    }

    public T getMessage() {
        return message;
    }
}
