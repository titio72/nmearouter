package com.aboni.nmea.router;

import com.aboni.nmea.router.agent.NMEAAgentBuilder;
import com.aboni.nmea.router.agent.impl.NMEAAgentBuilderImpl;
import com.aboni.nmea.router.impl.NMEACacheImpl;
import com.aboni.nmea.router.impl.NMEARouterImpl;
import com.aboni.nmea.router.impl.NMEAStreamImpl;
import com.aboni.nmea.router.meteo.Meteo;
import com.aboni.nmea.router.meteo.MeteoReader;
import com.aboni.nmea.router.meteo.impl.DBMeteo;
import com.aboni.nmea.router.meteo.impl.DBMeteoReader;
import com.aboni.nmea.router.meteo.impl.DBMeteoWriter;
import com.aboni.nmea.router.services.WebServiceFactory;
import com.aboni.nmea.router.services.impl.WebServiceFactoryImpl;
import com.aboni.nmea.router.track.*;
import com.aboni.nmea.router.track.impl.*;
import com.aboni.utils.StatsWriter;
import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.name.Names;

public class NMEARouterModule extends AbstractModule {

	@Override
	protected void configure() {
        bind(NMEACache.class).to(NMEACacheImpl.class).in(Singleton.class);
        bind(NMEAStream.class).to(NMEAStreamImpl.class).in(Singleton.class);
        bind(NMEARouter.class).to(NMEARouterImpl.class).in(Singleton.class);
        bind(WebServiceFactory.class).to(WebServiceFactoryImpl.class).in(Singleton.class);
        bind(NMEAAgentBuilder.class).to(NMEAAgentBuilderImpl.class);
        bind(TrackQueryManager.class).to(DBTrackQueryManager.class);
        bind(TripManager.class).to(DBTripManager.class);
        bind(TrackManager.class).to(TrackManagerImpl.class);
        bind(Meteo.class).to(DBMeteo.class);
        bind(MeteoReader.class).to(DBMeteoReader.class);
        bind(TrackReader.class).to(DBTrackReader.class);
        bind(TrackWriter.class).to(DBTrackWriter.class);
        bind(TrackPointBuilder.class).to(TrackPointBuilderImpl.class);
        bind(String.class).annotatedWith(Names.named("TrackTableName")).toInstance("track");
        bind(String.class).annotatedWith(Names.named("TripTableName")).toInstance("trip");
        bind(String.class).annotatedWith(Names.named("MeteoTableName")).toInstance("meteo");
        bind(String.class).annotatedWith(Names.named("AgentTableName")).toInstance("agent");
        bind(StatsWriter.class).annotatedWith(Names.named("MeteoStatsWriter")).to(DBMeteoWriter.class);
    }

}
