package com.aboni.nmea.router.services;

import com.aboni.misc.Utils;
import com.aboni.nmea.router.agent.impl.simulator.NMEASimulatorSource;
import org.json.JSONObject;

public class SimulatorService extends JSONWebService {

	@Override
	public JSONObject getResult(ServiceConfig config) {
		JSONObject res = new JSONObject();
		if (NMEASimulatorSource.getSimulator()!=null) {
			double speed = config.getDouble("speed", Double.NaN);
			if (!Double.isNaN(speed)) {
				NMEASimulatorSource.getSimulator().setSpeed(speed);
			}

			double windSpeed = config.getDouble("wspeed", Double.NaN);
			if (!Double.isNaN(windSpeed)) {
				NMEASimulatorSource.getSimulator().setwSpeed(windSpeed);
			}

			double windDir = config.getDouble("wdir", Double.NaN);
			if (!Double.isNaN(windDir)) {
				NMEASimulatorSource.getSimulator().setwDirection(Utils.normalizeDegrees0To360(windDir));
			}

			double heading = config.getDouble("head", Double.NaN);
			if (!Double.isNaN(heading)) {
				NMEASimulatorSource.getSimulator().setHeading(Utils.normalizeDegrees0To360(heading));
			}
			res.put("speed", NMEASimulatorSource.getSimulator().getSpeed());
			res.put("wspeed", NMEASimulatorSource.getSimulator().getwSpeed());
			res.put("wdir", NMEASimulatorSource.getSimulator().getwDirection());
			res.put("heading", NMEASimulatorSource.getSimulator().getHeading());
		}
		return res;
	}
}
