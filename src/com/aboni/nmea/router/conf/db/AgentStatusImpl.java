package com.aboni.nmea.router.conf.db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import com.aboni.utils.DBHelper;
import com.aboni.utils.ServerLog;

public class AgentStatusImpl implements AgentStatus {

	private DBHelper db;
	
	private Map<String, AgentStatus.STATUS> status;
	private Map<String, String> data;
	
	private PreparedStatement updateStatusSt;
	private PreparedStatement updateDataSt;
	
	public AgentStatusImpl() {
		status = new HashMap<>();
		data = new HashMap<>();
		updateStatusSt = null;
		updateDataSt = null;
		try {
			db = new DBHelper(true);
			loadAll();
		} catch (Exception e) {
			ServerLog.getLogger().Error("Error loading agents status", e);
		}
	}

	private PreparedStatement getUpdateSt() {
		if (updateStatusSt==null) {
			initStatements();
		}
		return updateStatusSt;
	}

	private PreparedStatement getUpdateDataSt() {
		if (updateDataSt==null) {
			initStatements();
		}
		return updateDataSt;
	}

	private void initStatements() {
		try {
			if (db!=null) {
				updateStatusSt = db.getConnection().prepareStatement("insert into agent (id, autostart) values (?, ?) on duplicate key update autostart = ?");
				updateDataSt = db.getConnection().prepareStatement("insert into agent (id, filterOut) values (?, ?) on duplicate key update filterOut = ?");
			}
		} catch (Exception e) {
			ServerLog.getLogger().Error("Error creating statement for agent status update", e);
		}
	}
	
	private void loadAll() throws SQLException {
		String sql = "select id, autostart, filterOut from agent";
		Statement st = db.getConnection().createStatement();
		ResultSet rs = st.executeQuery(sql);
		while (rs.next()) {
			String agId = rs.getString(1);
			STATUS agSt = (1==rs.getInt(2))?STATUS.AUTO:STATUS.MANUAL;
			String agData = rs.getString(3);
			status.put(agId,  agSt);
			data.put(agId, agData);
		}
		st.close();
	}

	@Override
	public synchronized STATUS getStartMode(String agent) {
		if (status.containsKey(agent)) {
			return status.get(agent);
		} else {
			return STATUS.UNKNOWN;
		}
	}

	@Override
	public synchronized String getFilterOutData(String agent) {
		if (data.containsKey(agent)) {
			return data.get(agent);
		} else {
			return null;
		}
	}

	@Override
	public synchronized void setStartMode(String agent, STATUS s) {
		status.put(agent, s);
		PreparedStatement p = getUpdateSt();
		if (p!=null) {
			try {
				p.setString(1, agent);
				p.setInt(2, (s==STATUS.AUTO)?1:0);
				p.setInt(3, (s==STATUS.AUTO)?1:0);
				p.executeUpdate();
			} catch (Exception e) {
				ServerLog.getLogger().Error("Cannot persist status for Agent {" + agent + "} Start {" + s + "}", e);
			}
		} else {
			ServerLog.getLogger().Error("Cannot persist status for Agent {" + agent + "} Start {" + status + "}");
		}
	}


	@Override
	public synchronized void setFilterOutData(String agent, String agData) {
		data.put(agent, agData);
		PreparedStatement p = getUpdateDataSt();
		if (p!=null) {
			try {
				p.setString(1, agent);
				p.setString(2, agData);
				p.setString(3, agData);
				p.executeUpdate();
			} catch (Exception e) {
				ServerLog.getLogger().Error("Cannot persist status for Agent {" + agent + "} Data {" + agData + "}", e);
			}
		} else {
			ServerLog.getLogger().Error("Cannot persist status for Agent {" + agent + "} Data {" + agData + "}");
		}
	}

}
