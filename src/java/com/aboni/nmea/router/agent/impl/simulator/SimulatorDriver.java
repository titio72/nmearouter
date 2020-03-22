package com.aboni.nmea.router.agent.impl.simulator;

public interface SimulatorDriver {
    double getHeading();

    void setHeading(double heading);

    double getWindSpeed();

    void setWindSpeed(double wSpeed);

    double getWindDirection();

    void setWindDirection(double wDirection);

    double getSpeed();

    void setSpeed(double speed);
}
