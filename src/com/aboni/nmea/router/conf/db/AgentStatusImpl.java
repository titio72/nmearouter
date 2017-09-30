package com.aboni.nmea.router.conf.db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import com.aboni.utils.ServerLog;
import com.aboni.utils.db.DBHelper;

public class AgentStatusImpl implements AgentStatus {

	private DBHelper db;
	
	private Map<String, AgentStatus.STATUS> status;
	private Map<String, String> filterOut;
	private Map<String, String> filterIn;
	
	private PreparedStatement updateStatusSt;
	private PreparedStatement updateFilterIn;
	private PreparedStatement updateFilterOut;
	
	public AgentStatusImpl() {
		status = new HashMap<>();
		filterOut = new HashMap<>();
		filterIn = new HashMap<>();
		updateStatusSt = null;
		updateFilterIn = null;
		updateFilterOut = null;
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

	private PreparedStatement getUpdateFilterOutSt() {
		if (updateFilterOut==null) {
			initStatements();
		}
		return updateFilterOut;
	}

	private PreparedStatement getUpdateFilterInSt() {
		if (updateFilterIn==null) {
			initStatements();
		}
		return updateFilterIn;
	}

	private void initStatements() {
		try {
			if (db!=null) {
				updateStatusSt = db.getConnection().prepareStatement("insert into agent (id, autostart) values (?, ?) on duplicate key update autostart = ?");
				updateFilterOut = db.getConnection().prepareStatement("update agent set filterOut=? where id=?");
				updateFilterIn = db.getConnection().prepareStatement("update agent set filterIn=? where id=?");
			}
		} catch (Exception e) {
			ServerLog.getLogger().Error("Error creating statement for agent status update", e);
		}
	}
	
	private void loadAll() throws SQLException {
		String sql = "select id, autostart, filterOut, filterIn from agent";
		Statement st = db.getConnection().createStatement();
		ResultSet rs = st.executeQuery(sql);
		while (rs.next()) {
			String agId = rs.getString(1);
			STATUS agSt = (1==rs.getInt(2))?STATUS.AUTO:STATUS.MANUAL;
			String agFOut = rs.getString(3);
			String agFIn = rs.getString(4);
			status.put(agId,  agSt);
			filterOut.put(agId, agFOut);
			filterIn.put(agId, agFIn);
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
		if (filterOut.containsKey(agent)) {
			return filterOut.get(agent);
		} else {
			return null;
		}
	}
	@Override
	public synchronized String getFilterInData(String agent) {
		if (filterIn.containsKey(agent)) {
			return filterIn.get(agent);
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
		filterOut.put(agent, agData);
		PreparedStatement p = getUpdateFilterOutSt();
		if (p!=null) {
			try {
				p.setString(1, agData);
				p.setString(2, agent);
				p.executeUpdate();
			} catch (Exception e) {
				ServerLog.getLogger().Error("Cannot persist status for Agent {" + agent + "} Data {" + agData + "}", e);
			}
		} else {
			ServerLog.getLogger().Error("Cannot persist status for Agent {" + agent + "} Data {" + agData + "}");
		}
	}

	@Override
	public synchronized void setFilterInData(String agent, String agData) {
		filterIn.put(agent, agData);
		PreparedStatement p = getUpdateFilterInSt();
		if (p!=null) {
			try {
				p.setString(1, agData);
				p.setString(2, agent);
				p.executeUpdate();
			} catch (Exception e) {
				ServerLog.getLogger().Error("Cannot persist status for Agent {" + agent + "} Data {" + agData + "}", e);
			}
		} else {
			ServerLog.getLogger().Error("Cannot persist status for Agent {" + agent + "} Data {" + agData + "}");
		}
	}

}
