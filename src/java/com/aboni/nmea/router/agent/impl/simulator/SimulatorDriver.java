package com.aboni.nmea.router.agent.impl.simulator;

public interface SimulatorDriver {
    double getHeading();

    void setHeading(double heading);

    double getwSpeed();

    void setwSpeed(double wSpeed);

    double getwDirection();

    void setwDirection(double wDirection);

    double getSpeed();

    void setSpeed(double speed);
}
