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

public final class PowerMetrics {

    private PowerMetrics() {
    }

    ;

    public static final Metric VOLTAGE_0 = new Metric("V_0", "Voltage service battery", Unit.VOLTS);
    public static final Metric CURRENT_0 = new Metric("C_0", "Current service battery", Unit.AMPERE);
    public static final Metric TEMPERATURE_0 = new Metric("T_0", "Temperature service battery", Unit.CELSIUS);
    public static final Metric SOC_0 = new Metric("S_0", "SOC service battery", Unit.CELSIUS);
    public static final Metric POWER_0 = new Metric("P_0", "Power service battery", Unit.WATT);
}
