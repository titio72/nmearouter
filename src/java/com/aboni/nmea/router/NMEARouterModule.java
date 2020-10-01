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

package com.aboni.nmea.router;

import com.aboni.geo.DeviationManager;
import com.aboni.geo.impl.DeviationManagerImpl;
import com.aboni.nmea.router.agent.AgentStatusManager;
import com.aboni.nmea.router.agent.NMEAAgentBuilderJson;
import com.aboni.nmea.router.agent.impl.AgentStatusManagerImpl;
import com.aboni.nmea.router.agent.impl.NMEAAgentBuilderJsonImpl;
import com.aboni.nmea.router.data.meteo.Meteo;
import com.aboni.nmea.router.data.meteo.MeteoReader;
import com.aboni.nmea.router.data.meteo.WindStatsReader;
import com.aboni.nmea.router.data.meteo.impl.DBMeteo;
import com.aboni.nmea.router.data.meteo.impl.DBMeteoReader;
import com.aboni.nmea.router.data.meteo.impl.DBMeteoWriter;
import com.aboni.nmea.router.data.meteo.impl.DBWindStatsReader;
import com.aboni.nmea.router.data.sampledquery.RangeFinder;
import com.aboni.nmea.router.data.sampledquery.SampledQueryConf;
import com.aboni.nmea.router.data.sampledquery.TimeSeriesReader;
import com.aboni.nmea.router.data.sampledquery.impl.DBRangeFinder;
import com.aboni.nmea.router.data.sampledquery.impl.DBTimeSeriesReader;
import com.aboni.nmea.router.data.sampledquery.impl.SampledQueryConfMeteo;
import com.aboni.nmea.router.data.sampledquery.impl.SampledQueryConfSpeed;
import com.aboni.nmea.router.data.track.*;
import com.aboni.nmea.router.data.track.impl.*;
import com.aboni.nmea.router.filters.FilterSetSerializer;
import com.aboni.nmea.router.filters.impl.JSONFilterSetSerializer;
import com.aboni.nmea.router.impl.*;
import com.aboni.nmea.router.n2k.N2KFastCache;
import com.aboni.nmea.router.n2k.N2KMessage2NMEA0183;
import com.aboni.nmea.router.n2k.N2KMessageParser;
import com.aboni.nmea.router.n2k.N2KStream;
import com.aboni.nmea.router.n2k.can.HL340USBSerialCANReader;
import com.aboni.nmea.router.n2k.can.SerialCANReader;
import com.aboni.nmea.router.n2k.impl.N2KFastCacheImpl;
import com.aboni.nmea.router.n2k.impl.N2KMessage2NMEA0183Impl;
import com.aboni.nmea.router.n2k.impl.N2KMessageParserImpl;
import com.aboni.nmea.router.n2k.impl.N2KStreamImpl;
import com.aboni.nmea.router.n2k.messages.N2KMessageFactory;
import com.aboni.nmea.router.n2k.messages.impl.N2KMessageFactoryImpl;
import com.aboni.nmea.router.services.QueryFactory;
import com.aboni.nmea.router.services.WebServiceFactory;
import com.aboni.nmea.router.services.impl.QueryFactoryImpl;
import com.aboni.nmea.router.services.impl.WebHandler;
import com.aboni.nmea.router.services.impl.WebServiceFactoryImpl;
import com.aboni.utils.StatsWriter;
import com.aboni.utils.db.DBEventWriter;
import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.name.Names;
import org.eclipse.jetty.server.handler.AbstractHandler;

@SuppressWarnings("OverlyCoupledClass")
public class NMEARouterModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(RouterMessageFactory.class).to(RouterMessageFactoryImpl.class);
        bind(AgentStatusManager.class).to(AgentStatusManagerImpl.class).in(Singleton.class);
        bind(NMEARouterBuilder.class).annotatedWith(Names.named("router")).to(NMEARouterDefaultBuilderImpl.class);
        bind(NMEARouterBuilder.class).annotatedWith(Names.named("play")).to(NMEARouterPlayerBuilderImpl.class);
        bind(AbstractHandler.class).to(WebHandler.class);
        bind(NMEACache.class).to(NMEACacheImpl.class).in(Singleton.class);
        bind(TimestampProvider.class).to(DefaultTimestampProvider.class).in(Singleton.class);
        bind(NMEAStream.class).to(NMEAStreamImpl.class).in(Singleton.class);
        bind(NMEARouter.class).to(NMEARouterImpl.class).in(Singleton.class);
        bind(WebServiceFactory.class).to(WebServiceFactoryImpl.class).in(Singleton.class);
        bind(NMEAAgentBuilderJson.class).to(NMEAAgentBuilderJsonImpl.class);
        bind(TrackQueryManager.class).to(DBTrackQueryManager.class);
        bind(TripManagerX.class).to(TripManagerXImpl.class);
        bind(TrackManager.class).to(TrackManagerImpl.class);
        bind(Meteo.class).to(DBMeteo.class);
        bind(TrackPointBuilder.class).to(TrackPointBuilderImpl.class);
        bind(TrackDumperFactory.class).to(TrackDumperFactoryImpl.class);
        bind(MeteoReader.class).to(DBMeteoReader.class);
        bind(TrackReader.class).to(DBTrackReader.class);
        bind(DBEventWriter.class).annotatedWith(Names.named(Constants.TAG_TRACK)).to(DBTrackEventWriter.class);
        bind(DBEventWriter.class).annotatedWith(Names.named(Constants.TAG_TRIP)).to(DBTripEventWriter.class);
        bind(String.class).annotatedWith(Names.named(Constants.TAG_TRACK)).toInstance("track");
        bind(String.class).annotatedWith(Names.named(Constants.TAG_TRIP)).toInstance("trip");
        bind(String.class).annotatedWith(Names.named(Constants.TAG_METEO)).toInstance("meteo");
        bind(String.class).annotatedWith(Names.named(Constants.TAG_AGENT)).toInstance("agent");
        bind(StatsWriter.class).annotatedWith(Names.named(Constants.TAG_METEO)).to(DBMeteoWriter.class);
        bind(SampledQueryConf.class).annotatedWith(Names.named(Constants.TAG_SPEED)).to(SampledQueryConfSpeed.class);
        bind(SampledQueryConf.class).annotatedWith(Names.named(Constants.TAG_METEO)).to(SampledQueryConfMeteo.class);
        bind(RangeFinder.class).to(DBRangeFinder.class);
        bind(TimeSeriesReader.class).to(DBTimeSeriesReader.class);
        bind(QueryFactory.class).to(QueryFactoryImpl.class);
        bind(FilterSetSerializer.class).annotatedWith(Names.named(Constants.TAG_JSON)).to(JSONFilterSetSerializer.class);
        bind(WindStatsReader.class).to(DBWindStatsReader.class);
        bind(N2KMessageParser.class).to(N2KMessageParserImpl.class);
        bind(N2KMessage2NMEA0183.class).to(N2KMessage2NMEA0183Impl.class);
        bind(N2KFastCache.class).to(N2KFastCacheImpl.class);
        bind(N2KStream.class).to(N2KStreamImpl.class);
        bind(N2KMessageFactory.class).to(N2KMessageFactoryImpl.class).in(Singleton.class);
        bind(SerialCANReader.class).to(HL340USBSerialCANReader.class);
        bind(DeviationManager.class).to(DeviationManagerImpl.class).in(Singleton.class);

    }
}
