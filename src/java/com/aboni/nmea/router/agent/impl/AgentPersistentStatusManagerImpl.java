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

import com.aboni.log.Log;
import com.aboni.log.LogStringBuilder;
import com.aboni.log.SafeLog;
import com.aboni.nmea.router.agent.AgentActivationMode;
import com.aboni.nmea.router.agent.AgentPersistentStatus;
import com.aboni.nmea.router.agent.AgentPersistentStatusManager;
import com.aboni.nmea.router.conf.MalformedConfigurationException;
import com.aboni.nmea.router.filters.DummyFilter;
import com.aboni.nmea.router.filters.JSONFilterParser;
import com.aboni.nmea.router.filters.NMEAFilter;
import com.aboni.nmea.router.utils.db.DBHelper;
import org.json.JSONObject;

import javax.inject.Inject;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

public class AgentPersistentStatusManagerImpl implements AgentPersistentStatusManager {

    public static final String FILTER_TARGET_FIELD_NAME = "filterTarget";
    public static final String FILTER_SOURCE_FIELD_NAME = "filterSource";
    private final Map<String, AgentPersistentStatus> statusMap;
    private final String table;
    private final Log log;
    private final JSONFilterParser filterParser;
    private static final String AGENT_STATUS_MANAGER_CATEGORY = "Agent Status Manager";
    private static final String FILTER_KEY_NAME = "filter";
    private static final String AGENT_KEY_NAME = "agent";
    private static final String DEFAULT_TABLE_NAME = "agent";

    @Inject
    public AgentPersistentStatusManagerImpl(Log log, JSONFilterParser filterParser) {
        this(log, filterParser, DEFAULT_TABLE_NAME);
    }

    public AgentPersistentStatusManagerImpl(Log log, JSONFilterParser serializer, String tableName) {
        if (serializer==null) throw new IllegalArgumentException("Filter set serializer is null");
        this.log = SafeLog.getSafeLog(log);
        this.filterParser = serializer;
        table = tableName;
        statusMap = new HashMap<>();
        try {
            loadAllWithRetry();
            log.info(LogStringBuilder.start(AGENT_STATUS_MANAGER_CATEGORY)
                    .wO("Load").wV("agents", statusMap.size()).toString());
        } catch (Exception e) {
            log.error(LogStringBuilder.start(AGENT_STATUS_MANAGER_CATEGORY).wO("Load").toString(), e);
        }
    }

    private String getSQL(String sql) {
        return sql.replace("TO_BE_REPLACED", table);
    }

    public static void createTable(Log log, String table) throws SQLException, MalformedConfigurationException {
        try (
                DBHelper db = new DBHelper(log, true);
                Statement st = db.getConnection().createStatement()) {
            String sqlCreate = "CREATE TABLE " + table + " (" +
                    "id VARCHAR(32) NOT NULL, " +
                    "autostart TINYINT default 1, " +
                    FILTER_TARGET_FIELD_NAME + " TEXT, " +
                    FILTER_SOURCE_FIELD_NAME + " TEXT, " +
                    "PRIMARY KEY (`id`))";
            st.execute(sqlCreate);
        }
    }

    private void loadAllWithRetry() throws SQLException, MalformedConfigurationException {
        try {
            loadAll();
        } catch (SQLException e) {
            log.error(LogStringBuilder.start(AGENT_STATUS_MANAGER_CATEGORY).wO("Load").toString(), e);
            log.info(LogStringBuilder.start(AGENT_STATUS_MANAGER_CATEGORY).wO("Create table").toString());
            createTable(log, table);
            loadAll();
        }
    }

    private void loadAll() throws SQLException, MalformedConfigurationException {
        try (DBHelper db = new DBHelper(log, true)) {
            String sqlLoadAll = getSQL("select id, autostart, " + FILTER_TARGET_FIELD_NAME + ", " + FILTER_SOURCE_FIELD_NAME + " from TO_BE_REPLACED");
            db.executeQuery(sqlLoadAll, (ResultSet rs)->{
                while (rs.next()) {
                    String agId = rs.getString("id");
                    AgentActivationMode agSt = (1 == rs.getInt("autostart")) ? AgentActivationMode.AUTO : AgentActivationMode.MANUAL;
                    String agFOut = rs.getString(FILTER_TARGET_FIELD_NAME);
                    String agFIn = rs.getString(FILTER_SOURCE_FIELD_NAME);
                    statusMap.put(agId, new AgentPersistentStatusImpl(agSt,
                            getFilter(agFOut),
                            getFilter(agFIn)));
                }
            });
        }
    }

