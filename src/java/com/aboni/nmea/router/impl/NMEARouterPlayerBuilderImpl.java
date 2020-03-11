package com.aboni.nmea.router.impl;

import com.aboni.nmea.router.NMEACache;
import com.aboni.nmea.router.NMEARouter;
import com.aboni.nmea.router.NMEARouterBuilder;
import com.aboni.nmea.router.agent.impl.NMEAConsoleTarget;
import com.aboni.nmea.router.agent.impl.NMEAPlayer;
import com.aboni.nmea.router.agent.impl.NMEASocketServer;
import com.aboni.nmea.router.conf.net.NetConf;
import com.aboni.utils.ThingsFactory;

public class NMEARouterPlayerBuilderImpl implements NMEARouterBuilder {

    private NMEARouter router;
    private final String playFile;

    public NMEARouterPlayerBuilderImpl(String playFile) {
        this.playFile = playFile;
    }


    @Override
    public NMEARouter getRouter() {
        return router;
    }

    @Override
	public NMEARouterBuilder init() {
        router = ThingsFactory.getInstance(NMEARouter.class);

        NMEASocketServer sock = new NMEASocketServer(ThingsFactory.getInstance(NMEACache.class));
        sock.setup("TCP", null, new NetConf(null, 1111, false, true));
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

        return this;
    }


}
