package com.aboni.nmea.router.message;

public interface MsgSpeed extends Message {

    int getSID();

    double getSpeedWaterRef();

    double getSpeedGroundRef();

    String getSpeedSensorType();

    int getSpeedDirection();
}
