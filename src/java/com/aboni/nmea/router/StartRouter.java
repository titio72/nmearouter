package com.aboni.nmea.router;

import com.aboni.nmea.router.impl.NMEARouterDefaultBuilderImpl;
import com.aboni.nmea.router.impl.NMEARouterPlayerBuilderImpl;
import com.aboni.nmea.router.services.EventSocket;
import com.aboni.nmea.router.services.WebServiceFactory;
import com.aboni.nmea.router.services.impl.WebInterfaceImpl;
import com.aboni.nmea.sentences.NMEAUtils;
import com.aboni.sensors.HMC5883Calibration;
import com.aboni.sensors.SensorHMC5883;
import com.aboni.utils.ServerLog;
import com.aboni.utils.ThingsFactory;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.websocket.jsr356.server.deploy.WebSocketServerContainerInitializer;

import javax.validation.constraints.NotNull;
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

	private static void consoleOut(String s) {
	    ServerLog.getConsoleOut().println(s);
    }

    public static void main(@NotNull String[] args) {
        Injector injector = Guice.createInjector(new NMEARouterModule());
        ThingsFactory.setInjector(injector);
        int ix;
        if (checkFlag(HELP, args) >= 0) {
            consoleOut("-sensor : sensor monitor\r\n" +
                    "-play : NMEA file to play\r\n" +
                    "-cal : compass calibration\r\n");
        } else if ((ix = checkFlag(PLAY, args)) >= 0) {
            startRouter(injector, new NMEARouterPlayerBuilderImpl(args[ix + 1]));
        } else if (checkFlag(CALIBRATION, args) >= 0) {
            startCalibration();
	    } else {
            startRouter(injector, new NMEARouterDefaultBuilderImpl(Constants.ROUTER_CONF));
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

            ServerContainer webSocketContainer = WebSocketServerContainerInitializer.configureContext(context);
            EventSocket.setNMEAStream(injector.getInstance(NMEAStream.class));
            webSocketContainer.addEndpoint(EventSocket.class);

            server.start();
            server.join();
                
        } catch (Exception e) {
            ServerLog.getLogger().error("Error starting Web server", e);
        }
    }

    private static void startCalibration() {
        SensorHMC5883 m = new SensorHMC5883();
        m.setDefaultSmoothingAlpha(1.0);
        try {
            m.init(1);
            HMC5883Calibration cc = new HMC5883Calibration(m, 15L * 1000L);
            consoleOut("Start");
            cc.start();
            consoleOut("Radius: " + cc.getRadius());
            consoleOut("StdDev: " + cc.getsDev());
            consoleOut("StdDev: " + cc.getsDev());
            consoleOut("C_X:    " + cc.getCalibration()[0]);
            consoleOut("C_Y:    " + cc.getCalibration()[1]);
            consoleOut("C_Z:    " + cc.getCalibration()[2]);
        } catch (Exception e1) {
            ServerLog.getLogger().error("Error during calibration", e1);
        }
    }

    private static void startRouter(Injector injector, NMEARouterBuilder builder) {
        Date date = new Date();
		SimpleDateFormat f = new SimpleDateFormat("yyyy MM dd HH:mm:ss");
		ServerLog.getLogger().info(FILLER);
		ServerLog.getLogger().info(fill("---- NMEARouter "));
		ServerLog.getLogger().info(FILLER);
		ServerLog.getLogger().info(fill("---- Start " + f.format(date) + " "));
		ServerLog.getLogger().info(FILLER);
        NMEAUtils.registerExtraSentences();
        injector.getInstance(NMEAStream.class); // be sure the stream started
    	if (builder.init()!=null) {
    		NMEARouter r = builder.getRouter();
        	r.start();
        }
    }

    private static final String FILLER =  "--------------------------------------------------------------------------------";

	private static String fill(String msg) {
	    return (msg + FILLER).substring(0, FILLER.length());
    }
}
