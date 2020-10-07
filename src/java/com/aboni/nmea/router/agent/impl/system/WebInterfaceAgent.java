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
import com.aboni.nmea.router.OnRouterMessage;
import com.aboni.nmea.router.RouterMessage;
import com.aboni.nmea.router.agent.impl.NMEAAgentImpl;
import com.aboni.nmea.router.services.*;
import com.aboni.utils.Log;
import com.aboni.utils.LogStringBuilder;
import com.aboni.utils.ThingsFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;

@SuppressWarnings("OverlyCoupledClass")
public class WebInterfaceAgent extends NMEAAgentImpl {

    public static final String WEB_UI_CATEGORY = "WebUI";
    private boolean webStarted;
    private Server server;

    private final Log log;
    private final NMEAStream stream;

    @Inject
    public WebInterfaceAgent(@NotNull NMEACache cache, @NotNull NMEAStream stream, @NotNull Log log) {
        super(cache);
        this.log = log;
        this.stream = stream;
        setSourceTarget(false, true);
    }

    public static class MyWebSocketServlet extends WebSocketServlet {
        private final NMEAStream stream;

        public MyWebSocketServlet(NMEAStream stream) {
            this.stream = stream;
        }

        @Override
        public void configure(WebSocketServletFactory wsFactory) {
            wsFactory.setCreator((servletUpgradeRequest, servletUpgradeResponse) -> new EventSocket(stream));
        }
    }

    @Override
    protected final boolean onActivate() {
        synchronized (this) {
            if (!webStarted) {
                log.info(LogStringBuilder.start(WEB_UI_CATEGORY).wO("init").toString());
                org.eclipse.jetty.util.log.Logger l = new org.eclipse.jetty.util.log.JavaUtilLog("jetty");
                org.eclipse.jetty.util.log.Log.setLog(l);
                server = new Server(1112);
                ResourceHandler resourceHandler = new ResourceHandler();
                resourceHandler.setWelcomeFiles(new String[]{"index.html"});
                resourceHandler.setResourceBase("./web");

                ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
                context.setContextPath("/");
                context.addServlet(new ServletHolder(new MyWebSocketServlet(stream)), "/events");
                registerServlets(context);

                HandlerList handlers = new HandlerList();
                handlers.addHandler(resourceHandler);
                handlers.addHandler(context);

                server.setHandler(handlers);

                try {
                    server.start();
                    webStarted = true;
                    log.info(LogStringBuilder.start(WEB_UI_CATEGORY).wO("start").toString());
                } catch (Exception e) {
                    log.errorForceStacktrace(LogStringBuilder.start(WEB_UI_CATEGORY).wO("start").toString(), e);
                    return false;
                }
            }
            return true;
        }
    }

    private void registerServlets(ServletContextHandler context) {
        context.addServlet(new ServletHolder(new RouterServlet<>(ThingsFactory.getInstance(WindStatsService.class))), "/windanalytics");
        context.addServlet(new ServletHolder(new RouterServlet<>(ThingsFactory.getInstance(AISTargetsService.class))), "/ais");
        context.addServlet(new ServletHolder(new RouterServlet<>(ThingsFactory.getInstance(GPSStatusService.class))), "/gps");
        context.addServlet(new ServletHolder(new RouterServlet<>(ThingsFactory.getInstance(TrackAnalyticsService.class))), "/trackanalytics");
        context.addServlet(new ServletHolder(new RouterServlet<>(ThingsFactory.getInstance(TrackService.class))), "/track");
        context.addServlet(new ServletHolder(new RouterServlet<>(ThingsFactory.getInstance(AgentStatusService.class))), "/agentsj");
        context.addServlet(new ServletHolder(new RouterServlet<>(ThingsFactory.getInstance(ServiceShutdown.class))), "/shutdown");
        context.addServlet(new ServletHolder(new RouterServlet<>(ThingsFactory.getInstance(SimulatorService.class))), "/sim");
        context.addServlet(new ServletHolder(new RouterServlet<>(ThingsFactory.getInstance(MeteoService.class))), "/meteo");
        context.addServlet(new ServletHolder(new RouterServlet<>(ThingsFactory.getInstance(ChangeTripDescService.class))), "/changetripdesc");
        context.addServlet(new ServletHolder(new RouterServlet<>(ThingsFactory.getInstance(DropTripService.class))), "/droptrip");
        context.addServlet(new ServletHolder(new RouterServlet<>(ThingsFactory.getInstance(TripListService.class))), "/trips");
        context.addServlet(new ServletHolder(new RouterServlet<>(ThingsFactory.getInstance(SpeedService.class))), "/speed");
        context.addServlet(new ServletHolder(new RouterServlet<>(ThingsFactory.getInstance(ServiceDBBackup.class))), "/backup");
        context.addServlet(new ServletHolder(new RouterServlet<>(ThingsFactory.getInstance(AgentFilterService.class))), "/filter");
        context.addServlet(new ServletHolder(new RouterServlet<>(ThingsFactory.getInstance(AutoPilotService.class))), "/auto");
        context.addServlet(new ServletHolder(new RouterServlet<>(ThingsFactory.getInstance(YearlyAnalyticsService.class))), "/distanalysis");
    }

    @Override
    public void onDeactivate() {
        synchronized (this) {
            if (webStarted) {
                try {
                    log.info(LogStringBuilder.start(WEB_UI_CATEGORY).wO("stop").toString());
                    server.stop();
                } catch (Exception e) {
                    log.errorForceStacktrace(LogStringBuilder.start(WEB_UI_CATEGORY).wO("stop").toString(), e);
                }
                server = null;
            }
        }
    }

    @Override
    public String getDescription() {
        return "Web interface - sessions " + EventSocket.getSessions();
    }

    @OnRouterMessage
    public void onSentenceMessage(RouterMessage msg) {
        stream.pushSentence(msg);
    }
}