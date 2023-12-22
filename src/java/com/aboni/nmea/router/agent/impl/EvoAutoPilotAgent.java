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

import com.aboni.log.Log;
import com.aboni.nmea.message.*;
import com.aboni.nmea.router.EvoAutoPilotStatus;
import com.aboni.nmea.router.OnRouterMessage;
import com.aboni.nmea.router.RouterMessage;
import com.aboni.nmea.router.RouterMessageFactory;
import com.aboni.utils.TimestampProvider;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EvoAutoPilotAgent extends NMEAAgentImpl implements EvoAutoPilotStatus {

    @Inject
    public EvoAutoPilotAgent(Log log, TimestampProvider tp, RouterMessageFactory messageFactory) {
        super(log, tp, messageFactory, false, true);
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

    private double apHeading;
    private double apLockedHeading;
    private double apWindDatum;
    private double apAverageWind;
    private PilotMode mode;

    @OnRouterMessage
    public void onMessage(RouterMessage routerMessage) {
        Message m = routerMessage.getPayload();
        if (m instanceof MsgSeatalkPilotHeading) {
            apHeading = ((MsgSeatalkPilotHeading) m).getHeadingMagnetic();
        } else if (m instanceof MsgSeatalkPilotLockedHeading) {
            handleLockedHeadingMessage(routerMessage.getTimestamp(), (MsgSeatalkPilotLockedHeading) m);
        } else if (m instanceof MsgSeatalkPilotWindDatum) {
            handleWindDatumMessage(routerMessage.getTimestamp(), (MsgSeatalkPilotWindDatum) m);
        } else if (m instanceof MsgSeatalkPilotMode) {
            handlePilotModeMessage(routerMessage.getTimestamp(), (MsgSeatalkPilotMode) m);
        }
    }

    private void handleLockedHeadingMessage(long timestamp, MsgSeatalkPilotLockedHeading m) {
        double newLockedHeading = m.getLockedHeadingMagnetic();
        if (Double.isNaN(apLockedHeading) || Math.abs(newLockedHeading - apLockedHeading)>0.1) {
            apLockedHeading = newLockedHeading;
            notifyLockedHeading(timestamp);
        }
    }

    private void handleWindDatumMessage(long timestamp, MsgSeatalkPilotWindDatum m) {
        apAverageWind = m.getRollingAverageWind();
        double newDatum = m.getWindDatum();
        if (Double.isNaN(apWindDatum) || Math.abs(newDatum - apWindDatum)>0.1) {
            apWindDatum = newDatum;
            notifyWindDatum(timestamp);
        }
    }

    private void handlePilotModeMessage(long timestamp, MsgSeatalkPilotMode m) {
        if (m.getMode()!=mode) {
            PilotMode oldMode = mode;
            mode = m.getMode();
            if (mode != PilotMode.VANE) {
                apWindDatum = Double.NaN;
                apAverageWind = Double.NaN;
            }
            if (mode != PilotMode.AUTO) {
                apLockedHeading = Double.NaN;
            }
            notifyMode(oldMode, timestamp);
        }
    }

    private void notifyWindDatum(long timestamp) {
        List<PilotStatusListener> copy;
        synchronized (listenerSet) {
            copy = new ArrayList<>(listenerSet);
        }
        for (PilotStatusListener l: copy) {
            l.onPilotStatus(null, mode, getApWindDatum(), timestamp);
        }
    }

    private void notifyLockedHeading(long timestamp) {
        List<PilotStatusListener> copy;
        synchronized (listenerSet) {
            copy = new ArrayList<>(listenerSet);
        }
        for (PilotStatusListener l: copy) {
            l.onPilotStatus(null, mode, getApLockedHeading(), timestamp);
        }
    }

    private void notifyMode(PilotMode oldMode, long timestamp) {
        List<PilotStatusListener> copy;
        synchronized (listenerSet) {
            copy = new ArrayList<>(listenerSet);
        }
        for (PilotStatusListener l: copy) {
            l.onPilotStatus(oldMode, mode, Double.NaN, timestamp);
        }
    }

    private final Set<PilotStatusListener> listenerSet = new HashSet<>();

    @Override
    public void listen(PilotStatusListener listener) {
        synchronized (listenerSet) {
            listenerSet.add(listener);
        }
    }

    @Override
    public void stopListening(PilotStatusListener listener) {
        synchronized (listenerSet) {
            listenerSet.remove(listener);
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
