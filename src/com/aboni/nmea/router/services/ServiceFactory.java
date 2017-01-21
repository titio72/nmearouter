package com.aboni.nmea.router.services;

public class ServiceFactory {

	private static ServiceFactory instance = new ServiceFactory();
	
	
	
	private ServiceFactory() {
		
		
		
	}
	
	public static ServiceFactory etInstance() {
		return instance;
	}
	
	

}
