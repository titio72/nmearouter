package com.aboni.utils;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

public class HWSettings {

	private final static Properties prop = new Properties();
	private static long lastProp;
	
	private HWSettings() {}
	
    private static void readConf() {
        try {
            File f = new File(Constants.SENSOR);
            if (f.exists() && f.lastModified() > lastProp) {
                ServerLog.getLogger().Info("Reading sensor configuration file");
                FileInputStream propInput = new FileInputStream(f);
                prop.clear();
                prop.load(propInput);
                propInput.close();
                lastProp = f.lastModified();
            }
        } catch (Exception e) {
            ServerLog.getLogger().Error("Cannot read sensors configuration!", e);
        }
    }
    
    public static String getProperty(String key, String def) {
    	synchronized (prop) {
	    	readConf();
	    	return prop.getProperty(key, def);
    	}
    }
    
    public static String getProperty(String key) {
    	synchronized (prop) {
	    	readConf();
	    	return prop.getProperty(key);
    	}
    }
    
    public static double getPropertyAsDouble(String key, double defValue) {
    	synchronized (prop) {
			String s = getProperty(key);
			if (s!=null) {
				try {
					return Double.parseDouble(s);
				} catch (NumberFormatException e) {
					String msg = String.format("Invalid sensor property {%s} value {%s}", key, s);
					ServerLog.getLogger().Error(msg);
					throw new NumberFormatException(msg);
				}
			}
			return defValue;
		}
    }
    
    public static int getPropertyAsInteger(String key, int defValue) {
    	synchronized (prop) {
			String s = getProperty(key);
			if (s!=null) {
				try {
					return Integer.parseInt(s);
				} catch (NumberFormatException e) {
					String msg = String.format("Invalid sensor property {%s} value {%s}", key, s);
					ServerLog.getLogger().Error(msg);
					throw new NumberFormatException(msg);
				}
			}
			return defValue;
		}
    }
}
