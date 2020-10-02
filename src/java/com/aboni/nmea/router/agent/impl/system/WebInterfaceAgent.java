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

package com.aboni.nmea.router.agent.impl.system;

import com.aboni.nmea.router.NMEACache;
import com.aboni.nmea.router.NMEAStream;
import com.aboni.nmea.router.agent.impl.NMEAAgentImpl;
import com.aboni.nmea.router.services.EventSocket;
import com.aboni.utils.Log;
import com.aboni.utils.LogStringBuilder;
import com.aboni.utils.ThingsFactory;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.websocket.jsr356.server.ServerContainer;
import org.eclipse.jetty.websocket.jsr356.server.deploy.WebSocketServerContainerInitializer;

import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.validation.constraints.NotNull;

public class WebInterfaceAgent extends NMEAAgentImpl {

    public static final String WEB_UI_CATEGORY = "WebUI";
    private boolean webStarted;
    private Server server;
    private final NMEAStream stream;

    private final Log log;

    @Inject
    public WebInterfaceAgent(@NotNull NMEACache cache, @NotNull NMEAStream stream, @NotNull Log log) {
        super(cache);
        this.log = log;
        this.stream = stream;
        setSourceTarget(false, false);
    }

    @Override
    protected final boolean onActivate() {
        synchronized (this) {
            if (!webStarted) {
                log.info(LogStringBuilder.start(WEB_UI_CATEGORY).withOperation("init").toString());
                org.eclipse.jetty.util.log.Logger l = new org.eclipse.jetty.util.log.JavaUtilLog("jetty");
                org.eclipse.jetty.util.log.Log.setLog(l);
                server = new Server(1112);
                ResourceHandler resourceHandler = new ResourceHandler();
                resourceHandler.setWelcomeFiles(new String[]{"index.html"});
                resourceHandler.setResourceBase("./web");
                ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
                context.setContextPath("/");
                HandlerList handlers = new HandlerList();
                handlers.setHandlers(new Handler[]{resourceHandler, ThingsFactory.getInstance(AbstractHandler.class), context});
                server.setHandler(handlers);
                EventSocket.setNMEAStream(stream);
                WebSocketServerContainerInitializer.configure(context,
                        (ServletContext servletContext, ServerContainer serverContainer) -> serverContainer.addEndpoint(EventSocket.class)
                );
                try {
                    server.start();
                    webStarted = true;
                    log.info(LogStringBuilder.start(WEB_UI_CATEGORY).withOperation("start").toString());
                } catch (Exception e) {
                    log.errorForceStacktrace(LogStringBuilder.start(WEB_UI_CATEGORY).withOperation("start").toString(), e);
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
                    log.info(LogStringBuilder.start(WEB_UI_CATEGORY).withOperation("stop").toString());
                    server.stop();
                } catch (Exception e) {
                    log.errorForceStacktrace(LogStringBuilder.start(WEB_UI_CATEGORY).withOperation("stop").toString(), e);
                }
                server = null;
            }
        }
    }


    @Override
    public String getDescription() {
        return "Web interface - sessions " + EventSocket.getSessions();
    }
}