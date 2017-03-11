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
	
	private PreparedStatement updateStatusSt;
	
	public AgentStatusImpl() {
		status = new HashMap<>();
		updateStatusSt = null;
		try {
			db = new DBHelper(true);
			loadAll();
		} catch (Exception e) {
			ServerLog.getLogger().Error("Error loading agents status", e);
		}
	}

	private PreparedStatement getUpdateSt() {
		if (updateStatusSt==null) {
			try {
				if (db!=null) {
					updateStatusSt = db.getConnection().prepareStatement("insert into agent (id, autostart) values (?, ?) on duplicate key update autostart = ?");
				}
				//updateStatusSt = db.getConnection().prepareStatement("update agent set status = ? where id = ?");
			} catch (Exception e) {
				ServerLog.getLogger().Error("Error creating statement for agent status update", e);
			}
		}
		return updateStatusSt;
	}
	
	private void loadAll() throws SQLException {
		String sql = "select id, autostart from agent";
		Statement st = db.getConnection().createStatement();
		ResultSet rs = st.executeQuery(sql);
		while (rs.next()) {
			String agId = rs.getString(1);
			STATUS agSt = (1==rs.getInt(2))?STATUS.AUTO:STATUS.MANUAL;
			status.put(agId,  agSt);
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

}
