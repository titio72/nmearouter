package com.aboni.nmea.router.agent.impl.system;

import com.aboni.nmea.router.NMEACache;
import com.aboni.nmea.router.NMEAStream;
import com.aboni.nmea.router.agent.impl.NMEAAgentImpl;
import com.aboni.nmea.router.services.EventSocket;
import com.aboni.utils.ServerLog;
import com.aboni.utils.ThingsFactory;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.websocket.jsr356.server.deploy.WebSocketServerContainerInitializer;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.websocket.server.ServerContainer;

public class WebInterfaceAgent extends NMEAAgentImpl {

    private boolean webStarted;
    private Server server;

    @Inject
    public WebInterfaceAgent(@NotNull NMEACache cache) {
        super(cache);
        setSourceTarget(false, false);
    }

    @Override
    protected final boolean onActivate() {
        synchronized (this) {
            if (!webStarted) {
                getLogger().info("Starting web interface");
                org.eclipse.jetty.util.log.Logger l = new org.eclipse.jetty.util.log.JavaUtilLog("jetty");
                Log.setLog(l);
                server = new Server(1112);
                ResourceHandler resourceHandler = new ResourceHandler();
                resourceHandler.setWelcomeFiles(new String[]{"index.html"});
                resourceHandler.setResourceBase("./web");
                ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
                context.setContextPath("/");
                HandlerList handlers = new HandlerList();
                handlers.setHandlers(new Handler[]{resourceHandler, ThingsFactory.getInstance(AbstractHandler.class), context});
                server.setHandler(handlers);
                try {
                    ServerContainer webSocketContainer = WebSocketServerContainerInitializer.configureContext(context);
                    EventSocket.setNMEAStream(ThingsFactory.getInstance(NMEAStream.class));
                    webSocketContainer.addEndpoint(EventSocket.class);
                    server.start();
                    webStarted = true;
                    getLogger().info("Started web interface");
                } catch (Exception e) {
                    ServerLog.getLogger().errorForceStacktrace("Cannot start web interface", e);
                    return false;
                }
            }
            return true;
        }
    }

    @Override
    public void onDeactivate() {
        synchronized (this) {
            if (webStarted) {
                try {
                    getLogger().info("Stopping web interface");
                    server.stop();
                    getLogger().info("Stopped web interface");
                } catch (Exception e) {
                    ServerLog.getLogger().errorForceStacktrace("Error stopping web interface", e);
                }
                server = null;
            }
        }
    }


    @Override
    public String getDescription() {
        return "Web interface";
    }
}