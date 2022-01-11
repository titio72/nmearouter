/*
 * Copyright (c) 2022,  Andrea Boni
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

public interface MsgDCDetailedStatus extends Message {

    int getInstance();

    int getSID();

    DCType getType();

    /**
     * State of charge.
     *
     * @return SOC in %
     */
    double getSOC();

    /**
     * State of health.
     *
     * @return SOH in %
     */
    double getSOH();

    /**
     * Time to discharge in minutes.
     *
     * @return the time left to discharge.
     */
    int getTimeToGo();

    double getRippleVoltage();

    default @Override
    JSONObject toJSON() {
        JSONObject json = new JSONObject();
        json.put("topic", "dc_status");
        JSONUtils.addInt(json, getInstance(), "instance");
        JSONUtils.addDouble(json, getSOC(), "SOC");
        JSONUtils.addDouble(json, getSOH(), "SOH");
        JSONUtils.addDouble(json, getRippleVoltage(), "ripple");
        JSONUtils.addString(json, getType().toString(), "type");
        return json;
    }

    default @Override
    String getMessageContentType() {
        return "DC Detailed Status";
    }
}
