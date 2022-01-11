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

import com.aboni.nmea.router.message.DCType;
import com.aboni.nmea.router.message.MsgDCDetailedStatus;

public class MsgDCDetailedStatusImpl implements MsgDCDetailedStatus {

    private final int instance;
    private final int sid;
    private final double ripple;
    private final double soc;
    private final double soh;
    private final int ttg;
    private final DCType type;

    public MsgDCDetailedStatusImpl(int sid, int instance, DCType type, double soc, double soh, int ttg, double ripple) {
        this.instance = instance;
        this.sid = sid;
        this.soh = soh;
        this.soc = soc;
        this.ripple = ripple;
        this.ttg = ttg;
        this.type = type;
    }

    @Override
    public int getInstance() {
        return instance;
    }

    @Override
    public int getSID() {
        return sid;
    }

    @Override
    public DCType getType() {
        return type;
    }

    @Override
    public double getRippleVoltage() {
        return ripple;
    }

    @Override
    public double getSOC() {
        return soc;
    }

    @Override
    public double getSOH() {
        return soh;
    }

    @Override
    public int getTimeToGo() {
        return ttg;
    }

    @Override
    public String toString() {
        return String.format("DC Detailed Status: Instance {%d} Type {%s} SOC {%.2f} SOH {%.2f} TTG {%d} Ripple {%.2f}",
                getInstance(), getType().toString(), getSOC(), getSOH(), getTimeToGo(), getRippleVoltage());
    }
}
