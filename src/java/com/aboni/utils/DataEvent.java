package com.aboni.utils;

public class DataEvent<T> {

    private final T data;
    private final  long timestamp;
    private final String source;

    public DataEvent(T data, long timestamp, String source) {
        this.data = data;
        this.timestamp = timestamp;
        this.source = source;
    }

    public T getData() {
        return data;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getSource() {
        return source;
    }
}
