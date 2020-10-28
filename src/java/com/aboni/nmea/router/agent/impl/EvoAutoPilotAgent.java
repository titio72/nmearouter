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

package com.aboni.nmea.router.agent.impl;

import com.aboni.nmea.router.EvoAutoPilotStatus;
import com.aboni.nmea.router.OnRouterMessage;
import com.aboni.nmea.router.RouterMessage;
import com.aboni.nmea.router.TimestampProvider;
import com.aboni.nmea.router.message.*;
import com.aboni.utils.Log;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;

public class EvoAutoPilotAgent extends NMEAAgentImpl implements EvoAutoPilotStatus {

    public static final String NEW_STATUS_KEY_NAME = "new status";

    @Inject
    public EvoAutoPilotAgent(@NotNull Log log, @NotNull TimestampProvider tp) {
        super(log, tp, false, true);
    }

    @Override
    public String getType() {
        return "EVOPilot";
    }

    @Override
    public String toString() {
        return getType();
    }

    @Override
    public String getDescription() {
        return "Raymarine SeaTalk autopilot driver [" + getMode() + "]";
    }

    @Override
    protected boolean onActivate() {
        return true;
    }

    private double apHeading;
    private double apLockedHeading;
    private double apWindDatum;
    private double apAverageWind;
    private PilotMode mode;

    @OnRouterMessage
    public void onMessage(RouterMessage routerMessage) {
        Message m = routerMessage.getMessage();
        if (m instanceof MsgSeatalkPilotHeading) {
            apHeading = ((MsgSeatalkPilotHeading) m).getHeadingMagnetic();
        } else if (m instanceof MsgSeatalkPilotLockedHeading) {
            apLockedHeading = ((MsgSeatalkPilotLockedHeading) m).getLockedHeadingMagnetic();
        } else if (m instanceof MsgSeatalkPilotWindDatum) {
            apAverageWind = ((MsgSeatalkPilotWindDatum) m).getRollingAverageWind();
            apWindDatum = ((MsgSeatalkPilotWindDatum) m).getWindDatum();
        } else if (m instanceof MsgSeatalkPilotMode) {
            mode = ((MsgSeatalkPilotMode) m).getMode();
            if (mode!=PilotMode.VANE) {
                apWindDatum = Double.NaN;
                apAverageWind = Double.NaN;
            }
            if (mode!=PilotMode.AUTO) {
                apLockedHeading = Double.NaN;
            }
        }
    }

    @Override
    public double getApHeading() {
        return apHeading;
    }

    @Override
    public double getApLockedHeading() {
        return apLockedHeading;
    }

    @Override
    public double getApWindDatum() {
        return apWindDatum;
    }

    @Override
    public double getApAverageWind() {
        return apAverageWind;
    }

    @Override
    public PilotMode getMode() {
        return mode;
    }
}
