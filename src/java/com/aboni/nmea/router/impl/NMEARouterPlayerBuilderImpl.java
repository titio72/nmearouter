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
import com.aboni.nmea.router.conf.AgentTypes;
import com.aboni.nmea.router.conf.SimpleConf;

import javax.inject.Inject;
import java.util.Properties;

public class NMEARouterPlayerBuilderImpl implements NMEARouterBuilder {

    private final NMEAAgentBuilderJson builder;

    @Inject
    public NMEARouterPlayerBuilderImpl(NMEAAgentBuilderJson builder) {
        if (builder==null) throw new IllegalArgumentException("Agent builder is null");
        this.builder = builder;
    }

    @Override
    public void init(NMEARouter router, Properties props) {
        String playFile = props.getProperty("file");

        NMEAAgent sock = builder.createAgent(new SimpleConf(AgentTypes.TCP, "TCP").
                setAttribute("inout", "OUT").
                setAttribute("port", 1111));
        router.addAgent(sock);
        sock.start();

        NMEAAgent console = builder.createAgent(new SimpleConf(AgentTypes.CONSOLE, "CONSOLE"));
        router.addAgent(console);
        console.start();

        NMEAAgent play = builder.createAgent(new SimpleConf(AgentTypes.PLAYER, "PLAYER").setAttribute("file", playFile));

        router.addAgent(play);
        play.start();
    }
}
