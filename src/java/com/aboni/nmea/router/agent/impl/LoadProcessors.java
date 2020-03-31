package com.aboni.nmea.router.agent.impl;

import com.aboni.nmea.router.NMEACache;
import com.aboni.nmea.router.agent.QOS;
import com.aboni.nmea.router.filters.impl.NMEASpeedFilter;
import com.aboni.nmea.router.processors.*;
import com.aboni.utils.Log;
import com.aboni.utils.ThingsFactory;

import java.util.ArrayList;
import java.util.List;

public class LoadProcessors {

    private LoadProcessors() {
    }

    static List<NMEAPostProcess> load(QOS qos, Log log) {
        List<NMEAPostProcess> res = new ArrayList<>();
        if (qos != null) {
            NMEACache cache = ThingsFactory.getInstance(NMEACache.class);
            for (String q : qos.getKeys()) {
                switch (q) {
                    case "speed_filter":
                        log.info("QoS {SPEED_FILTER}");
                        res.add(new NMEAGenericFilterProc(ThingsFactory.getInstance(NMEASpeedFilter.class)));
                        break;
                    case "dpt":
                        log.info("QoS {DPT}");
                        res.add(ThingsFactory.getInstance(NMEADepthEnricher.class));
                        break;
                    case "rmc2vtg":
                        log.info("QoS {RMC2VTG}");
                        res.add(ThingsFactory.getInstance(NMEARMC2VTGProcessor.class));
                        break;
                    case "truewind_sog":
                        log.info("QoS {TRUEWIND_SOG}");
                        res.add(new NMEAMWVTrue(cache, true));
                        break;
                    case "truewind":
                        log.info("QoS {TRUEWIND}");
                        res.add(new NMEAMWVTrue(cache, false));
                        break;
                    case "enrich_hdg":
                        log.info("QoS {ENRICH_HDG}");
                        res.add(ThingsFactory.getInstance(NMEAHDGEnricher.class));
                        break;
                    case "enrich_hdm":
                        log.info("QoS {ENRICH_HDM}");
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
