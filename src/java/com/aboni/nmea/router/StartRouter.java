package com.aboni.nmea.router;

import com.aboni.nmea.router.impl.NMEARouterDefaultBuilderImpl;
import com.aboni.nmea.router.impl.NMEARouterPlayerBuilderImpl;
import com.aboni.nmea.router.services.EventSocket;
import com.aboni.nmea.router.services.WebServiceFactory;
import com.aboni.nmea.router.services.impl.WebInterfaceImpl;
import com.aboni.nmea.sentences.NMEAUtils;
import com.aboni.sensors.DoCalibration;
import com.aboni.utils.Constants;
import com.aboni.utils.ServerLog;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.websocket.jsr356.server.deploy.WebSocketServerContainerInitializer;

import javax.websocket.server.ServerContainer;
import java.text.SimpleDateFormat;
import java.util.Date;

public class StartRouter {

    private static final String CALIBRATION = "-cal";
    private static final String PLAY = "-play";
    private static final String HELP = "-help";
    
	private static int checkFlag(String flag, String[] args) {
        if (args!=null) {
            for (int i = 0; i<args.length; i++) {
                if (flag.equals(args[i])) return i;
            }
        }
        return -1;
	}
    
	public static void main(String[] args) {
		Injector injector = Guice.createInjector(new NMEARouterModule());
		
		int ix;
        if (checkFlag(HELP, args)>=0) {
            System.out.println("-web : activate web interface\r\n" + 
                    "-sensor : sensor monitor\r\n" +
                    "-play : NMEA file to play\r\n" +
                    "-cal : compass calibration\r\n");
        } else if ((ix = checkFlag(PLAY, args))>=0) {
        	startRouter(injector, new NMEARouterPlayerBuilderImpl(injector, args[ix + 1]));
       } else if (checkFlag(CALIBRATION, args)>=0) {
            startCalibration(args);
	    } else {
            startRouter(injector, new NMEARouterDefaultBuilderImpl(injector, Constants.ROUTER_CONF));
    		startWebInterface(injector);
	    }
	}

	private static void startWebInterface(Injector injector) {
        try {
            org.eclipse.jetty.util.log.Logger l;
            l = new org.eclipse.jetty.util.log.JavaUtilLog("jetty");

            Log.setLog(l);

            Server server = new Server(1112);

            ResourceHandler resourceHandler = new ResourceHandler();
            resourceHandler.setWelcomeFiles(new String[]{ "index.html" });
            resourceHandler.setResourceBase("./web");

            // Setup the basic application "context" for this application at "/"
            // This is also known as the handler tree (in jetty speak)
            ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
            context.setContextPath("/");


            HandlerList handlers = new HandlerList();
            WebServiceFactory svcFactory = injector.getInstance(WebServiceFactory.class);
            handlers.setHandlers(new Handler[] { resourceHandler, new WebInterfaceImpl(svcFactory), context });
            server.setHandler(handlers);

            ServerContainer wscontainer = WebSocketServerContainerInitializer.configureContext(context);
            EventSocket.setNMEAStream(injector.getInstance(NMEAStream.class));
            wscontainer.addEndpoint(EventSocket.class);

            server.start();
            server.dump(System.err);
            server.join();
                
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void startCalibration(String[] args) {
        DoCalibration.main(args);
    }

    private static void startRouter(Injector injector, NMEARouterBuilder builder) {
        System.out.println("Start");
		Date date = new Date();
		SimpleDateFormat f = new SimpleDateFormat("yyyy MM dd HH:mm:ss");
		ServerLog.getLogger().info("--------------------------------------------------------------------------------");
		ServerLog.getLogger().info("---- NMEARouter ----------------------------------------------------------------");
		ServerLog.getLogger().info("--------------------------------------------------------------------------------");
		ServerLog.getLogger().info("---- Start " + f.format(date) + "--------------------------------------------------");
		ServerLog.getLogger().info("--------------------------------------------------------------------------------");
        NMEAUtils.registerExtraSentences();
        injector.getInstance(NMEAStream.class); // be sure the stream started
    	if (builder.init()!=null) {
    		NMEARouter r = builder.getRouter();
        	r.start();
        }
    }
}
