/*
 * Copyright (c) 2022,  Andrea Boni
 * This file is part of NMEARouter.
 * NMEARouter is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * NMEARouter is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with NMEARouter.  If not, see <http://www.gnu.org/licenses/>.
 */

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

package com.aboni.nmea.router.utils;

import com.aboni.nmea.router.Constants;
import com.aboni.utils.LogStringBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

public class HWSettings {

    private static final Properties prop = new Properties();
    private static long lastProp;

    private HWSettings() {}

    private static void readConf() {
        Log log = ThingsFactory.getInstance(Log.class);
        try {
            File f = new File(Constants.SENSOR);
            if (f.exists() && f.lastModified() > lastProp) {
                log.info(LogStringBuilder.start("HWSettings").wO("load").wV("file", f.getAbsolutePath()).toString());
                try (FileInputStream propInput = new FileInputStream(f)) {
                    prop.clear();
                    prop.load(propInput);
                    lastProp = f.lastModified();
                }
            }
        } catch (Exception e) {
            log.error(LogStringBuilder.start("HWSettings").wO("load").toString(), e);
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
                    throw new NumberFormatException(msg);
                }
            }
            return defValue;
        }
    }
}
