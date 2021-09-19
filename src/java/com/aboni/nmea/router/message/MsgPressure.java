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

public interface MsgPressure extends Message {

    int getSID();

    int getInstance();

    double getPressure();

    PressureSource getPressureSource();

    default @Override
    JSONObject toJSON() {
        JSONObject res = new JSONObject();
        res.put("topic", "pressure");
        JSONUtils.addDouble(res, getPressure(), "pressure");
        res.put("source", getPressureSource().toString());
        res.put("instance", getInstance());
        return res;
    }

    @Override
    default String getMessageContentType() {
        return "Pressure";
    }

}
