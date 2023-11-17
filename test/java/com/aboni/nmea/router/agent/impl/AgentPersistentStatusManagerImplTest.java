package com.aboni.nmea.router.agent.impl;

import com.aboni.nmea.router.agent.AgentActivationMode;
import com.aboni.nmea.router.conf.MalformedConfigurationException;
import com.aboni.nmea.router.filters.JSONFilterParser;
import com.aboni.nmea.router.filters.NMEAFilter;
import com.aboni.nmea.router.filters.DummyFilter;
import com.aboni.log.ConsoleLog;
import com.aboni.nmea.router.agent.impl.AgentPersistentStatusManagerImpl;
import com.aboni.nmea.router.utils.db.DBHelper;
import junit.framework.TestCase;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class AgentPersistentStatusManagerImplTest extends TestCase {

    private AgentPersistentStatusManagerImpl statusManager;

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
        try (
                DBHelper h = new DBHelper(null,true);
                Statement st = h.getConnection().createStatement()) {
            st.execute("drop table agent_test");
        }
    }

    @Test
    public void testAutocreate() throws Exception {
        create();
        try (DBHelper h = new DBHelper(null,true)) {
            h.executeQuery("select * from agent_test", (DBHelper.QueryReader<Exception>) resultSet -> assertNotNull(resultSet));
        }
    }

    @Test
    public void testLoad() throws MalformedConfigurationException, ClassNotFoundException, SQLException {
        AgentPersistentStatusManagerImpl.createTable(ConsoleLog.getLogger(), "agent_test");
        try (DBHelper h = new DBHelper(ConsoleLog.getLogger(), true)) {
            loadTestData(h, "AG1", true, "in1", "out1");
            loadTestData(h, "AG2", false, "in2", "out2");
            create();
            assertNotNull(statusManager.getPersistentStatus("AG1"));
            assertNotNull(statusManager.getPersistentStatus("AG2"));
            assertEquals(AgentActivationMode.AUTO, statusManager.getPersistentStatus("AG1").getStatus());
            assertEquals(AgentActivationMode.MANUAL, statusManager.getPersistentStatus("AG2").getStatus());
            assertEquals("in1", statusManager.getPersistentStatus("AG1").getSourceFilter().toString());
            assertEquals("in2", statusManager.getPersistentStatus("AG2").getSourceFilter().toString());
            assertEquals("out1", statusManager.getPersistentStatus("AG1").getTargetFilter().toString());
            assertEquals("out2", statusManager.getPersistentStatus("AG2").getTargetFilter().toString());
        }
    }

    @Test
    public void testSetAutostart() throws Exception {
        AgentPersistentStatusManagerImpl.createTable(ConsoleLog.getLogger(), "agent_test");
        try (DBHelper h = new DBHelper(ConsoleLog.getLogger(), true)) {
            loadTestData(h, "AG1", false, "in1", "out1");
            create();
            statusManager.setStartMode("AG1", AgentActivationMode.AUTO);
            assertEquals(AgentActivationMode.AUTO, statusManager.getPersistentStatus("AG1").getStatus());
            checkAutostartStatus(h,"AG1", 1);
        }
    }

    @Test
    public void testSetFilterTarget() throws Exception {
        AgentPersistentStatusManagerImpl.createTable(ConsoleLog.getLogger(), "agent_test");
        try (DBHelper h = new DBHelper(ConsoleLog.getLogger(), true)) {
            loadTestData(h, "AG1", false, "in1", "out1");
            create();
            statusManager.setTargetFilter("AG1", new DummyFilter("new filter target"));
            assertEquals("new filter target", statusManager.getPersistentStatus("AG1").getTargetFilter().toString());
            checkFilterTarget(h,"AG1", "new filter target");
        }
    }

    @Test
    public void testSetFilterIn() throws Exception {
        AgentPersistentStatusManagerImpl.createTable(ConsoleLog.getLogger(), "agent_test");
        try (DBHelper h = new DBHelper(ConsoleLog.getLogger(), true)) {
            loadTestData(h, "AG1", false, "in1", "out1");
            create();
            statusManager.setSourceFilter("AG1", new DummyFilter("new filter source"));
            assertEquals("new filter source", statusManager.getPersistentStatus("AG1").getSourceFilter().toString());
            checkFilterSource(h,"AG1", "new filter source");
        }
    }

    private void checkAutostartStatus(DBHelper h, String ag, int auto) throws Exception {
        try (Statement st = h.getConnection().createStatement()) {
            String sql = "select * from agent_test where id='" + ag + "'";
            ResultSet rs = st.executeQuery(sql);
            if (rs.next()) {
                assertEquals(auto, rs.getInt("autoStart"));
            } else {
                fail("agent " + ag + " not found");
            }
        }
    }

    private void checkFilterTarget(DBHelper h, String ag, String filter) throws Exception {
        checkFilter(h, ag, filter, "filterTarget");
    }

    private void checkFilterSource(DBHelper h, String ag, String filter) throws Exception {
        checkFilter(h, ag, filter, "filterSource");
    }

    private void checkFilter(DBHelper h, String ag, String filter, String filterField) throws Exception {
        try (Statement st = h.getConnection().createStatement()) {
            String sql = "select * from agent_test where id='" + ag + "'";
            ResultSet rs = st.executeQuery(sql);
            if (rs.next()) {
                assertEquals(new DummyFilter(filter).toJSON().toString(), rs.getString(filterField));
            } else {
                fail("agent " + ag + " not found");
            }
        }
    }

    private void loadTestData(DBHelper h, String ag, boolean auto, String src, String trg) throws SQLException, MalformedConfigurationException, ClassNotFoundException {
        try (Statement st = h.getConnection().createStatement()) {
            String sql = "insert into agent_test (id, autoStart, filterTarget, filterSource) values (" +
                    "'" + ag + "', " + (auto?1:0) + ", " +
                    "'" + new DummyFilter(trg).toJSON() + "', " +
                    "'" + new DummyFilter(src).toJSON() + "')";
            st.execute(sql);
        }
    }

    private void create() {
        statusManager = new AgentPersistentStatusManagerImpl(ConsoleLog.getLogger(), new JSONFilterParser() {

            @Override
            public NMEAFilter getFilter(JSONObject obj) {
                return DummyFilter.parseFilter(obj);
            }
        }, "agent_test");
    }
}