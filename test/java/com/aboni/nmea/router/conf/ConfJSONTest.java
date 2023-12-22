/*
 * Copyright (c) 2022,  Andrea Boni
 * This file is part of NMEARouter.
 * NMEARouter is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * NMEARouter is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with NMEARouter.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.aboni.nmea.router.conf;

import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

public class ConfJSONTest {

    private static final String confEmpty = "{}";
    private static final String confEmptyAgents = "{ agents: [] }";
    private static final String confLogDebug = "{ \"logLevel\": \"DEBUG\" }";
    private static final String confSimulatorX = "{" +
            "   agents: [" +
            "       {" +
            "           \"type\": \"SimulatorX\"," +
            "           \"name\": \"SIM1\"," +
            "           \"qos\": \"truewind_sog;enrich_hdm;rmc_filter;speed_filter\"" +
            "       }" +
            "   ]" +
            "}";
    private static final String confSimulatorXOnlyOut = "{" +
            "   agents: [" +
            "       {" +
            "           \"inout\": \"OUT\"," +
            "           \"type\": \"SimulatorX\"," +
            "           \"name\": \"SIM1\"," +
            "           \"qos\": \"truewind_sog;enrich_hdm;rmc_filter;speed_filter\"" +
            "       }" +
            "   ]" +
            "}";

    private void createFile(String file, String j) throws IOException {
        File f = new File(file);
        if (f.exists()) f.delete();
        try (FileWriter w = new FileWriter(f)) {
            w.write(j);
        }
    }

    @Test
    public void testEmpty() throws IOException, MalformedConfigurationException {
        createFile("test.json", confEmpty);
        ConfJSON c = new ConfJSON("test.json");
        assertEquals(LogLevelType.INFO, c.getLogLevel());
        assertThrows(MalformedConfigurationException.class, () -> c.getAgents());
    }

    @Test
    public void testEmptyAgents() throws IOException, MalformedConfigurationException {
        createFile("test.json", confEmptyAgents);
        ConfJSON c = new ConfJSON("test.json");
        assertEquals(LogLevelType.INFO, c.getLogLevel());
        assertTrue(c.getAgents().isEmpty());
    }

    @Test
    public void testLogDebug() throws IOException {
        createFile("test.json", confLogDebug);
        ConfJSON c = new ConfJSON("test.json");
        assertEquals(LogLevelType.DEBUG, c.getLogLevel());
    }

    @Test
    public void testValidAgent() throws IOException, MalformedConfigurationException {
        // default is INOUT
        createFile("test.json", confSimulatorX);
        ConfJSON c = new ConfJSON("test.json");
        List<AgentConfJSON> agents = c.getAgents();
        assertEquals(1, agents.size());
        AgentConfJSON simConf = agents.get(0);
        assertEquals("SIM1", simConf.getName());
        assertEquals("SimulatorX", simConf.getType());
        assertEquals(InOut.INOUT, simConf.getInOut());
        Set<String> qos = new HashSet<>(Arrays.asList(simConf.getQos().getKeys()));
        assertEquals(4, qos.size());
        assertTrue(qos.contains("truewind_sog"));
        assertTrue(qos.contains("enrich_hdm"));
        assertTrue(qos.contains("rmc_filter"));
        assertTrue(qos.contains("speed_filter"));
    }

    @Test
    public void testValidAgentOutOnly() throws IOException, MalformedConfigurationException {
        // default is INOUT
        createFile("test.json", confSimulatorXOnlyOut);
        ConfJSON c = new ConfJSON("test.json");
        List<AgentConfJSON> agents = c.getAgents();
        assertEquals(1, agents.size());
        AgentConfJSON simConf = agents.get(0);
        assertEquals("SIM1", simConf.getName());
        assertEquals("SimulatorX", simConf.getType());
        assertEquals(InOut.OUT, simConf.getInOut());
        Set<String> qos = new HashSet<>(Arrays.asList(simConf.getQos().getKeys()));
        assertEquals(4, qos.size());
        assertTrue(qos.contains("truewind_sog"));
        assertTrue(qos.contains("enrich_hdm"));
        assertTrue(qos.contains("rmc_filter"));
        assertTrue(qos.contains("speed_filter"));
    }
}