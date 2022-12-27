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

package com.aboni.nmea.router.data.sampledquery;

import com.aboni.nmea.router.data.StatsSample;
import com.aboni.utils.Utils;
import org.json.JSONObject;

class DefaultSampleWriter implements SampleWriter {
    @Override
    public JSONObject[] getSampleNode(StatsSample sample) {
        JSONObject s = new JSONObject();
        s.put("time", sample.getT0());
        s.put("vMin", Utils.round(sample.getMin(), 2));
        s.put("v", Utils.round(sample.getAvg(), 2));
        s.put("vMax", Utils.round(sample.getMax(), 2));
        return new JSONObject[]{s};
    }
}
