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

import com.aboni.nmea.router.Constants;
import com.aboni.nmea.router.OnRouterMessage;
import com.aboni.nmea.router.RouterMessage;
import com.aboni.nmea.router.TimestampProvider;
import com.aboni.nmea.router.data.Sampler;
import com.aboni.nmea.router.data.StatsWriter;
import com.aboni.nmea.router.data.metrics.PowerMetrics;
import com.aboni.nmea.router.message.Message;
import com.aboni.nmea.router.message.MsgBattery;
import com.aboni.nmea.router.message.MsgDCDetailedStatus;
import com.aboni.nmea.router.utils.Log;

import javax.inject.Inject;
import javax.inject.Named;

public class NMEAPowerDBTarget extends NMEAAgentImpl {

    private static final long ONE_MINUTE = 60000L;
    private static final int BATTERY_INSTANCE = 0;

    private final Sampler powerSampler;
    private final Log log;

    @Inject
    public NMEAPowerDBTarget(Log log, TimestampProvider tp, @Named(Constants.TAG_POWER) StatsWriter w) {
        super(log, tp, false, true);
        if (w==null) throw new IllegalArgumentException("StatWriter cannot be null");
        this.log = log;
        powerSampler = new Sampler(log, tp, w, "Power2DB");
        powerSampler.initMetric(PowerMetrics.VOLTAGE_0,
                (Message m) -> ((m instanceof MsgBattery) && ((MsgBattery) m).getInstance() == BATTERY_INSTANCE),
                (Message m) -> ((MsgBattery) m).getVoltage(),
                ONE_MINUTE, "V_0", -100.0, 100.0);
        powerSampler.initMetric(PowerMetrics.CURRENT_0,
                (Message m) -> ((m instanceof MsgBattery) && ((MsgBattery) m).getInstance() == BATTERY_INSTANCE),
                (Message m) -> ((MsgBattery) m).getCurrent(),
                ONE_MINUTE, "C_0", -500.0, 500.0);
        powerSampler.initMetric(PowerMetrics.TEMPERATURE_0,
                (Message m) -> ((m instanceof MsgBattery) && ((MsgBattery) m).getInstance() == BATTERY_INSTANCE),
                (Message m) -> ((MsgBattery) m).getTemperature(),
                ONE_MINUTE, "T_0", -30.0, 100.0);
        powerSampler.initMetric(PowerMetrics.POWER_0,
                (Message m) -> ((m instanceof MsgBattery) && ((MsgBattery) m).getInstance() == BATTERY_INSTANCE),
                (Message m) -> ((MsgBattery) m).getCurrent() * ((MsgBattery) m).getVoltage(),
                ONE_MINUTE, "P_0", -5000.0, 5000.0);
        powerSampler.initMetric(PowerMetrics.SOC_0,
                (Message m) -> ((m instanceof MsgDCDetailedStatus) && ((MsgDCDetailedStatus) m).getInstance() == BATTERY_INSTANCE),
                (Message m) -> ((MsgDCDetailedStatus) m).getSOC(),
                ONE_MINUTE, "SOC", 0.0, 1.0);
    }

    @Override
    public final String getDescription() {
        return getType();
    }

    @Override
    public String getType() {
        return "Power utilization sampler DB";
    }

    @Override
    public String toString() {
        return getType();
    }

    @Override
    protected boolean onActivate() {
        try {
            powerSampler.start();
            return true;
        } catch (Exception e) {
            log.errorForceStacktrace(() -> getLogBuilder().wO("activate").toString(), e);
            return false;
        }
    }

    @Override
    public void onTimer() {
        super.onTimer();
        powerSampler.dumpAndReset();
    }

    @Override
    protected void onDeactivate() {
        powerSampler.stop();
    }

    @OnRouterMessage
    public void onSentence(RouterMessage msg) {
        powerSampler.onSentence(msg);
    }
}
