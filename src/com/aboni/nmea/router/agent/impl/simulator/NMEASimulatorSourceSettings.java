package com.aboni.nmea.router.agent.impl.simulator;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import com.aboni.utils.Constants;
import com.aboni.utils.ServerLog;

public class NMEASimulatorSourceSettings {
	
	public long lastConfModified = 0;
	
	public boolean _vhw = true;
	public boolean _gll = true;
	public boolean _rmc = true;
	public boolean _dpt = true;
	public boolean _dbt = true;
	public boolean _mtw = true;
	public boolean _mta = true;
	public boolean _mbb = true;
	public boolean _mhu = true;
	public boolean _mwv_a = true;
	public boolean _mwv_t = true;
	public boolean _vwr = true;
	public boolean _hdm = true;
	public boolean _hdg = true;
	public boolean _hdt = true;
	public boolean _vtg = true;
	public boolean _xdrDiag = true;
	public boolean _xdrMeteo = true;
	public boolean _xdrMeteoAtm = true;
	public boolean _xdrMeteoHum = true;
	public boolean _xdrMeteoTmp = true;
	public boolean _xdrGYR = true;
	public boolean _autoPilot = false;
	public double _speed = 5.7;
	public double _wSpeed = 9.6;
	public double _wDirection = 270;
	public double _heading = 345;
	public double _temp = 21.5;
	public double _press = 1013;
	public double _hum = 58;

	public NMEASimulatorSourceSettings() {
	}
	
    public void loadConf() {
    	try {
    		File f = new File(Constants.SIM);
    		if (f.exists()) {
    			if (f.lastModified() > lastConfModified) {
    				lastConfModified = f.lastModified();
    				Properties p = new Properties();
    				FileInputStream fi = new FileInputStream(f);
    				p.load(fi);
    				fi.close();
    				readConf(p);
    			}
    		}
    	} catch (Exception e) {
			ServerLog.getLogger().Error("Error reading smulator conf", e);
		}
    }
    
	private void readConf(Properties p) {
		 _vhw   = p.getProperty("simulate.vhw", "0").equals("1");  // water spead and heading
		 _gll   = p.getProperty("simulate.gll", "0").equals("1");  // gps
		 _rmc   = p.getProperty("simulate.rmc", "0").equals("1");  // gps
		 _dpt   = p.getProperty("simulate.dpt", "0").equals("1");  // depth
		 _dbt   = p.getProperty("simulate.dbt", "0").equals("1");  // depth
		 _mtw   = p.getProperty("simulate.mtw", "0").equals("1");  // water temp
		 _mta   = p.getProperty("simulate.mta", "0").equals("1");  // air temp
		 _mbb   = p.getProperty("simulate.mbb", "0").equals("1");  // atm pressure
		 _mhu   = p.getProperty("simulate.mhu", "0").equals("1");  // humidity
		 _mwv_a = p.getProperty("simulate.mwv.apparent", "0").equals("1");  // wind apparent
		 _mwv_t = p.getProperty("simulate.mwv.true", "0").equals("1");  // wind true
		 _vwr   = p.getProperty("simulate.vwr", "0").equals("1");  // relative wind speed and angle (apparent)
		 _hdm   = p.getProperty("simulate.hdm", "0").equals("1"); // magn heading
		 _hdg   = p.getProperty("simulate.hdg", "0").equals("1");  // magn heading + variation/deviation
		 _hdt   = p.getProperty("simulate.hdt", "0").equals("1"); // true heading
		 _vtg   = p.getProperty("simulate.vtg", "0").equals("1");  // cog-sog
		 _autoPilot	= p.getProperty("simulate.autopilot", "0").equals("1");
		 _xdrDiag 		= p.getProperty("simulate.xdr.diag", "0").equals("1");
		 _xdrMeteo 		= p.getProperty("simulate.xdr.meteo", "0").equals("1");
		 _xdrMeteoAtm 	= p.getProperty("simulate.xdr.meteo.atm", "0").equals("1");
		 _xdrMeteoHum 	= p.getProperty("simulate.xdr.meteo.hum", "0").equals("1");
		 _xdrMeteoTmp 	= p.getProperty("simulate.xdr.meteo.tmp", "0").equals("1");
		 _xdrGYR 		= p.getProperty("simulate.xdr.gyro", "0").equals("1");
		
		try { _speed = Double.parseDouble(p.getProperty("simulate.speed", "5.9")); } catch (Exception e) {}
		try { _wSpeed = Double.parseDouble(p.getProperty("simulate.wSpeed", "11.1")); } catch (Exception e) {}
		try { _wDirection = Double.parseDouble(p.getProperty("simulate.wDirection", "270")); } catch (Exception e) {}
		try { _heading = Double.parseDouble(p.getProperty("simulate.heading", "354")); } catch (Exception e) {}
		try { _press = Double.parseDouble(p.getProperty("simulate.pressure", "1013")); } catch (Exception e) {}
		try { _temp = Double.parseDouble(p.getProperty("simulate.temperature", "22.1")); } catch (Exception e) {}
		try { _hum = Double.parseDouble(p.getProperty("simulate.humidity", "48.2")); } catch (Exception e) {}
	}
	    
}