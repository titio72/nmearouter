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

package com.aboni.nmea.router.impl;

import com.aboni.nmea.router.NMEARouter;
import com.aboni.nmea.router.NMEARouterBuilder;
import com.aboni.nmea.router.agent.NMEAAgent;
import com.aboni.nmea.router.agent.NMEAAgentBuilderJson;
import com.aboni.nmea.router.conf.AgentConfJSON;
import com.aboni.nmea.router.conf.AgentTypes;
import com.aboni.nmea.router.conf.QOS;
import com.aboni.utils.ThingsFactory;
import org.json.JSONObject;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.util.Properties;

public class NMEARouterPlayerBuilderImpl implements NMEARouterBuilder {

    private static class SimpleConf implements AgentConfJSON {

        private final String name;
        private final String type;
        private final JSONObject cfg = new JSONObject();

        private SimpleConf(String type, String name) {
            this.type = type;
            this.name = name;
        }

        SimpleConf setAttribute(String key, int value) {
            cfg.put(key, value);
            return this;
        }

        SimpleConf setAttribute(String key, String value) {
            cfg.put(key, value);
            return this;
        }


        @Override
        public String getType() {
            return type;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public QOS getQos() {
            return null;
        }

        @Override
        public JSONObject getConfiguration() {
            return null;
        }
    }

    @Inject
    public NMEARouterPlayerBuilderImpl() {
        // do nothing
    }

    @Override
    public void init(@NotNull NMEARouter router, Properties props) {
        String playFile = props.getProperty("file");
        NMEAAgentBuilderJson builderJson = ThingsFactory.getInstance(NMEAAgentBuilderJson.class);

        NMEAAgent sock = builderJson.createAgent(new SimpleConf(AgentTypes.TCP, "TCP").
                setAttribute("inout", "OUT").
                setAttribute("port", 1111));
        router.addAgent(sock);
        sock.start();

        NMEAAgent console = builderJson.createAgent(new SimpleConf(AgentTypes.CONSOLE, "CONSOLE"));
        router.addAgent(console);
        console.start();

        NMEAAgent play = builderJson.createAgent(new SimpleConf(AgentTypes.PLAYER, "PLAYER").setAttribute("file", playFile));

        router.addAgent(play);
        play.start();
    }
}
