package com.aboni.nmea.router.services;

import com.aboni.utils.Sample;
import com.aboni.utils.Sampler;
import com.aboni.utils.ServerLog;
import com.aboni.utils.db.DBHelper;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;

abstract class SampledQueryService implements WebService {

    public SampledQueryService() {
    }
    
    protected abstract String	getTable();
    protected abstract String 	getMaxField();
    protected abstract String	getAvgField();
    protected abstract String	getMinField();
    protected abstract String	getWhere();
    protected abstract void 	fillResponse(ServiceOutput response, List<Sample> samples) throws IOException;
	protected abstract void 	onPrepare(ServiceConfig config);
    
	private static final int DEFAULT_MAX_SAMPLES = 150;
	
	private int getMaxSamples(ServiceConfig config) {
        return config.getInteger("samples", DEFAULT_MAX_SAMPLES);
	}
	
    @Override
    public void doIt(ServiceConfig config, ServiceOutput response) {
    	
    	onPrepare(config);

        response.setContentType("application/json");
        DateRangeParameter fromTo = new DateRangeParameter(config);
        Calendar cFrom = fromTo.getFrom();
        Calendar cTo = fromTo.getTo();
        
        int maxSamples = getMaxSamples(config);
        
        try (DBHelper db = new DBHelper(true)) {
            DBHelper.Range range = db.getTimeframe(getTable(), cFrom, cTo);
            List<Sample> samples = null;
            if (range!=null) {
                Sampler sampler = getSampler(cFrom, cTo, maxSamples, db, range);
                samples = sampler.getSamples();
            }
        	fillResponse(response, samples);
        } catch (Exception e) {
            ServerLog.getLogger().error("Error writing sample", e);
        }
    }

    private Sampler getSampler(Calendar cFrom, Calendar cTo, int maxSamples, DBHelper db, DBHelper.Range range) throws SQLException {
        int sampling = range.getSampling(maxSamples);
        Sampler sampler = new Sampler(sampling, maxSamples);
        try (PreparedStatement stm = db.getTimeSeries(getTable(), new String[]{getMaxField(), getAvgField(), getMinField()}, cFrom, cTo, getWhere())) {
            readSamples(sampler, stm);
        }
        return sampler;
    }

    private void readSamples(Sampler sampler, PreparedStatement stm) throws SQLException {
        try (ResultSet rs = stm.executeQuery()) {
            while (rs.next()) {
                Timestamp ts = rs.getTimestamp(1);
                double vMax = rs.getDouble(2);
                double v = rs.getDouble(3);
                double vMin = rs.getDouble(4);
                sampler.doSampling(ts.getTime(), vMax, v, vMin);
            }
        }
    }
}