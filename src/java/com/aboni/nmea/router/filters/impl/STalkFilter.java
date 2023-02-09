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

package com.aboni.nmea.router.filters.impl;

import com.aboni.nmea.router.RouterMessage;
import com.aboni.nmea.router.filters.NMEAFilter;
import com.aboni.nmea.router.nmea0183.NMEA0183Message;
import com.aboni.utils.JSONUtils;
import net.sf.marineapi.nmea.sentence.STALKSentence;
import net.sf.marineapi.nmea.sentence.Sentence;
import org.json.JSONObject;

public class STalkFilter implements NMEAFilter {

    public static final String FILTER = "filter";
    private final String command;
    private final boolean negate;

    public STalkFilter(String command, boolean negate) {
        this.command = command;
        this.negate = negate;
    }

    public String getCommand() {
		return command;
	}

	public boolean isNegate() {
		return negate;
	}

    @Override
    public boolean match(RouterMessage m) {
        if (m.getMessage() instanceof NMEA0183Message && ((NMEA0183Message) m.getMessage()).getSentence() instanceof STALKSentence) {
            Sentence s = ((NMEA0183Message) m.getMessage()).getSentence();
            boolean b = command.equals(((STALKSentence) s).getCommand());
            return negate == (!b);
        } else {
            return false;
        }
    }

    @Override
    public JSONObject toJSON() {
        JSONObject obj = new JSONObject();
        JSONObject fltObj = new JSONObject();
        obj.put(FILTER, fltObj);
        fltObj.put("type", FILTER_TYPE);
        fltObj.put("command", command);
        fltObj.put("negate", negate);
        return obj;
    }

    public static final String FILTER_TYPE = "seatalk";

    public static STalkFilter parseFilter(JSONObject obj) {
        obj = JSONFilterUtils.getFilter(obj, FILTER_TYPE);
        return new STalkFilter(
                JSONUtils.getAttribute(obj, "command", ""),
                JSONUtils.getAttribute(obj, "negate", false)
        );
    }
}
