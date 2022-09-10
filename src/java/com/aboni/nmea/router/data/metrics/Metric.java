/*
 * Copyright (c) 2021,  Andrea Boni
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

package com.aboni.nmea.router.data.metrics;

import com.aboni.nmea.router.data.Unit;

import javax.validation.constraints.NotNull;

public class Metric {

    private final String id;
    private final String description;
    private final Unit unit;

    public Metric(@NotNull String id, @NotNull String description, @NotNull Unit unit) {
        this.id = id;
        this.description = description;
        this.unit = unit;
    }

    public String getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public Unit getUnit() {
        return unit;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Metric) return id.equals(((Metric) o).getId());
        else return false;
    }
}
