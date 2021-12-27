package com.aboni.nmea.router.impl;

import com.aboni.nmea.router.TimestampProvider;

public class DefaultTimestampProvider extends TimestampProvider {

    @Override
    public long getNow() {
        return System.currentTimeMillis();
    }
}
