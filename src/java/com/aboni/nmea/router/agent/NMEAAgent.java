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

import com.aboni.nmea.router.conf.QOS;
import com.aboni.nmea.router.JSONable;
import com.aboni.utils.Startable;

public interface NMEAAgent extends Startable, JSONable {

    String getType();

    String getName();

    String getDescription();

    boolean isBuiltIn();

    boolean isUserCanStartAndStop();

    void setup(String name, QOS qos);

    void setStatusListener(NMEAAgentStatusListener listener);

    NMEASource getSource();

    NMEATarget getTarget();

    void onTimer();

    void onTimerHR();
}
