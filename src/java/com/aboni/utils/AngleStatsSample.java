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

package com.aboni.utils;

import com.aboni.misc.Utils;

public class AngleStatsSample extends StatsSample {

    public AngleStatsSample(String tag) {
        super(tag);
    }

    @Override
    public void add(double v) {
        if (samples == 0) {
            v = Utils.normalizeDegrees0To360(v);
            avg = v;
            max = v;
            min = v;
            samples = 1;
        } else {
            double a = Utils.getNormal180(avg, v);
            avg = ((avg * samples) +  a) / (samples +1);
            avg = Utils.normalizeDegrees0To360(avg);
            max = Utils.normalizeDegrees0To360(Math.max(max,  a));
            min = Utils.normalizeDegrees0To360(Math.min(min,  a));
            samples++;
        }
    }
}
