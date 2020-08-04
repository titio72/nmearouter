package com.aboni.nmea.router;

import com.aboni.nmea.router.n2k.AISStaticData;
import com.aboni.nmea.router.n2k.AISPositionReport;

import java.util.List;

public interface AISTargets {
    List<AISPositionReport> getAISTargets();

    AISStaticData getData(String mmsi);
}
