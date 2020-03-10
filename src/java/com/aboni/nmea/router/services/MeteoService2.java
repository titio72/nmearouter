package com.aboni.nmea.router.services;

import com.aboni.nmea.router.meteo.Meteo;
import com.aboni.nmea.router.meteo.MeteoManagementException;
import com.aboni.utils.ThingsFactory;
import org.json.JSONObject;

import java.time.Instant;

public class MeteoService2 extends JSONWebService {

    public MeteoService2() {
        super();
        setLoader(this::getResult);
    }

    private JSONObject getResult(ServiceConfig config) throws JSONGenerationException {
        Instant from = config.getParamAsInstant("dateFrom", Instant.now().minusSeconds(86400L), 0);
        Instant to = config.getParamAsInstant("dateTo", Instant.now(), 1);

        Meteo m = ThingsFactory.getInstance(Meteo.class);
        if (m != null) {
            try {
                return m.getMeteoSerie(from, to);
            } catch (MeteoManagementException e) {
                throw new JSONGenerationException("Error loading meteo data", e);
            }
        } else {
            throw new JSONGenerationException("Error loading meteo time series - no suitable loader implementation");
        }
    }
}
