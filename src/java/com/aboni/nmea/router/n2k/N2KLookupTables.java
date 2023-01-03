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

package com.aboni.nmea.router.n2k;

import com.aboni.nmea.router.Constants;
import com.aboni.nmea.router.utils.Log;
import com.aboni.nmea.router.utils.ThingsFactory;
import com.aboni.utils.LogStringBuilder;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public class N2KLookupTables {

    public enum LOOKUP_MAPS {
        MANUFACTURER_CODE,
        INDUSTRY_CODE,
        SHIP_TYPE,
        DEVICE_CLASS,
        REPEAT_INDICATOR,
        AIS_TRANSCEIVER,
        AIS_ASSIGNED_MODE,
        ATON_TYPE,
        AIS_SPECIAL_MANEUVER,
        POSITION_FIX_DEVICE,
        ENGINE_INSTANCE,
        ENGINE_STATUS_1,
        ENGINE_STATUS_2,
        GEAR_STATUS,
        POSITION_ACCURACY,
        RAIM_FLAG,
        ALERT_RESPONSE_COMMAND,
        ALERT_LANGUAGE_ID,
        ALERT_STATE,
        ALERT_THRESHOLD_STATUS,
        ALERT_CATEGORY,
        ALERT_TRIGGER_CONDITION,
        ALERT_TYPE,
        ENTERTAINMENT_EQ,
        ENTERTAINMENT_FILTER,
        POWER_FACTOR,
        TIME_STAMP,
        GNS_AIS,
        RESIDUAL_MODE,
        GNS,
        GNS_METHOD,
        GNS_INTEGRITY,
        WIND_REFERENCE,
        WATER_REFERENCE,
        SYSTEM_TIME,
        MAGNETIC_VARIATION,
        YES_NO,
        OK_WARNING,
        DIRECTION_REFERENCE,
        NAV_STATUS,
        ENTERTAINMENT_CHANNEL,
        ENTERTAINMENT_GROUP,
        ENTERTAINMENT_TYPE,
        ENTERTAINMENT_LIKE_STATUS,
        ENTERTAINMENT_SHUFFLE_STATUS,
        ENTERTAINMENT_REPEAT_STATUS,
        ENTERTAINMENT_PLAY_STATUS,
        SEATALK_ALARM_GROUP,
        ENTERTAINMENT_ZONE,
        ENTERTAINMENT_SOURCE,
        SEATALK_ALARM_ID,
        TEMPERATURE_SOURCE,
        HUMIDITY_SOURCE,
        PRESSURE_SOURCE,
        DSC_FORMAT,
        DSC_CATEGORY,
        DSC_NATURE,
        DSC_FIRST_TELE_COMMAND,
        DSC_SECOND_TELE_COMMAND,
        DSC_EXPANSION_DATA,
        SEATALK_ALARM_STATUS
    }
    
    private N2KLookupTables() {
    }

    private static final EnumMap<LOOKUP_MAPS, Map<Integer, String>> TABLES = new EnumMap<>(LOOKUP_MAPS.class);

    public static Map<Integer, String> getTable(LOOKUP_MAPS m) {
        return TABLES.getOrDefault(m, null);
    }

    static {
        StringBuilder b = new StringBuilder();
        try (FileReader r = new FileReader(Constants.CONF_DIR + "/n2k_lookup.json")) {
            BufferedReader bf = new BufferedReader(r);
            String l;
            while ((l = bf.readLine()) != null) {
                b.append(l);
            }
            JSONObject j = new JSONObject(b.toString());
            for (LOOKUP_MAPS mapName : LOOKUP_MAPS.values()) {
                if (j.has(mapName.toString())) {
                    JSONObject jMap = j.getJSONObject(mapName.toString());
                    Map<Integer, String> m = new HashMap<>();
                    TABLES.put(mapName, m);
                    for (String k : jMap.keySet()) {
                        int iK = Integer.parseInt(k);
                        String v = jMap.getString(k);
                        m.put(iK, v);
                    }
                }
            }
        } catch (Exception e) {
            ThingsFactory.getInstance(Log.class).error(() -> LogStringBuilder.start("N2KLookupTables").wO("load").toString(), e);
        }
    }

    public static void dump() {
        JSONObject j = new JSONObject();
        try (FileWriter w = new FileWriter(Constants.CONF_DIR + "/n2k_lookup.json")) {
            for (LOOKUP_MAPS l: LOOKUP_MAPS.values()) {
                JSONObject jLookupMap = new JSONObject();
                Map<Integer, String> m = getTable(l);
                for (Map.Entry<Integer, String> e: m.entrySet()) {
                    jLookupMap.put(e.getKey().toString(), e.getValue());
                }
                j.put("" + l, jLookupMap);
            }
            w.write(j.toString(2));
        } catch (Exception e) {
            ThingsFactory.getInstance(Log.class).error(() -> LogStringBuilder.start("N2KLookupTables").wO("dump").toString(), e);
        }
    }
}
