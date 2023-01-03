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

import com.aboni.utils.Utils;
import org.json.JSONObject;

public interface MsgSeatalkPilotWindDatum extends Message {

    double getRollingAverageWind();

    double getWindDatum();

    @Override
    default JSONObject toJSON() {
        JSONObject j = new JSONObject();
        j.put("topic", "pilot_wind_datum");
        if (!Double.isNaN(getWindDatum())) {
            j.put("wind_datum", Math.round(getWindDatum()));
            j.put("wind_datum_view", Math.abs(Utils.normalizeDegrees180To180(Math.round(getWindDatum()))));
            j.put("wind_datum_side", getWindDatum() > 180.0 ? "P" : "S");
        }
        if (!Double.isNaN(getRollingAverageWind())) {
            j.put("rolling_average_wind", Math.round(getRollingAverageWind()));
            j.put("rolling_average_wind_view", Math.abs(Utils.normalizeDegrees180To180(Math.round(getRollingAverageWind()))));
            j.put("rolling_average_wind_side", getRollingAverageWind() > 180.0 ? "P" : "S");
        }
        return j;
    }

    @Override
    default String getMessageContentType() {
        return "PilotWind";
    }
}
