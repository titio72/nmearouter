package com.aboni.nmea.router.agent.impl.simulator;

import com.aboni.utils.Constants;
import com.aboni.utils.ServerLog;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

public class NMEASimulatorSourceSettings {

	private static final String YES = "1";

	private final String confFile;

	private long lastConfModified = 0;

	private boolean vhw = true;
	private boolean vlw = true;
	private boolean gll = true;

	private boolean rmc = true;
	private boolean gpsOut = false;
	private boolean dpt = true;
	private boolean dbt = true;
	private boolean mtw = true;
	private boolean mta = true;
	private boolean mbb = true;
	private boolean mhu = true;
	private boolean mda = true;
	private boolean mwva = true;
	private boolean mwvt = true;
	private boolean vwr = true;
	private boolean hdm = true;
	private boolean hdg = true;
	private boolean hdt = true;
	private boolean vtg = true;
	private boolean rsa = true;
	private boolean xdrDiag = true;
	private boolean xdrMeteo = true;
	private boolean xdrMeteoAtm = true;
	private boolean xdrMeteoHum = true;
	private boolean xdrMeteoTmp = true;
	private boolean xdrGYR = true;
	private boolean autoPilot = false;
	private double rudder = 6.0;
	private double speed = 5.7;
	private double wSpeed = 9.6;
	private double wDirection = 270;
	private double heading = 345;
	private double temp = 21.5;
	private double press = 1013;
	private double hum = 58;
	private double polarCoeff = 0.85;
	private double depth = 8.0;
	private double depthOffset = 0.0;
	private double depthRange = 2.0;
	private String polars = "dufour40.csv";

    private boolean splitWind = false;

	private boolean usePolars;
    private boolean windSpeedInMS;

	public boolean isVhw() {
		return vhw;
	}

	public void setVhw(boolean vhw) {
		this.vhw = vhw;
	}

	public boolean isVlw() {
		return vlw;
	}

	public void setVlw(boolean vlw) {
		this.vlw = vlw;
	}

	public boolean isGll() {
		return gll;
	}

	public void setGll(boolean gll) {
		this.gll = gll;
	}

	public boolean isRmc() {
		return rmc;
	}

	public void setRmc(boolean rmc) {
		this.rmc = rmc;
	}

	public boolean isGpsOut() {
		return gpsOut;
	}

	public void setGpsOut(boolean gpsOut) {
		this.gpsOut = gpsOut;
	}

	public boolean isDpt() {
		return dpt;
	}

	public void setDpt(boolean dpt) {
		this.dpt = dpt;
	}

	public boolean isDbt() {
		return dbt;
	}

	public void setDbt(boolean dbt) {
		this.dbt = dbt;
	}

	public boolean isMtw() {
		return mtw;
	}

	public void setMtw(boolean mtw) {
		this.mtw = mtw;
	}

	public boolean isMta() {
		return mta;
	}

	public void setMta(boolean mta) {
		this.mta = mta;
	}

	public boolean isMbb() {
		return mbb;
	}

	public void setMbb(boolean mbb) {
		this.mbb = mbb;
	}

	public boolean isMhu() {
		return mhu;
	}

	public void setMhu(boolean mhu) {
		this.mhu = mhu;
	}

	public boolean isMda() {
		return mda;
	}

	public void setMda(boolean mda) {
		this.mda = mda;
	}

	public boolean isMwvA() {
		return mwva;
	}

	public void setMwva(boolean mwva) {
		this.mwva = mwva;
	}

	public boolean isMwvT() {
		return mwvt;
	}

	public void setMwvt(boolean mwvt) {
		this.mwvt = mwvt;
	}

	public boolean isVwr() {
		return vwr;
	}

	public void setVwr(boolean vwr) {
		this.vwr = vwr;
	}

	public boolean isHdm() {
		return hdm;
	}

	public void setHdm(boolean hdm) {
		this.hdm = hdm;
	}

	public boolean isHdg() {
		return hdg;
	}

	public void setHdg(boolean hdg) {
		this.hdg = hdg;
	}

	public boolean isHdt() {
		return hdt;
	}

	public void setHdt(boolean hdt) {
		this.hdt = hdt;
	}

	public boolean isVtg() {
		return vtg;
	}

	public void setVtg(boolean vtg) {
		this.vtg = vtg;
	}

	public boolean isRsa() {
		return rsa;
	}

	public void setRsa(boolean rsa) {
		this.rsa = rsa;
	}

	public boolean isXdrDiag() {
		return xdrDiag;
	}

