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

package com.aboni.nmea.router.agent.impl.system;

import com.aboni.log.Log;
import com.aboni.log.LogStringBuilder;
import com.aboni.nmea.message.TemperatureSource;
import com.aboni.nmea.message.impl.MsgTemperatureImpl;
import com.aboni.nmea.router.RouterMessageFactory;
import com.aboni.nmea.router.agent.impl.NMEAAgentImpl;
import com.aboni.nmea.router.utils.HWSettings;
import com.aboni.sensors.hw.CPUTemp;
import com.aboni.sensors.hw.Fan;
import com.aboni.utils.TimestampProvider;

import javax.inject.Inject;

public class FanAgent extends NMEAAgentImpl {

    private static final double FAN_THRESHOLD_ON = 55.0;
    private static final double FAN_THRESHOLD_OFF = 52.0;
    private final Fan fan;

    @Inject
    public FanAgent(TimestampProvider tp, Log log, RouterMessageFactory messageFactory) {
        super(log, tp, messageFactory, true, false);
        this.fan = new Fan();
    }

    @Override
    public String getDescription() {
        return "CPU Temp " + CPUTemp.getInstance().getTemp() + "C° Fan " + (fan.isFanOn() ? "On" : "Off") +
                " [" + getThresholdOff() + "/" + getThresholdOn() + "]";
    }

    @Override
    protected boolean onActivate() {
        return CPUTemp.getInstance().isSupported();
    }

    @Override
    public void onTimer() {
        super.onTimer();
        if (isStarted()) {
            double temp = CPUTemp.getInstance().getTemp();
            if (fan.isFanOn() && temp<getThresholdOff()) fan(false);
            else if (!fan.isFanOn() && temp>getThresholdOn()) fan(true);
            sendCPUTemp(temp);
        }
    }

    private double getThresholdOff() {
        return HWSettings.getPropertyAsDouble("fan.off", FAN_THRESHOLD_OFF);
    }

    private double getThresholdOn() {
        return HWSettings.getPropertyAsDouble("fan.on", FAN_THRESHOLD_ON);
    }

    private void fan(boolean on) {
        getLog().info(LogStringBuilder.start("Fan Manager").wO("switch").wV("status", on).toString());
        fan.switchFan(on);
    }

    private void sendCPUTemp(double temp) {
        try {
            postMessage(new MsgTemperatureImpl(0, 0, TemperatureSource.CPU, temp, 60.0));
        } catch (Exception e) {
            getLog().debug(() -> LogStringBuilder.start("Fan Manager").wO("read temperature")
                    .wV("error", e.getMessage()).toString());
        }
    }
}
