package com.aboni.nmea.router;

public class NMEARouterProvider {

    private NMEARouterProvider() {}
    
    private static NMEARouter router;
    
    static void setRouter(NMEARouter router) {
        NMEARouterProvider.router = router;
    }
    
    public static NMEARouter getRouter() {
        return router;
    }
}
