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
import com.aboni.nmea.router.services.WebServiceFactory;
import com.aboni.nmea.router.services.impl.WebServiceFactoryImpl;
import com.aboni.nmea.router.track.TrackPointBuilder;
import com.aboni.nmea.router.track.TrackQueryManager;
import com.aboni.nmea.router.track.TrackReader;
import com.aboni.nmea.router.track.TripManager;
import com.aboni.nmea.router.track.impl.DBTrackQueryManager;
import com.aboni.nmea.router.track.impl.DBTrackReader;
import com.aboni.nmea.router.track.impl.DBTripManager;
import com.aboni.nmea.router.track.impl.TrackPointBuilderImpl;
import com.google.inject.AbstractModule;
import com.google.inject.Singleton;

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
        bind(Meteo.class).to(DBMeteo.class);
        bind(MeteoReader.class).to(DBMeteoReader.class);
        bind(TrackReader.class).to(DBTrackReader.class);
        bind(TrackPointBuilder.class).to(TrackPointBuilderImpl.class);
    }

}
