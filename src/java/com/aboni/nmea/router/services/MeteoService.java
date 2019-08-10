package com.aboni.nmea.router.services;

public class MeteoService extends SampledQueryService {

	private static SampledQueryConf getConf() {
		SampledQueryService.SampledQueryConf conf = new SampledQueryConf();
		conf.setAvgField("v");
		conf.setMaxField("vMax");
		conf.setMinField("vMin");
		conf.setWhere(null);
		conf.setSeriesNameField("type");
		conf.setTable("meteo");
		return conf;
	}

	public MeteoService() {
		super(getConf(), null);
	}
}
