package com.aboni.nmea.router;

import com.aboni.nmea.router.agent.NMEAAgentBuilder;
import com.aboni.nmea.router.agent.impl.NMEAAgentBuilderImpl;
import com.aboni.nmea.router.impl.NMEACacheImpl;
import com.aboni.nmea.router.impl.NMEARouterImpl;
import com.aboni.nmea.router.impl.NMEAStreamImpl;
import com.aboni.nmea.router.services.WebServiceFactory;
import com.aboni.nmea.router.services.impl.WebServiceFactoryImpl;
import com.google.inject.AbstractModule;
import com.google.inject.Singleton;

public class NMEARouterModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(NMEACache.class).to(NMEACacheImpl.class).in(Singleton.class);
		bind(NMEAStream.class).to(NMEAStreamImpl.class).in(Singleton.class);
		bind(NMEARouter.class).to(NMEARouterImpl.class).in(Singleton.class);
		bind(WebServiceFactory.class).to(WebServiceFactoryImpl.class);
		bind(NMEAAgentBuilder.class).to(NMEAAgentBuilderImpl.class);
	}

}
