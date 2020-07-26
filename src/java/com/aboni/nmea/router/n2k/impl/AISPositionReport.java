package com.aboni.nmea.router.n2k.impl;

import net.sf.marineapi.nmea.util.Position;

public interface AISPositionReport {

    String getAISClass();

    Position getPosition();

    int getMessageId();

    String getPositionAccuracy();

    String getRepeatIndicator();

    boolean issRAIM();

    double getCog();

    double getSog();

    double getHeading();

    String getMMSI();

    String getTimestampStatus();

    int getTimestamp();

    String getNavStatus();

    long getAge(long now);

    void setOverrideTime(long t);

    long getOverrrideTime();


}
