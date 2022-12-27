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

import com.aboni.nmea.router.OnRouterMessage;
import com.aboni.nmea.router.RouterMessage;
import com.aboni.nmea.router.TimestampProvider;
import com.aboni.nmea.router.agent.impl.NMEAAgentImpl;
import com.aboni.nmea.router.message.MsgPosition;
import com.aboni.nmea.router.utils.Log;
import com.pi4j.io.gpio.*;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.util.Timer;
import java.util.TimerTask;

public class PowerLedAgent extends NMEAAgentImpl {

    private static final Pin GPS = RaspiPin.GPIO_23;
    private static final Pin PWR = RaspiPin.GPIO_02;
    private final GpioPinDigitalOutput pin;
    private final GpioPinDigitalOutput pinGps;
    private final TimestampProvider timestampProvider;
    private long lastGps;

    @Inject
    public PowerLedAgent(@NotNull Log log, @NotNull TimestampProvider tp) {
        super(log, tp, false, true);
        this.timestampProvider = tp;
        lastGps = 0;
        GpioController gpio = GpioFactory.getInstance();
        pin = gpio.provisionDigitalOutputPin(PWR, "pwr", PinState.LOW);
        pinGps = gpio.provisionDigitalOutputPin(GPS, "gps", PinState.LOW);
        pin.setShutdownOptions(true, PinState.LOW);
        pinGps.setShutdownOptions(true, PinState.LOW);
    }

    @Override
    public String getDescription() {
        return ((timestampProvider.getNow() - lastGps) < 2000) ? "On Gps[on]" : "On Gps[off]";
    }

    @Override
    protected boolean onActivate() {
        powerUp();
        new Timer(true).scheduleAtFixedRate(new TimerTask() {

            @Override
            public void run() {
                if ((timestampProvider.getNow() - lastGps) > 2000) {
                    powerGPSDown();
                }
            }
        }, 0, 2000);
        return true;
    }

    @Override
    protected void onDeactivate() {
        powerDown();
        powerGPSDown();
    }

    private void powerDown() {
        pin.low();
    }

    private void powerGPSDown() {
        pinGps.low();
    }

    private void powerUp() {
        pin.high();
    }

    @OnRouterMessage
    public void onMessage(RouterMessage s) {
        if (s.getPayload() instanceof MsgPosition) {
            lastGps = timestampProvider.getNow();
            powerGPSUp();
        }
    }

    private void powerGPSUp() {
        pinGps.high();
    }
}
