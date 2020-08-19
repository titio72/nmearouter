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

package com.aboni.nmea.router.n2k.impl;

import com.aboni.misc.Utils;
import com.aboni.nmea.router.n2k.N2KMessageHeader;
import com.aboni.nmea.router.n2k.messages.N2KSeatalkPilotHeading;

public class N2KSeatalkPilotHeadingImpl extends N2KMessageImpl implements N2KSeatalkPilotHeading {

    private double headingMagnetic;

    private double headingTrue;

    public N2KSeatalkPilotHeadingImpl(byte[] data) {
        super(getDefaultHeader(PGN), data);
        fill();
    }

    public N2KSeatalkPilotHeadingImpl(N2KMessageHeader header, byte[] data) {
        super(header, data);
        fill();
    }

    private void fill() {
        Double d = parseDouble(data, 40, 16, 0.0001, false);
        headingMagnetic = (d == null) ? Double.NaN : Utils.round(Math.toDegrees(d), 1);

        d = parseDouble(data, 24, 16, 0.0001, false);
        headingTrue = (d == null) ? Double.NaN : Utils.round(Math.toDegrees(d), 1);
    }

    @Override
    public double getHeadingMagnetic() {
        return headingMagnetic;
    }

    @Override
    public double getHeadingTrue() {
        return headingTrue;
    }
}
