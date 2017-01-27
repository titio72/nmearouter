package com.aboni.nmea.router;

public class NMEARouterProvider {
	
    private NMEARouterProvider() {}
    
    
    private static NMEARouter router;
    
    public static synchronized void setRouter(NMEARouter router) {
    	NMEARouterProvider.router = router;
    }
    
    public static synchronized NMEARouter getRouter() {
        return router;
    }
}
