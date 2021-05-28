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

package com.aboni.nmea.router.message.impl;

import com.aboni.nmea.router.message.MsgGNSSPosition;
import net.sf.marineapi.nmea.util.Position;

import java.time.Instant;

public class MsgGNSSPositionImpl implements MsgGNSSPosition {
    private final Position position;
    private final Instant time;
    private final int satellites;
    private final double hdop;
    private final double pdop;

    public MsgGNSSPositionImpl(Position position, Instant time, int satellites, double hdop, double pdop) {
        this.position = position;
        this.time = time;
        this.hdop = hdop;
        this.pdop = pdop;
        this.satellites = satellites;
    }


    @Override
    public int getSID() {
        return -1;
    }

    @Override
    public Instant getTimestamp() {
        return time;
    }

    @Override
    public double getAltitude() {
        return 0;
    }

    @Override
    public String getGnssType() {
        return "GPS";
    }

    @Override
    public String getMethod() {
        return "GNSS Fix";
    }

    @Override
    public String getIntegrity() {
        return "No integrity checking";
    }

    @Override
    public int getNSatellites() {
        return satellites;
    }

    @Override
    public double getHDOP() {
        return hdop;
    }

    @Override
    public boolean isHDOP() {
        return !Double.isNaN(hdop);
    }

    @Override
    public double getPDOP() {
        return pdop;
    }

    @Override
    public boolean isPDOP() {
        return !Double.isNaN(pdop);
    }

    @Override
    public double getGeoidalSeparation() {
        return Double.NaN;
    }

    @Override
    public int getReferenceStations() {
        return 0;
    }

    @Override
    public String getReferenceStationType() {
        return null;
    }

    @Override
    public int getReferenceStationId() {
        return 0;
    }

    @Override
    public double getAgeOfDgnssCorrections() {
        return 0;
    }

    @Override
    public Position getPosition() {
        return position;
    }

    @Override
    public String toString() {
        return String.format("GNSSPosition: Position {%s} Time {%s}", getPosition(), getTimestamp());
    }
}
