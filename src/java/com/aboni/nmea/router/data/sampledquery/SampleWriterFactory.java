package com.aboni.nmea.router.data.sampledquery;

public interface SampleWriterFactory {
    SampleWriter getWriter(String type);
}
