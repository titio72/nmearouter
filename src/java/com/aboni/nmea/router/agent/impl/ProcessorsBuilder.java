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

package com.aboni.nmea.router.agent.impl;

import com.aboni.nmea.router.NMEACache;
import com.aboni.nmea.router.conf.QOS;
import com.aboni.nmea.router.filters.impl.NMEASpeedFilter;
import com.aboni.nmea.router.processors.*;
import com.aboni.utils.Log;
import com.aboni.utils.ThingsFactory;

import java.util.ArrayList;
import java.util.List;

public class ProcessorsBuilder {

    private ProcessorsBuilder() {
    }

    static List<NMEAPostProcess> load(QOS qos, Log log) {
        List<NMEAPostProcess> res = new ArrayList<>();
        if (qos != null) {
            NMEACache cache = ThingsFactory.getInstance(NMEACache.class);
            for (String q : qos.getKeys()) {
                switch (q) {
                    case "speed_filter":
                        log.info("QoS {Speed filter}");
                        res.add(new NMEAGenericFilterProc(cache, ThingsFactory.getInstance(NMEASpeedFilter.class)));
                        break;
                    case "dpt":
                        log.info("QoS {Depth}");
                        res.add(ThingsFactory.getInstance(NMEADepthEnricher.class));
                        break;
                    case "rmc2vtg":
                        log.info("QoS {RMC to VTG}");
                        res.add(ThingsFactory.getInstance(NMEARMC2VTGProcessor.class));
                        break;
                    case "truewind_sog":
                        log.info("QoS {True wind SOG}");
                        res.add(new NMEAMWVTrue(cache, true));
                        break;
                    case "truewind":
                        log.info("QoS {True wind}");
                        res.add(new NMEAMWVTrue(cache, false));
                        break;
                    case "enrich_hdg":
                        log.info("QoS {Enrich HDG}");
                        res.add(ThingsFactory.getInstance(NMEAHDGEnricher.class));
                        break;
                    case "enrich_hdm":
                        log.info("QoS {Enrich HDM}");
                        res.add(ThingsFactory.getInstance(NMEAHDMEnricher.class));
                        break;
                    case "rmc_filter":
                        log.info("QoS {RMC filter}");
                        res.add(ThingsFactory.getInstance(NMEARMCFilterProcessor.class));
                        break;
                    default:
                        break;
                }
            }
        }
        return res;
    }
}
