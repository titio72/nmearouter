package com.aboni.nmea.router.services;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.List;

import com.aboni.utils.Sample;

public class MeteoService extends SampledQueryService {

	private String type;
    
    public MeteoService() {
    }
     
    @Override
	protected void fillResponse(ServiceOutput response, List<Sample> samples) throws IOException {
		response.getWriter().write("{\"type\":\""+ type +"\", \"serie\":[");
		boolean first = true;
        if (samples!=null) {
			for (Sample s: samples) {
			    Timestamp ts = new Timestamp(s.t0);
			    double vMax = s.vMax;
			    double v = s.v;
			    double vMin = s.vMin;
				
				if (!first) {
			        response.getWriter().write(",");
				}
			    response.getWriter().write("{\"time\":\"" + ts.toString() + "\",");
			    response.getWriter().write("\"vMin\":" + vMin + ",");
			    response.getWriter().write("\"v\":" + v + ",");
			    response.getWriter().write("\"vMax\":" + vMax + "}");
			    first = false;
			}
        }
		response.getWriter().write("]}");
	}

	@Override
	protected String getTable() {
		return "meteo";
	}

	@Override
	protected String getMaxField() {
		return "vMax";
	}

	@Override
	protected String getAvgField() {
		return "v";
	}

	@Override
	protected String getMinField() {
		return "vMin";
	}

	@Override
	protected String getWhere() {
		return " type = '" + type + "'";
	}

	@Override
	protected void onPrepare(ServiceConfig config) {
        type = config.getParameter("type", "PR_");
	}
}
