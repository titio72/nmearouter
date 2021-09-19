/*
 * Copyright (c) 2020,  Andrea Boni
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

package com.aboni.nmea.router.message;

import java.time.Instant;

public interface MsgGNSSPosition extends MsgPosition {

    int getSID();

    Instant getTimestamp();

    double getAltitude();

    String getGnssType();

    String getMethod();

    String getIntegrity();

    /**
     * Number of satellites used for the position calc.
     *
     * @return N of sats is successful, 0xFF if not available
     */
    int getNSatellites();

    double getHDOP();

    boolean isHDOP();

    double getPDOP();

    boolean isPDOP();

    double getGeoidalSeparation();

    int getReferenceStations();

    String getReferenceStationType();

    int getReferenceStationId();

    double getAgeOfDgnssCorrections();

    @Override
    default String getMessageContentType() {
        return "GNSSPosition";
    }
}
