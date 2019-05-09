package com.aboni.nmea.router.services;

import com.aboni.utils.Sample;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.List;

public class MeteoService extends SampledQueryService {

	private String type;
    
    @Override
	protected void fillResponse(ServiceOutput response, List<Sample> samples) throws IOException {
		response.getWriter().write("{\"type\":\""+ type +"\", \"serie\":[");
		boolean first = true;
        if (samples!=null) {
			for (Sample s: samples) {
			    Timestamp ts = new Timestamp(s.getT0());
			    double vMax = s.getvMax();
			    double v = s.getV();
			    double vMin = s.getvMin();
				
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