	public void setXdrDiag(boolean xdrDiag) {
		this.xdrDiag = xdrDiag;
	}

	public boolean isXdrMeteo() {
		return xdrMeteo;
	}

	public void setXdrMeteo(boolean xdrMeteo) {
		this.xdrMeteo = xdrMeteo;
	}

	public boolean isXdrMeteoAtm() {
		return xdrMeteoAtm;
	}

	public void setXdrMeteoAtm(boolean xdrMeteoAtm) {
		this.xdrMeteoAtm = xdrMeteoAtm;
	}

	public boolean isXdrMeteoHum() {
		return xdrMeteoHum;
	}

	public void setXdrMeteoHum(boolean xdrMeteoHum) {
		this.xdrMeteoHum = xdrMeteoHum;
	}

	public boolean isXdrMeteoTmp() {
		return xdrMeteoTmp;
	}

	public void setXdrMeteoTmp(boolean xdrMeteoTmp) {
		this.xdrMeteoTmp = xdrMeteoTmp;
	}

	public boolean isXdrGYR() {
		return xdrGYR;
	}

	public void setXdrGYR(boolean xdrGYR) {
		this.xdrGYR = xdrGYR;
	}

	public boolean isAutoPilot() {
		return autoPilot;
	}

	public void setAutoPilot(boolean autoPilot) {
		this.autoPilot = autoPilot;
	}

	public double getRudder() {
		return rudder;
	}

	public void setRudder(double rudder) {
		this.rudder = rudder;
	}

	public double getSpeed() {
		return speed;
	}

	public void setSpeed(double speed) {
		this.speed = speed;
	}

	public double getwSpeed() {
		return wSpeed;
	}

	public void setwSpeed(double wSpeed) {
		this.wSpeed = wSpeed;
	}

	public double getwDirection() {
		return wDirection;
	}

	public void setwDirection(double wDirection) {
		this.wDirection = wDirection;
	}

	public double getHeading() {
		return heading;
	}

	public void setHeading(double heading) {
		this.heading = heading;
	}

	public double getTemp() {
		return temp;
	}

	public void setTemp(double temp) {
		this.temp = temp;
	}

	public double getPress() {
		return press;
	}

	public void setPress(double press) {
		this.press = press;
	}

	public double getHum() {
		return hum;
	}

	public void setHum(double hum) {
		this.hum = hum;
	}

	public double getPolarCoeff() {
		return polarCoeff;
	}

	public void setPolarCoeff(double polarCoeff) {
		this.polarCoeff = polarCoeff;
	}

	public double getDepth() {
		return depth;
	}

	public void setDepth(double depth) {
		this.depth = depth;
	}

	public double getDepthOffset() {
		return depthOffset;
	}

	public void setDepthOffset(double depthOffset) {
		this.depthOffset = depthOffset;
	}

	public double getDepthRange() {
		return depthRange;
	}

	public void setDepthRange(double depthRange) {
		this.depthRange = depthRange;
	}

	public String getPolars() {
		return polars;
	}

	public void setPolars(String polars) {
		this.polars = polars;
	}

	public boolean isUsePolars() {
		return usePolars;
	}

	public void setUsePolars(boolean usePolars) {
		this.usePolars = usePolars;
	}

	
	public NMEASimulatorSourceSettings(String confFile) {
		this.confFile = confFile;
	}
	
    public void loadConf() {
    	try {
    		File f = new File(Constants.CONF_DIR, confFile);
    		if (f.exists() && f.lastModified() > lastConfModified) {
    				lastConfModified = f.lastModified();
    				Properties p = new Properties();
    				try (FileInputStream fi = new FileInputStream(f)) {
						p.load(fi);
					}
    				readConf(p);
    		}
    	} catch (Exception e) {
			ServerLog.getLogger().error("Error reading smulator conf", e);
		}
    }
    
