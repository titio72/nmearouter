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

import com.aboni.log.Log;
import com.aboni.log.LogStringBuilder;
import com.aboni.nmea.n2k.N2KMessage;
import com.aboni.nmea.nmea0183.NMEA0183Message;
import com.aboni.nmea.router.NMEAStream;
import com.aboni.nmea.router.OnRouterMessage;
import com.aboni.nmea.router.RouterMessage;
import com.aboni.nmea.router.RouterMessageFactory;
import com.aboni.nmea.router.agent.impl.NMEAAgentImpl;
import com.aboni.nmea.router.message.JSONMessage;
import com.aboni.nmea.router.services.*;
import com.aboni.nmea.router.utils.ThingsFactory;
import com.aboni.utils.TimestampProvider;
import com.aboni.utils.Utils;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AllowSymLinkAliasChecker;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

import javax.inject.Inject;
import java.io.File;

@SuppressWarnings("OverlyCoupledClass")
public class WebInterfaceAgent extends NMEAAgentImpl {

    public static final String WEB_UI_CATEGORY = "WebUI";
    public static final String WEB_UI_START_LOG_TOKEN = "start";
    public static final String WEB_UI_STATS_LOG_TOKEN = "stats";
    private boolean webStarted;
    private Server server;

    private final NMEAStream stream;

    private static class Stats {
        long n2k;
        long n0183;
        long json;
        long lastStatsTime;

        void incrNMEA083() {
            synchronized (this) {
                n0183++;
            }
        }

        void incrN2k() {
            synchronized (this) {
                n2k++;
            }
        }

        void incrJson() {
            synchronized (this) {
                json++;
            }
        }

        void reset(long t) {
            synchronized (this) {
                lastStatsTime = t;
                json = 0;
                n0183 = 0;
                n2k = 0;
            }
        }
    }

    private final Stats stats = new Stats();

    @Inject
    public WebInterfaceAgent(TimestampProvider tp, NMEAStream stream, Log log, RouterMessageFactory messageFactory) {
        super(log, tp, messageFactory, false, true);
        this.stream = stream;
    }

    public static class MyWebSocketServlet extends WebSocketServlet {
        private final NMEAStream stream;
        private final Log log;

        public MyWebSocketServlet(NMEAStream stream, Log log) {
            this.stream = stream;
            this.log = log;
        }

        @Override
        public void configure(WebSocketServletFactory wsFactory) {
            wsFactory.setCreator((servletUpgradeRequest, servletUpgradeResponse) -> new EventSocket(stream, log));
        }
    }

    @Override
    protected final boolean onActivate() {
        synchronized (this) {
            if (!webStarted) {
                getLog().info(LogStringBuilder.start(WEB_UI_CATEGORY).wO("init").toString());
                org.eclipse.jetty.util.log.Logger l = new org.eclipse.jetty.util.log.JavaUtilLog("jetty");
                org.eclipse.jetty.util.log.Log.setLog(l);
                server = new Server(1112);
                ResourceHandler resourceHandler = new ResourceHandler();
                resourceHandler.setWelcomeFiles(new String[]{"index.html"});
                File base = new File("./web");
                try {
                    resourceHandler.setResourceBase(base.getCanonicalPath());
                } catch (Exception e) {
                    getLog().errorForceStacktrace(LogStringBuilder.start(WEB_UI_CATEGORY).wO(WEB_UI_START_LOG_TOKEN).toString(), e);
                    return false;
                }

                ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
                context.setContextPath("/");
                context.addServlet(new ServletHolder(new MyWebSocketServlet(stream, getLog())), "/events");
                context.addAliasCheck(new AllowSymLinkAliasChecker());
                registerServlets(context);

                HandlerList handlers = new HandlerList();
                handlers.addHandler(resourceHandler);
                handlers.addHandler(context);

                server.setHandler(handlers);

                try {
                    server.start();
                    webStarted = true;
                    getLog().info(LogStringBuilder.start(WEB_UI_CATEGORY).wO(WEB_UI_START_LOG_TOKEN).toString());
                } catch (Exception e) {
                    getLog().errorForceStacktrace(LogStringBuilder.start(WEB_UI_CATEGORY).wO(WEB_UI_START_LOG_TOKEN).toString(), e);
                    return false;
                }

                stats.reset(getTimestampProvider().getNow());
            }
            return true;
        }
    }

