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
import com.aboni.nmea.router.agent.AgentPersistentStatusManager;
import com.aboni.nmea.router.agent.NMEAAgentBuilderJson;
import com.aboni.nmea.router.agent.impl.NMEAAgentBuilderJsonImpl;
import com.aboni.nmea.router.data.DataReader;
import com.aboni.nmea.router.data.SeriesReader;
import com.aboni.nmea.router.data.StatsWriter;
import com.aboni.nmea.router.data.impl.MemoryStatsWriter;
import com.aboni.nmea.router.data.metrics.WindStatsReader;
import com.aboni.nmea.router.data.power.impl.DBPowerStatsWriter;
import com.aboni.nmea.router.data.sampledquery.RangeFinder;
import com.aboni.nmea.router.data.sampledquery.SampledQueryConf;
import com.aboni.nmea.router.data.sampledquery.TimeSeriesReader;
import com.aboni.nmea.router.agent.impl.AgentPersistentStatusManagerImpl;
import com.aboni.nmea.router.data.metrics.impl.*;
import com.aboni.nmea.router.data.power.impl.DBPowerEventWriter;
import com.aboni.nmea.router.data.power.impl.DBPowerReader;
import com.aboni.nmea.router.data.power.impl.DBPowerSeriesReader;
import com.aboni.nmea.router.data.sampledquery.impl.*;
import com.aboni.nmea.router.data.track.*;
import com.aboni.nmea.router.data.track.impl.*;
import com.aboni.nmea.router.filters.JSONFilterParser;
import com.aboni.nmea.router.filters.impl.JSONFilterParserImpl;
import com.aboni.nmea.router.impl.*;
import com.aboni.nmea.n2k.can.HL340USBSerialCANReader;
import com.aboni.nmea.n2k.can.SerialCANReader;
import com.aboni.nmea.router.services.QueryFactory;
import com.aboni.nmea.router.services.impl.QueryFactoryImpl;
import com.aboni.log.Log;
import com.aboni.log.LogAdmin;
import com.aboni.nmea.router.utils.RouterLog;
import com.aboni.nmea.router.utils.db.DBEventWriter;
import com.aboni.utils.DefaultTimestampProvider;
import com.aboni.utils.TimestampProvider;
import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.name.Names;

@SuppressWarnings("OverlyCoupledClass")
public class NMEARouterModule extends AbstractModule {

    private final LogAdmin logger = new RouterLog();

    @Override
    protected void configure() {
        bind(LogAdmin.class).toInstance(logger);
        bind(Log.class).toInstance(logger);
        bind(TimestampProvider.class).to(DefaultTimestampProvider.class).in(Singleton.class);
        bind(RouterMessageFactory.class).to(RouterMessageFactoryImpl.class);
        bind(AgentPersistentStatusManager.class).to(AgentPersistentStatusManagerImpl.class).in(Singleton.class);
        bind(NMEARouterBuilder.class).annotatedWith(Names.named("router")).to(NMEARouterDefaultBuilderImpl.class);
        bind(NMEARouterBuilder.class).annotatedWith(Names.named("play")).to(NMEARouterPlayerBuilderImpl.class);
        bind(NMEACache.class).to(NMEACacheImpl.class).in(Singleton.class);
        bind(NMEAStream.class).to(NMEAStreamImpl.class);
        bind(NMEARouter.class).to(NMEARouterImpl.class).in(Singleton.class);
        bind(NMEAAgentBuilderJson.class).to(NMEAAgentBuilderJsonImpl.class);
        bind(MonthYearTrackStats.class).to(DBMonthYearTrackStats.class);
        bind(TripManagerX.class).to(TripManagerXImpl.class).in(Singleton.class);
        bind(TrackManager.class).to(TrackManagerImpl.class);
        bind(SeriesReader.class).annotatedWith(Names.named(Constants.TAG_METEO)).to(DBMetricSeriesReader.class);
        bind(SeriesReader.class).annotatedWith(Names.named(Constants.TAG_POWER)).to(DBPowerSeriesReader.class);
        bind(SeriesReader.class).to(DBMetricSeriesReader.class);
        bind(TrackPointBuilder.class).to(TrackPointBuilderImpl.class);
        bind(TrackDumperFactory.class).to(TrackDumperFactoryImpl.class);
        bind(DataReader.class).annotatedWith(Names.named(Constants.TAG_METEO)).to(DBMetricReader.class);
        bind(DataReader.class).annotatedWith(Names.named(Constants.TAG_POWER)).to(DBPowerReader.class);
        bind(TrackReader.class).to(DBTrackReader.class);
        bind(String.class).annotatedWith(Names.named(Constants.TAG_TRACK)).toInstance("track");
        bind(String.class).annotatedWith(Names.named(Constants.TAG_TRIP)).toInstance("trip");
        bind(String.class).annotatedWith(Names.named(Constants.TAG_METEO)).toInstance("meteo");
        bind(String.class).annotatedWith(Names.named(Constants.TAG_AGENT)).toInstance("agent");
        bind(String.class).annotatedWith(Names.named(Constants.TAG_POWER)).toInstance("power");
        bind(StatsWriter.class).annotatedWith(Names.named(Constants.TAG_METEO_MONITOR)).to(MemoryStatsWriter.class);
        bind(StatsWriter.class).annotatedWith(Names.named(Constants.TAG_METEO)).to(DBMetricStatsWriter.class);
        bind(StatsWriter.class).annotatedWith(Names.named(Constants.TAG_POWER)).to(DBPowerStatsWriter.class);
        bind(DBEventWriter.class).annotatedWith(Names.named(Constants.TAG_METEO)).to(DBMetricEventWriter.class);
        bind(DBEventWriter.class).annotatedWith(Names.named(Constants.TAG_POWER)).to(DBPowerEventWriter.class);
        bind(SampledQueryConf.class).annotatedWith(Names.named(Constants.TAG_SPEED)).to(SampledQueryConfSpeed.class);
        bind(SampledQueryConf.class).annotatedWith(Names.named(Constants.TAG_METEO)).to(SampledQueryConfMeteo.class);
        bind(SampledQueryConf.class).annotatedWith(Names.named(Constants.TAG_POWER)).to(SampledQueryConfPower.class);
        bind(RangeFinder.class).to(DBRangeFinder.class);
        bind(TimeSeriesReader.class).to(DBTimeSeriesReader.class);
        bind(QueryFactory.class).to(QueryFactoryImpl.class);
        bind(JSONFilterParser.class).to(JSONFilterParserImpl.class);
        bind(WindStatsReader.class).to(WindStatsReaderImpl.class);
        bind(SerialCANReader.class).to(HL340USBSerialCANReader.class);
        bind(DeviationManager.class).to(DeviationManagerImpl.class).in(Singleton.class);
    }
}
