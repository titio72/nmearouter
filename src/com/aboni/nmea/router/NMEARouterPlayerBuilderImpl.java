package com.aboni.nmea.router;

import com.aboni.nmea.router.agent.NMEAAgent;
import com.aboni.nmea.router.agent.NMEAConsoleTarget;
import com.aboni.nmea.router.agent.NMEAPlayer;
import com.aboni.nmea.router.agent.NMEASocketTarget;
import com.aboni.nmea.router.impl.NMEARouterImpl;

public class NMEARouterPlayerBuilderImpl implements NMEARouterBuilder {

    private NMEARouter router;
    private String playFile;
    
    public NMEARouterPlayerBuilderImpl(String playFile) {
    	this.playFile = playFile;
    }
    

	@Override
	public NMEARouter getRouter() {
		return router;
	}

	@Override
	public NMEARouterBuilder init() {
        router = new NMEARouterImpl();
        
        NMEAAgent sock = new NMEASocketTarget("TCP", 1111, null);
        router.addAgent(sock);
        sock.start();

        NMEAConsoleTarget console = new NMEAConsoleTarget("CONSOLE", null);
        router.addAgent(console);
        console.start();
        
        NMEAPlayer play = new NMEAPlayer("PLAYER", null);
        play.setFile(playFile);
        router.addAgent(play);
        play.start();
        
        return this;
	}


}
