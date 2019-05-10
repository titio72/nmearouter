package com.aboni.nmea.router.agent.impl.meteo;

import com.aboni.utils.ServerLog;
import com.aboni.utils.db.Event;
import com.aboni.utils.db.EventWriter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

public class DBMeteoEventWriter implements EventWriter {

	private PreparedStatement stm;
	
	public DBMeteoEventWriter() {
		// nothing to initialize
	}
	
	private void prepareStatement(Connection c) throws SQLException {
		if (stm==null) {
			stm = c.prepareStatement("insert into meteo (type, v, vMax, vMin, TS) values (?, ?, ?, ?, ?)");
        }
	}
	
	@Override
	public void reset() {
		try {
			stm.close();
		} catch (Exception e) {
			ServerLog.getLogger().error("Error closing statement in " + getClass().getSimpleName(), e);
		}
		stm = null;
	}
	
	@Override
	public void write(Event e, Connection c) throws SQLException {
		if (c!=null && e instanceof MeteoEvent) {
			prepareStatement(c);
        	MeteoEvent m = (MeteoEvent)e;
			stm.setString(1, m.getStatsSample().getTag());
	        stm.setDouble(2, m.getStatsSample().getAvg());
	        stm.setDouble(3, m.getStatsSample().getMax());
	        stm.setDouble(4, m.getStatsSample().getMin());
	        stm.setTimestamp(5, new Timestamp(e.getTime()));
	        stm.execute();
		}
    }

}
