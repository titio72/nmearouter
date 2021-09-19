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

import com.aboni.utils.JSONUtils;
import org.json.JSONObject;

public interface MsgBattery extends Message {

    int getInstance();

    int getSID();

    double getVoltage();

    double getCurrent();

    double getTemperature();


    default @Override
    JSONObject toJSON() {
        JSONObject json = new JSONObject();
        json.put("topic", "battery");
        JSONUtils.addInt(json, getInstance(), "instance");
        JSONUtils.addDouble(json, getVoltage(), "voltage");
        JSONUtils.addDouble(json, getCurrent(), "current");
        JSONUtils.addDouble(json, getTemperature(), "temperature");
        return json;
    }

    default @Override
    String getMessageContentType() {
        return "Battery";
    }
}
