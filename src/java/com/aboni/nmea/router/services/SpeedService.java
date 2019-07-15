package com.aboni.nmea.router.services;

import com.aboni.utils.TimeSerieSample;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.List;

public class SpeedService extends SampledQueryService {

    public SpeedService() {
    	// nothing to initialize
    }



    @Override
    protected void fillResponse(ServiceOutput response, List<TimeSerieSample> samples) throws IOException {
		long lastTS = 0;
		double lastV = 0.0;
		boolean lastSkipped = true;
		boolean lastNull = false;
        int count = 0;

        response.getWriter().write("{\"serie\":[");
        if (samples!=null) {
	        for (TimeSerieSample s: samples) {
		        if (s.getV()<=0.1 && lastV<=0.1) {
					if (count > 0) {
						if (!lastSkipped) {
							count = writeZero(count, response, s.getT0());
						} else if (!lastNull) {
							count = writeNull(count, response, s.getT0());
							lastNull = true;
						}
					} else {
						// skip
					}
					lastSkipped = true;
				} else {
		        	if (lastSkipped && lastTS!=0) {
		        		lastSkipped = false;
						count = writeZero(count, response, lastTS);
					}
					count = writeValue(count, response, s);
					lastNull = false;
				}
				lastV = s.getV();
				lastTS = s.getLastTs();
			}
        }
        response.getWriter().write("]}");
    }

	private int writeValue(int count, ServiceOutput response, TimeSerieSample s) throws IOException {
		return write(count, response, s.getT0(), s.getvMin(), s.getV(), s.getvMax());
	}

	private int writeNull(int count, ServiceOutput response, long ts) throws IOException {
		return write(count, response, ts, "null", "null", "null");
	}

	private int writeZero(int count, ServiceOutput response, long ts) throws IOException {
		return write(count, response, ts, "0.0", "0.0", "0.0");
	}

	private int write(int count, ServiceOutput response, long ts, Object min, Object avg, Object max) throws IOException {
		if (count>0) response.getWriter().write(",");
		response.getWriter().write("{\"time\":\"" + new Timestamp(ts).toString() + "\",");
		response.getWriter().write("\"vMin\":" + min + ",");
		response.getWriter().write("\"v\":" + avg + ",");
		response.getWriter().write("\"vMax\":" + max + "}");
		return count + 1;
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
