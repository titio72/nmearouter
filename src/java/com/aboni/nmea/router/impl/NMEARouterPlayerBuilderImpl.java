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

import com.aboni.nmea.router.NMEACache;
import com.aboni.nmea.router.NMEARouter;
import com.aboni.nmea.router.NMEARouterBuilder;
import com.aboni.nmea.router.agent.impl.NMEAConsoleTarget;
import com.aboni.nmea.router.agent.impl.NMEAPlayer;
import com.aboni.nmea.router.agent.impl.NMEASocketServer;
import com.aboni.nmea.router.conf.net.NetConf;
import com.aboni.utils.ThingsFactory;
import net.sf.marineapi.nmea.sentence.Sentence;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.util.Properties;

public class NMEARouterPlayerBuilderImpl implements NMEARouterBuilder {

    @Inject
    public NMEARouterPlayerBuilderImpl() {
        // do nothing
    }

    @Override
    public void init(@NotNull NMEARouter router, Properties props) {
        String playFile = props.getProperty("file");

        NMEASocketServer sock = new NMEASocketServer(ThingsFactory.getInstance(NMEACache.class));
        sock.setup("TCP", null, new NetConf(null, 1111, false, true), Sentence::toSentence);
        router.addAgent(sock);
        sock.start();

        NMEAConsoleTarget console = ThingsFactory.getInstance(NMEAConsoleTarget.class);
        console.setup("CONSOLE", null);
        router.addAgent(console);
        console.start();

        NMEAPlayer play = ThingsFactory.getInstance(NMEAPlayer.class);
        play.setup("PLAYER", null);
        play.setFile(playFile);
        router.addAgent(play);
        play.start();
    }


}
