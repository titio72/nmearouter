/*
 * Copyright (c) 2020,  Andrea Boni
 * This file is part of NMEARouter.
 * NMEARouter is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * NMEARouter is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with NMEARouter.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.aboni.nmea.router.message;

public class MsgSpeedImpl implements MsgSpeed {

    private final double speed;
    private final double speedGround;
    private final String sensorType;
    private final int direction;
    private final int sid;

    public MsgSpeedImpl(double speed) {
        this(-1, speed, Double.NaN, "Paddle Wheel", 1);
    }

    public MsgSpeedImpl(int sid, double speed, double speedGround, String sensorType, int direction) {
        this.speed = speed;
        this.speedGround = speedGround;
        this.sensorType = sensorType;
        this.direction = direction;
        this.sid = sid;
    }

    @Override
    public int getSID() {
        return sid;
    }

    @Override
    public double getSpeedWaterRef() {
        return speed;
    }

    @Override
    public double getSpeedGroundRef() {
        return speedGround;
    }

    @Override
    public String getSpeedSensorType() {
        return sensorType;
    }

    @Override
    public int getSpeedDirection() {
        return direction;
    }

    @Override
    public String toString() {
        return String.format("Speed: Sensor {%s} Water Speed {%.1f}", getSpeedSensorType(), getSpeedWaterRef());
    }
}
