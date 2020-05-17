package com.aboni.nmea.router.services;

import com.aboni.misc.Utils;
import com.aboni.nmea.router.agent.impl.simulator.NMEASimulatorSource;
import org.json.JSONObject;

public class SimulatorService extends JSONWebService {

    public SimulatorService() {
        super();
        setLoader(this::getResult);
    }

    private JSONObject getResult(ServiceConfig config) {
		NMEASimulatorSource sim = NMEASimulatorSource.getSimulator();
		if (sim!=null) {

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
