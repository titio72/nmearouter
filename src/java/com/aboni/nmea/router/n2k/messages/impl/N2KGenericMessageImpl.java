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

package com.aboni.nmea.router.n2k.messages.impl;

import com.aboni.nmea.router.n2k.N2KMessageHeader;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class N2KGenericMessageImpl extends N2KMessageImpl {

    private static final DateTimeFormatter timeF = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH:mm:ss.SSS");

    public N2KGenericMessageImpl(N2KMessageHeader header, byte[] data) {
        super(header, data);
    }

    @Override
    public String toString() {
        StringBuilder res = new StringBuilder(String.format("%s,%d,%d,%d,%d,%d",
                LocalDateTime.now().format(timeF), getHeader().getPriority(),
                getHeader().getPgn(), getHeader().getSource(), getHeader().getDest(),
                getData().length));
        for (byte datum : data) res.append(String.format(",%02x", datum));
        return res.toString();
    }

    @Override
    public String getMessageContentType() {
        return "";
    }
}
