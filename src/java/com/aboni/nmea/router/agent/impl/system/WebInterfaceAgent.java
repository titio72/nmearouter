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

import com.aboni.misc.Utils;
import com.aboni.nmea.router.NMEAStream;
import com.aboni.nmea.router.OnRouterMessage;
import com.aboni.nmea.router.RouterMessage;
import com.aboni.nmea.router.TimestampProvider;
import com.aboni.nmea.router.agent.impl.NMEAAgentImpl;
import com.aboni.nmea.router.message.Message;
import com.aboni.nmea.router.nmea0183.NMEA0183Message;
import com.aboni.nmea.router.nmea0183.impl.Message2NMEA0183Impl;
import com.aboni.nmea.router.services.*;
import com.aboni.nmea.sentences.NMEA2JSONb;
import com.aboni.utils.Log;
import com.aboni.utils.LogStringBuilder;
import com.aboni.utils.ThingsFactory;
import net.sf.marineapi.nmea.sentence.Sentence;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;
import org.json.JSONObject;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;

@SuppressWarnings("OverlyCoupledClass")
public class WebInterfaceAgent extends NMEAAgentImpl {

    public static final String WEB_UI_CATEGORY = "WebUI";
    private boolean webStarted;
    private Server server;

    private final Log log;
    private final NMEAStream stream;
    private final NMEA2JSONb jsonConverter;
    private final Message2NMEA0183Impl nmeaConverter;
    private final TimestampProvider timestampProvider;

    private static class Stats {
        long jsonFromMsgToNMEA0183;
        long jsonDirectConversion;
        long jsonFromNMEA1083;
        long lastStatsTime;

        void incrNMEA083() {
            synchronized (this) {
                jsonFromNMEA1083++;
            }
        }

        void incrMsgToNMEA083() {
            synchronized (this) {
                jsonFromMsgToNMEA0183++;
            }
        }

        void incrDirect() {
            synchronized (this) {
                jsonDirectConversion++;
            }
        }

        void reset(long t) {
            synchronized (this) {
                lastStatsTime = t;
                jsonDirectConversion = 0;
                jsonFromMsgToNMEA0183 = 0;
                jsonFromNMEA1083 = 0;
            }
        }
    }

    private final Stats stats = new Stats();

    @Inject
    public WebInterfaceAgent(@NotNull TimestampProvider tp, @NotNull NMEAStream stream, @NotNull Log log) {
        super(log, tp, false, true);
        this.log = log;
        this.stream = stream;
        this.jsonConverter = new NMEA2JSONb();
        this.nmeaConverter = new Message2NMEA0183Impl();
        this.timestampProvider = tp;
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

                stats.reset(timestampProvider.getNow());
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
        AutoJSONMessage m = new AutoJSONMessage(msg);
        stream.pushMessage(m);
    }


    /**
     * Wraps a RouterMessage and automatically convert to JSON all the NMEA sentences (note: N2K are ignored)
     */
    private class AutoJSONMessage implements RouterMessage {

        private final RouterMessage message;
        private JSONObject jsonMessage;

        private AutoJSONMessage(RouterMessage message) {
            this.message = message;
            this.jsonMessage = (message.getPayload() instanceof JSONObject)? (JSONObject) message.getPayload() :null;
        }

        @Override
        public long getTimestamp() {
            return message.getTimestamp();
        }

        @Override
        public String getSource() {
            return message.getSource();
        }

        @Override
        public Object getPayload() {
            return getJSON();
        }

        @Override
        public Message getMessage() {
            return null;
        }

        @Override
        public JSONObject getJSON() {
            if (jsonMessage == null) {
                if (message.getMessage() instanceof NMEA0183Message) {
                    jsonMessage = jsonConverter.convert(((NMEA0183Message) message.getMessage()).getSentence());
                    stats.incrNMEA083();
                } else if (message.getMessage() != null) {
                    try {
                        jsonMessage = message.getMessage().toJSON();
                        stats.incrDirect();
                    } catch (UnsupportedOperationException ignored) {
                        fallbackStrategy();
                    }
                }
            }
            return jsonMessage;
        }

        private void fallbackStrategy() {
            Sentence[] ss = nmeaConverter.convert(message.getMessage());
            try {
                if (ss != null && ss.length != 0) {
                    // just take the first... to be removed anyway
                    jsonMessage = jsonConverter.convert(ss[0]);
                    if (jsonMessage!=null) {
                        stats.incrMsgToNMEA083();
                    }
                }
            } catch (Exception e) {
                getLogBuilder().wO("convert to JSON").wV("sentence", message.getMessage()).error(log, e);
            }
        }
    }

    @Override
    public void onTimer() {
        super.onTimer();
        long t = timestampProvider.getNow();
        synchronized (stats) {
            if (Utils.isOlderThan(stats.lastStatsTime, t, 29999)) {
                getLogBuilder().wO("stats").wV("NMEA0183", stats.jsonFromNMEA1083).
                        wV("direct", stats.jsonDirectConversion).
                        wV("msgToNMEA1083", stats.jsonFromMsgToNMEA0183).info(log);
                stats.reset(t);
            }
        }
    }
}