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

import com.aboni.utils.HWSettings;
import org.json.JSONObject;

public class MsgAttitudeImpl implements MsgAttitude {

    private final int sid;
    private final double pitch;
    private final double roll;
    private final double yaw;

    public MsgAttitudeImpl(double yaw, double roll, double pitch) {
        this(-1, yaw, roll, pitch);
    }

    public MsgAttitudeImpl(int sid, double yaw, double roll, double pitch) {
        this.sid = sid;
        this.pitch = pitch;
        this.roll = roll;
        this.yaw = yaw;
    }

    @Override
    public double getPitch() {
        return pitch;
    }

    @Override
    public int getSID() {
        return sid;
    }

    @Override
    public double getYaw() {
        return yaw;
    }

    @Override
    public double getRoll() {
        return roll;
    }

    @Override
    public String toString() {
        return String.format("Attitude: Yaw {%.1f} Pitch {%.1f} Roll {%.1f} ", getYaw(), getPitch(), getRoll());
    }

    @Override
    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        json.put("topic", "XDR");
        addMeasure(json, getYaw(), "YAW");
        addMeasure(json, getRoll() - HWSettings.getPropertyAsDouble("gyro.roll", 0.0), "ROLL");
        addMeasure(json, getPitch() - HWSettings.getPropertyAsDouble("gyro.pitch", 0.0), "PITCH");
        return json;
    }

    private static void addMeasure(JSONObject json, double value, String name) {
        if (!Double.isNaN(value)) {
            JSONObject m = new JSONObject();
            m.put("type", "A");
            m.put("value", value);
            m.put("unit", "D");
            json.put(name, m);
        }
    }
}
