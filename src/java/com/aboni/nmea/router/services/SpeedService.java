package com.aboni.nmea.router.services;

import com.aboni.utils.Sample;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.List;

public class SpeedService extends SampledQueryService {

    public SpeedService() {
    	// nothing to initialize
    }
    
	@Override
    protected void fillResponse(ServiceOutput response, List<Sample> samples) throws IOException {
        boolean first = true;
        response.getWriter().write("{\"serie\":[");
        if (samples!=null) {
	        for (Sample s: samples) {
		        if (!first) {
	                response.getWriter().write(",");
	        	}
	            response.getWriter().write("{\"time\":\"" + new Timestamp(s.getT0()).toString() + "\",");
	            response.getWriter().write("\"vMin\":" + s.getvMin() + ",");
	            response.getWriter().write("\"v\":" + s.getV() + ",");
	            response.getWriter().write("\"vMax\":" + s.getvMax() + "}");
	            first = false;
	        }
        }
        response.getWriter().write("]}");
    }

	@Override
	protected String getTable() {
		return "track";
	}

	@Override
	protected String getMaxField() {
		return "maxSpeed";
	}

	@Override
	protected String getAvgField() {
		return "speed";
	}

	@Override
	protected String getMinField() {
		return "speed";
	}

	@Override
	protected String getWhere() {
		return null;
	}

	@Override
	protected void onPrepare(ServiceConfig config) {
    	// nothing to initialize
	}
}
