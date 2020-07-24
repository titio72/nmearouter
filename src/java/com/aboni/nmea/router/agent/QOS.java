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

package com.aboni.nmea.router.agent;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

public class QOS {

	private final Map<String, Object> theQOS;

	public QOS() {
		theQOS = new HashMap<>();
	}

	public void addProp(String propName) {
		theQOS.put(propName, 1);
	}

	public void addProp(String propName, String v) {
        theQOS.put(propName, v);
    }

    public boolean get(String propName) {
        return theQOS.containsKey(propName);
    }

    public String[] getKeys() {
        return theQOS.keySet().toArray(new String[]{});
    }

    public static QOS parse(String qos) {
        QOS q = new QOS();
        if (qos != null) {
            StringTokenizer t = new StringTokenizer(qos, ";");
            while (t.hasMoreTokens()) {
                StringTokenizer t1 = new StringTokenizer(t.nextToken(), "=");
                if (t1.countTokens() == 1) {
                    String token = t1.nextToken();
                    q.addProp(token);
                } else {
                    q.addProp(t1.nextToken(), t1.nextToken());
                }
            }
        }
        return q;
    }
}
