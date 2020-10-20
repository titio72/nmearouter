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

package com.aboni.nmea.router;

public class SatInfo {

    public String getId() {
        return id;
    }

    public int getElevation() {
        return elevation;
    }

    public int getAzimuth() {
        return azimuth;
    }

    public int getNoise() {
        return noise;
    }

    public boolean isUsed() {
        return used;
    }

    public GPSSat getSat() {
        return sat;
    }

    public SatInfo(String id, int elevation, int azimuth, int noise, boolean used) {
        this.id = id;
        this.elevation = elevation;
        this.azimuth = azimuth;
        this.noise = noise;
        this.used = used;
        this.sat = GPSSatsRepository.getSat(Integer.parseInt(id));
    }

    private final String id;
    private final int elevation;
    private final int azimuth;
    private final int noise;
    private final boolean used;
    private final GPSSat sat;
}