    private void registerServlets(ServletContextHandler context) {
        context.addServlet(new ServletHolder(new NMEARouterServlet<>(ThingsFactory.getInstance(WindStatsService.class))), "/windanalytics");
        context.addServlet(new ServletHolder(new NMEARouterServlet<>(ThingsFactory.getInstance(AISTargetsService.class))), "/ais");
        context.addServlet(new ServletHolder(new NMEARouterServlet<>(ThingsFactory.getInstance(GPSStatusService.class))), "/gps");
        context.addServlet(new ServletHolder(new NMEARouterServlet<>(ThingsFactory.getInstance(TrackAnalyticsService.class))), "/trackanalytics");
        context.addServlet(new ServletHolder(new NMEARouterServlet<>(ThingsFactory.getInstance(TrackService.class))), "/track");
        context.addServlet(new ServletHolder(new NMEARouterServlet<>(ThingsFactory.getInstance(AgentStatusService.class))), "/agentsj");
        context.addServlet(new ServletHolder(new NMEARouterServlet<>(ThingsFactory.getInstance(ServiceShutdown.class))), "/shutdown");
        context.addServlet(new ServletHolder(new NMEARouterServlet<>(ThingsFactory.getInstance(SimulatorService.class))), "/sim");
        context.addServlet(new ServletHolder(new NMEARouterServlet<>(ThingsFactory.getInstance(MeteoService.class))), "/meteo");
        context.addServlet(new ServletHolder(new NMEARouterServlet<>(ThingsFactory.getInstance(MeteoService2.class))), "/meteo2");
        context.addServlet(new ServletHolder(new NMEARouterServlet<>(ThingsFactory.getInstance(PowerService2.class))), "/power2");
        context.addServlet(new ServletHolder(new NMEARouterServlet<>(ThingsFactory.getInstance(PowerAnalyticsService.class))), "/poweranalysis");
        context.addServlet(new ServletHolder(new NMEARouterServlet<>(ThingsFactory.getInstance(ChangeTripDescService.class))), "/changetripdesc");
        context.addServlet(new ServletHolder(new NMEARouterServlet<>(ThingsFactory.getInstance(DropTripService.class))), "/droptrip");
        context.addServlet(new ServletHolder(new NMEARouterServlet<>(ThingsFactory.getInstance(TrimTripService.class))), "/trimtrip");
        context.addServlet(new ServletHolder(new NMEARouterServlet<>(ThingsFactory.getInstance(TripListService.class))), "/trips");
        context.addServlet(new ServletHolder(new NMEARouterServlet<>(ThingsFactory.getInstance(SpeedService.class))), "/speed");
        context.addServlet(new ServletHolder(new NMEARouterServlet<>(ThingsFactory.getInstance(ServiceDBBackup.class))), "/backup");
        context.addServlet(new ServletHolder(new NMEARouterServlet<>(ThingsFactory.getInstance(AgentFilterService.class))), "/filter");
        context.addServlet(new ServletHolder(new NMEARouterServlet<>(ThingsFactory.getInstance(YearlyAnalyticsService.class))), "/distanalysis");
        context.addServlet(new ServletHolder(new NMEARouterServlet<>(ThingsFactory.getInstance(AutoPilotService.class))), "/ap");
        context.addServlet(new ServletHolder(new NMEARouterServlet<>(ThingsFactory.getInstance(SeatalkAlarmService.class))), "/alarms");
        context.addServlet(new ServletHolder(new NMEARouterServlet<>(ThingsFactory.getInstance(MeteoRollingWindowService.class))), "/meteorolling");
        context.addServlet(new ServletHolder(new NMEARouterServlet<>(ThingsFactory.getInstance(TrackFixerService.class))), "/fixtrack");
    }

    @Override
    public void onDeactivate() {
        synchronized (this) {
            if (webStarted) {
                try {
                    getLog().info(LogStringBuilder.start(WEB_UI_CATEGORY).wO("stop").toString());
                    server.stop();
                } catch (Exception e) {
                    getLog().errorForceStacktrace(LogStringBuilder.start(WEB_UI_CATEGORY).wO("stop").toString(), e);
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
        if (msg.getPayload() instanceof JSONMessage) stats.incrJson();
        else if (msg.getPayload() instanceof NMEA0183Message) stats.incrNMEA083();
        else if (msg.getPayload() instanceof N2KMessage) stats.incrN2k();
        stream.pushMessage(msg,  (Object listener)->{
            EventSocket eventSocket = (EventSocket)listener;
            return !eventSocket.isErr();
        });
    }

    @Override
    public void onTimer() {
        super.onTimer();
        long t = getTimestampProvider().getNow();
        synchronized (stats) {
            if (Utils.isOlderThan(stats.lastStatsTime, t, 29999)) {
                getLog().info(() -> getLogBuilder().wO(WEB_UI_STATS_LOG_TOKEN).
                        wV("NMEA0183", stats.n0183).
                        wV("NMEA2000", stats.n2k).
                        wV("json", stats.json).
                        toString());
                stats.reset(t);
            }
        }
    }
}
