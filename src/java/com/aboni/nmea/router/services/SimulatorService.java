/*
(C) 2020, Andrea Boni
This file is part of NMEARouter.
NMEARouter is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.
NMEARouter is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.
You should have received a copy of the GNU General Public License
along with NMEARouter.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.aboni.nmea.router.services;

import com.aboni.nmea.router.agent.impl.simulator.NMEASimulatorSource;
import com.aboni.nmea.router.utils.Log;
import com.aboni.utils.Utils;
import org.json.JSONObject;

import javax.inject.Inject;

public class SimulatorService extends JSONWebService {

    @Inject
    public SimulatorService(Log log) {
        super(log);
        setLoader(this::getResult);
    }

    private JSONObject getResult(ServiceConfig config) {
        NMEASimulatorSource sim = NMEASimulatorSource.getSimulator();
        if (sim != null) {

            double speed = config.getDouble("speed", Double.NaN);
            if (!Double.isNaN(speed)) {
                sim.setSpeed(speed);
            }

            double windSpeed = config.getDouble("wspeed", Double.NaN);
            if (!Double.isNaN(windSpeed)) {
                sim.setWindSpeed(windSpeed);
            }

            double windDir = config.getDouble("wdir", Double.NaN);
            if (!Double.isNaN(windDir)) {
                sim.setWindDirection(Utils.normalizeDegrees0To360(windDir));
            }

            double heading = config.getDouble("head", Double.NaN);
            if (!Double.isNaN(heading)) {
                sim.setHeading(Utils.normalizeDegrees0To360(heading));
            }
            JSONObject res = new JSONObject();
            res.put("speed", sim.getSpeed());
            res.put("wspeed", sim.getWindSpeed());
            res.put("wdir", sim.getWindDirection());
            res.put("heading", sim.getHeading());
            return res;
        } else {
            return getError("Simulator is not configured in this system");
        }
    }
}
