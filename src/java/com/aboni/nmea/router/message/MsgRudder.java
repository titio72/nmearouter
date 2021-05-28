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

import org.json.JSONObject;

public interface MsgRudder extends Message {

    int getInstance();

    double getAngle();

    double getAngleOrder();

    int getDirectionOrder();

    @Override
    default JSONObject toJSON() {
        JSONObject j = new JSONObject();
        j.put("topic", "RSA");
        j.put("instance", getInstance());
        if (!Double.isNaN(getAngle())) {
            j.put("angle", getAngle());
            j.put(
                    (getInstance() == 0) ? "starboard_angle" : "port_angle",
                    getAngle());
        }
        return j;
    }

    @Override
    default String getMessageContentType() {
        return "Rudder";
    }

}
