/*
(C) 2020, Andrea Boni
This file is part of NMEARouter.
NMEARouter is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.
NMEARouter is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.
You should have received a copy of the GNU General Public License
along with NMEARouter.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.aboni.nmea.router.agent.impl;

import com.aboni.nmea.router.agent.AgentStatusManager;
import com.aboni.utils.ServerLog;
import com.aboni.utils.db.DBHelper;

import javax.inject.Inject;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

public class AgentStatusManagerImpl implements AgentStatusManager {

    private static final String ERROR_MSG = "Cannot persist status for Agent";

    private final Map<String, AgentStatusManager.STATUS> status;
    private final Map<String, String> filterOut;
    private final Map<String, String> filterIn;

    @Inject
    public AgentStatusManagerImpl() {
        status = new HashMap<>();
        filterOut = new HashMap<>();
        filterIn = new HashMap<>();
        try {
            loadAll();
        } catch (Exception e) {
            ServerLog.getLogger().error("Error loading agents status", e);
        }
    }

    private void loadAll() throws SQLException, ClassNotFoundException {
        try (DBHelper db = new DBHelper(true)) {
            String sql = "select id, autostart, filterOut, filterIn from agent";
            try (Statement st = db.getConnection().createStatement()) {
                try (ResultSet rs = st.executeQuery(sql)) {
                    while (rs.next()) {
                        String agId = rs.getString(1);
                        STATUS agSt = (1 == rs.getInt(2)) ? STATUS.AUTO : STATUS.MANUAL;
                        String agFOut = rs.getString(3);
                        String agFIn = rs.getString(4);
                        status.put(agId, agSt);
                        filterOut.put(agId, agFOut);
                        filterIn.put(agId, agFIn);
                    }
                }
            }
        }
    }

    @Override
    public synchronized STATUS getStartMode(String agent) {
        return status.getOrDefault(agent, STATUS.UNKNOWN);
    }

    @Override
    public synchronized String getFilterOutData(String agent) {
        return filterOut.getOrDefault(agent, null);
    }

    @Override
    public synchronized String getFilterInData(String agent) {
        return filterIn.getOrDefault(agent, null);
    }

    @Override
    public synchronized void setStartMode(String agent, STATUS s) {
        status.put(agent, s);
        ServerLog.getLogger().info("Saving startup setting for agent {" + agent + "} Startup {" + s + "}");
        try (DBHelper db = new DBHelper(true)) {
            try (PreparedStatement updateStatusSt = db.getConnection().prepareStatement("insert into agent (id, autostart) values (?, ?) on duplicate key update autostart = ?")) {
                updateStatusSt.setString(1, agent);
                updateStatusSt.setInt(2, (s == STATUS.AUTO) ? 1 : 0);
                updateStatusSt.setInt(3, (s == STATUS.AUTO) ? 1 : 0);
                updateStatusSt.executeUpdate();
            }
        } catch (SQLException | ClassNotFoundException e) {
            ServerLog.getLogger().error(ERROR_MSG + " {" + agent + "} Start {" + s + "}", e);
        }
    }

    @Override
    public synchronized void setFilterOutData(String agent, String agData) {
        filterOut.put(agent, agData);
        ServerLog.getLogger().info("Saving outbound filter for agent {" + agent + "} FilterOut {" + agData + "}");
        try (DBHelper db = new DBHelper(true)) {
            try (PreparedStatement updateFilterOut =
                         db.getConnection().prepareStatement("update agent set filterOut=? where id=?")) {
                updateFilterOut.setString(1, agData);
                updateFilterOut.setString(2, agent);
                updateFilterOut.executeUpdate();
            }
        } catch (SQLException | ClassNotFoundException e) {
            ServerLog.getLogger().error(ERROR_MSG + " {" + agent + "} FilterOut {" + agData + "}", e);
        }
    }

    @Override
    public synchronized void setFilterInData(String agent, String agData) {
        filterIn.put(agent, agData);
        ServerLog.getLogger().info("Saving inbound filter for agent {" + agent + "} FilterIn {" + agData + "}");
        try (DBHelper db = new DBHelper(true)) {
            try (PreparedStatement updateFilterIn = db.getConnection().prepareStatement("update agent set filterIn=? where id=?")) {
                updateFilterIn.setString(1, agData);
                updateFilterIn.setString(2, agent);
                updateFilterIn.executeUpdate();
            }
        } catch (SQLException | ClassNotFoundException e) {
            ServerLog.getLogger().error(ERROR_MSG + " {" + agent + "} FilterIn {" + agData + "}", e);
        }
    }
}