    private NMEAFilter getFilter(String agFOut) {
        NMEAFilter filter;
        try {
            JSONObject json = new JSONObject(agFOut);
            filter = filterParser.getFilter(json);
        } catch (Exception e) {
            log.error("Error loading filter {" + agFOut + "}");
            filter = new DummyFilter("");
        }
        return filter;
    }

    @Override
    public AgentPersistentStatus getPersistentStatus(String agent) {
        return statusMap.getOrDefault(agent, null);
    }

    @Override
    public synchronized void setStartMode(String agent, AgentActivationMode s) {
        AgentPersistentStatus as = statusMap.getOrDefault(agent, null);
        if (as==null) as = new AgentPersistentStatusImpl(s, null, null);
        else as = new AgentPersistentStatusImpl(s, as.getTargetFilter(), as.getSourceFilter());
        statusMap.put(agent, as);

        log.info(LogStringBuilder.start(AGENT_STATUS_MANAGER_CATEGORY)
                .wO("Set Startup Mode").wV(AGENT_KEY_NAME, agent).wV("startup", s).toString());
        String sqlCreateAgent = getSQL("insert into TO_BE_REPLACED (id, autostart) values (?, ?) on duplicate key update autostart = ?");
        try (
                DBHelper db = new DBHelper(log, true);
                PreparedStatement updateStatusSt = db.getConnection().prepareStatement(sqlCreateAgent)) {
            updateStatusSt.setString(1, agent);
            updateStatusSt.setInt(2, (s == AgentActivationMode.AUTO) ? 1 : 0);
            updateStatusSt.setInt(3, (s == AgentActivationMode.AUTO) ? 1 : 0);
            updateStatusSt.executeUpdate();
        } catch (SQLException | MalformedConfigurationException e) {
            log.errorForceStacktrace(LogStringBuilder.start(AGENT_STATUS_MANAGER_CATEGORY)
                    .wO("Set Startup Mode").wV(AGENT_KEY_NAME, agent).wV("mode", s).toString(), e);
        }
    }

    @Override
    public synchronized void setTargetFilter(String agent, NMEAFilter filter) {
        AgentPersistentStatus as = statusMap.getOrDefault(agent, null);
        if (as==null) as = new AgentPersistentStatusImpl(AgentActivationMode.MANUAL, filter, null);
        else as = new AgentPersistentStatusImpl(as.getStatus(), filter, as.getSourceFilter());
        String filterSerialized = filter==null?null:filter.toJSON().toString();
        statusMap.put(agent, as);
        saveFilter(agent, filterSerialized, FILTER_TARGET_FIELD_NAME);
    }

    @Override
    public synchronized void setSourceFilter(String agent, NMEAFilter filter) {
        AgentPersistentStatus as = statusMap.getOrDefault(agent, null);
        if (as==null) as = new AgentPersistentStatusImpl(AgentActivationMode.MANUAL, null, filter);
        else as = new AgentPersistentStatusImpl(as.getStatus(), as.getTargetFilter(), filter);
        String filterSerialized = filter==null?null:filter.toJSON().toString();
        statusMap.put(agent, as);
        saveFilter(agent, filterSerialized, FILTER_SOURCE_FIELD_NAME);
    }

    private void saveFilter(String agent, String filterSerialized, String fieldName) {
        log.info(LogStringBuilder.start(AGENT_STATUS_MANAGER_CATEGORY)
                .wO("Set Filter").wV(AGENT_KEY_NAME, agent)
                .wV("Direction", fieldName)
                .wV(FILTER_KEY_NAME, filterSerialized).toString());
        String sqlSetAgentFilter = getSQL("insert into TO_BE_REPLACED (id, " + fieldName + ") values (?, ?) on duplicate key update " + fieldName + " = ?");
        try (
                DBHelper db = new DBHelper(log, true);
                PreparedStatement updateFilterStatement = db.getConnection().prepareStatement(sqlSetAgentFilter)) {
            updateFilterStatement.setString(1, agent);
            updateFilterStatement.setString(2, filterSerialized);
            updateFilterStatement.setString(3, filterSerialized);
            updateFilterStatement.executeUpdate();
        } catch (SQLException | MalformedConfigurationException e) {
            log.errorForceStacktrace(LogStringBuilder.start(AGENT_STATUS_MANAGER_CATEGORY)
                    .wO("Set Filter").wV(AGENT_KEY_NAME, agent).wV(FILTER_KEY_NAME, filterSerialized).toString(), e);
        }
    }
}
