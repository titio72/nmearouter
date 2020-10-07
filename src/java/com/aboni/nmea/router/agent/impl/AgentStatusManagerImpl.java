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
import com.aboni.nmea.router.conf.MalformedConfigurationException;
import com.aboni.utils.Log;
import com.aboni.utils.LogStringBuilder;
import com.aboni.utils.db.DBHelper;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

public class AgentStatusManagerImpl implements AgentStatusManager {

    public static final String AGENT_STATUS_MANAGER_CATEGORY = "Agent Status Manager";
    public static final String FILTER_KEY_NAME = "filter";
    public static final String AGENT_KEY_NAME = "agent";
    private final Map<String, AgentStatusManager.STATUS> status;
    private final Map<String, String> filterOut;
    private final Map<String, String> filterIn;

    private Log log;

    @Inject
    public AgentStatusManagerImpl(@NotNull Log log) {
        this.log = log;
        status = new HashMap<>();
        filterOut = new HashMap<>();
        filterIn = new HashMap<>();
        try {
            loadAll();
            log.info(LogStringBuilder.start(AGENT_STATUS_MANAGER_CATEGORY)
                    .wO("Load").wV("agents", status.size()).toString());
        } catch (Exception e) {
            log.error(LogStringBuilder.start(AGENT_STATUS_MANAGER_CATEGORY).wO("Load").toString(), e);
        }
    }

    private void loadAll() throws SQLException, ClassNotFoundException, MalformedConfigurationException {
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
        log.info(LogStringBuilder.start(AGENT_STATUS_MANAGER_CATEGORY)
                .wO("Set Startup Mode").wV(AGENT_KEY_NAME, agent).wV("startup", s).toString());
        try (DBHelper db = new DBHelper(true)) {
            try (PreparedStatement updateStatusSt = db.getConnection().prepareStatement("insert into agent (id, autostart) values (?, ?) on duplicate key update autostart = ?")) {
                updateStatusSt.setString(1, agent);
                updateStatusSt.setInt(2, (s == STATUS.AUTO) ? 1 : 0);
                updateStatusSt.setInt(3, (s == STATUS.AUTO) ? 1 : 0);
                updateStatusSt.executeUpdate();
            }
        } catch (SQLException | MalformedConfigurationException | ClassNotFoundException e) {
            log.errorForceStacktrace(LogStringBuilder.start(AGENT_STATUS_MANAGER_CATEGORY)
                    .wO("Set Startup Mode").wV(AGENT_KEY_NAME, agent).wV("mode", s).toString(), e);
        }
    }

    @Override
    public synchronized void setFilterOutData(String agent, String agData) {
        filterOut.put(agent, agData);
        log.info(LogStringBuilder.start(AGENT_STATUS_MANAGER_CATEGORY)
                .wO("Set Filter Out").wV(AGENT_KEY_NAME, agent).wV(FILTER_KEY_NAME, agData).toString());
        try (DBHelper db = new DBHelper(true)) {
            try (PreparedStatement updateFilterOut =
                         db.getConnection().prepareStatement("update agent set filterOut=? where id=?")) {
                updateFilterOut.setString(1, agData);
                updateFilterOut.setString(2, agent);
                updateFilterOut.executeUpdate();
            }
        } catch (SQLException | MalformedConfigurationException | ClassNotFoundException e) {
            log.errorForceStacktrace(LogStringBuilder.start(AGENT_STATUS_MANAGER_CATEGORY)
                    .wO("Set Filter Out").wV(AGENT_KEY_NAME, agent).wV(FILTER_KEY_NAME, agData).toString(), e);
        }
    }

    @Override
    public synchronized void setFilterInData(String agent, String agData) {
        filterIn.put(agent, agData);
        log.info(LogStringBuilder.start(AGENT_STATUS_MANAGER_CATEGORY)
                .wO("Set Filter In").wV(AGENT_KEY_NAME, agent).wV(FILTER_KEY_NAME, agData).toString());
        try (DBHelper db = new DBHelper(true)) {
            try (PreparedStatement updateFilterIn = db.getConnection().prepareStatement("update agent set filterIn=? where id=?")) {
                updateFilterIn.setString(1, agData);
                updateFilterIn.setString(2, agent);
                updateFilterIn.executeUpdate();
            }
        } catch (SQLException | MalformedConfigurationException | ClassNotFoundException e) {
            log.errorForceStacktrace(LogStringBuilder.start(AGENT_STATUS_MANAGER_CATEGORY)
                    .wO("Set Filter In").wV(AGENT_KEY_NAME, agent).wV(FILTER_KEY_NAME, agData).toString(), e);
        }
    }
}
