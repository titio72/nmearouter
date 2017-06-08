package com.aboni.nmea.router.services;

public interface WebServiceFactory {
	WebService getService(String target);
}