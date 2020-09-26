package com.aboni.nmea.router.impl;

import com.aboni.nmea.router.TimestampProvider;

public class DefaultTimestampProvider implements TimestampProvider {

    @Override
    public long getNow() {
        return System.currentTimeMillis();
    }
}
