package com.aboni.nmea.router.services;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;

import com.aboni.utils.Sample;
import com.aboni.utils.Sampler;
import com.aboni.utils.ServerLog;
import com.aboni.utils.db.DBHelper;

abstract class SampledQueryService implements WebService {

	private DBHelper db;
    private PreparedStatement stm;
    
    public SampledQueryService() {
    }
    
    protected abstract String	getTable();
    protected abstract String 	getMaxField();
    protected abstract String	getAvgField();
    protected abstract String	getMinField();
    protected abstract String	getWhere();
    protected abstract void 	fillResponse(ServiceOutput response, List<Sample> samples) throws IOException;
	protected abstract void 	onPrepare(ServiceConfig config);
    
	private static int DEFAULT_MAX_SAMPLES = 150;
	
	private int getMaxSamples(ServiceConfig config) {
        int maxSamples = DEFAULT_MAX_SAMPLES;
        try {
        	maxSamples = Integer.parseInt(config.getParameter("samples", "" + DEFAULT_MAX_SAMPLES));
        } catch (Exception e) {}
		return maxSamples;
	}
	
    @Override
    public void doIt(ServiceConfig config, ServiceOutput response) {
    	
    	onPrepare(config);

        response.setContentType("application/json");
        DateRangeParameter fromTo = new DateRangeParameter(config);
        Calendar cFrom = fromTo.getFrom();
        Calendar cTo = fromTo.getTo();
        
        int maxSamples = getMaxSamples(config);
        
        try {
            db = new DBHelper(true);
            DBHelper.Range range = db.getTimeframe(getTable(), cFrom, cTo);
            List<Sample> samples = null;
            if (range!=null) {
                int sampling = range.getSampling(maxSamples);
                Sampler sampler = new Sampler(sampling, maxSamples);
                stm = db.getTimeSeries(getTable(), new String[] {getMaxField(), getAvgField(), getMinField()}, cFrom, cTo, getWhere());
	            ResultSet rs = stm.executeQuery();
	            while (rs.next()) {
	                Timestamp ts = rs.getTimestamp(1);
	                double vMax = rs.getDouble(2);
	                double v = rs.getDouble(3);
	                double vMin = rs.getDouble(4);
	                sampler.doSampling(ts.getTime(), vMax, v, vMin);
	            }
	            samples = sampler.getSamples();
            }
        	fillResponse(response, samples);
        } catch (Exception e) {
        	e.printStackTrace();
            ServerLog.getLogger().Error("Error writing sample", e);
        } finally {
        	try {
				db.close();
			} catch (Exception e2) {}
        }
    }
}