	private void readConf(Properties p) {
		vhw = p.getProperty("simulate.vhw", "0").equals(YES);  // water spead and heading
		vlw = p.getProperty("simulate.vlw", "0").equals(YES);  // distance traveled through water
		gll = p.getProperty("simulate.gll", "0").equals(YES);  // gps
		rmc = p.getProperty("simulate.rmc", "0").equals(YES);  // gps
		gpsOut = p.getProperty("simulate.gps.outlier", "0").equals(YES); // simulate GPS going nuts
		dpt = p.getProperty("simulate.dpt", "0").equals(YES);  // depth
		dbt = p.getProperty("simulate.dbt", "0").equals(YES);  // depth
		mtw = p.getProperty("simulate.mtw", "0").equals(YES);  // water temp
		mta = p.getProperty("simulate.mta", "0").equals(YES);  // air temp
		mbb = p.getProperty("simulate.mbb", "0").equals(YES);  // atm pressure
		mhu = p.getProperty("simulate.mhu", "0").equals(YES);  // humidity
		mda = p.getProperty("simulate.mda", "0").equals(YES);  // aggregated meteo
		mwva = p.getProperty("simulate.mwv.apparent", "0").equals(YES);  // wind apparent
		mwvt = p.getProperty("simulate.mwv.true", "0").equals(YES);  // wind true
		vwr = p.getProperty("simulate.vwr", "0").equals(YES);  // relative wind speed and angle (apparent)
		hdm = p.getProperty("simulate.hdm", "0").equals(YES); // magn heading
		hdg = p.getProperty("simulate.hdg", "0").equals(YES);  // magn heading + variation/deviation
		hdt = p.getProperty("simulate.hdt", "0").equals(YES); // true heading
		vtg = p.getProperty("simulate.vtg", "0").equals(YES);  // cog-sog
		rsa = p.getProperty("simulate.rsa", "0").equals(YES);  // rudder angle
		usePolars = p.getProperty("simulate.use.polars", "0").equals(YES);  // use polars to calculate the speed
		autoPilot = p.getProperty("simulate.autopilot", "0").equals(YES);
		xdrDiag = p.getProperty("simulate.xdr.diag", "0").equals(YES);
		xdrMeteo = p.getProperty("simulate.xdr.meteo", "0").equals(YES);
		xdrMeteoAtm = p.getProperty("simulate.xdr.meteo.atm", "0").equals(YES);
		xdrMeteoHum = p.getProperty("simulate.xdr.meteo.hum", "0").equals(YES);
		xdrMeteoTmp = p.getProperty("simulate.xdr.meteo.tmp", "0").equals(YES);
		xdrGYR = p.getProperty("simulate.xdr.gyro", "0").equals(YES);

		splitWind = p.getProperty("simulate.split.wind", "0").equals(YES);
		windSpeedInMS = p.getProperty("simulate.wind.ms", "0").equals(YES);

		polars = p.getProperty("simulate.polars.file", "dufour35c.csv");

		try {
			polarCoeff = Double.parseDouble(p.getProperty("simulate.use.polars.coeff", "0.85"));
		} catch (Exception ignored) { /* optional data */ }
		try {
			speed = Double.parseDouble(p.getProperty("simulate.speed", "5.9"));
		} catch (Exception ignored) { /* optional data */ }
		try {
			rudder = Double.parseDouble(p.getProperty("simulate.rudder", "6.0"));
		} catch (Exception ignored) { /* optional data */ }
		try {
			wSpeed = Double.parseDouble(p.getProperty("simulate.wSpeed", "11.1"));
		} catch (Exception ignored) { /* optional data */ }
		try {
			wDirection = Double.parseDouble(p.getProperty("simulate.wDirection", "270"));
		} catch (Exception ignored) { /* optional data */ }
		try {
			heading = Double.parseDouble(p.getProperty("simulate.heading", "354"));
		} catch (Exception ignored) { /* optional data */ }
		try {
			press = Double.parseDouble(p.getProperty("simulate.pressure", "1013"));
		} catch (Exception ignored) { /* optional data */ }
		 try { temp = Double.parseDouble(p.getProperty("simulate.temperature", "22.1")); } catch (Exception ignored) { /* optional data */ }
		 try { hum = Double.parseDouble(p.getProperty("simulate.humidity", "48.2")); } catch (Exception ignored) { /* optional data */ }

		 try { depth = Double.parseDouble(p.getProperty("simulate.dpt.depth", "8.0")); } catch (Exception ignored) { /* optional data */ }
		 try { depthOffset = Double.parseDouble(p.getProperty("simulate.dpt.offset", "0.0")); } catch (Exception ignored) { /* optional data */ }
		 try { depthRange = Double.parseDouble(p.getProperty("simulate.dpt.range", "2.0")); } catch (Exception ignored) { /* optional data */ }
		 
		 
	}

    public boolean isSplitWind() {
        return splitWind;
    }

    public void setSplitWind(boolean splitWind) {
        this.splitWind = splitWind;
    }

    public boolean isWindSpeedInMS() {
        return windSpeedInMS;
    }

    public void setWindSpeedInMS(boolean windSpeedInMS) {
        this.windSpeedInMS = windSpeedInMS;
    }
}