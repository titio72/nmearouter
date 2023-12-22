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
import com.aboni.log.LogStringBuilder;
import com.aboni.nmea.router.RouterMessageFactory;
import com.aboni.sensors.SensorException;
import com.aboni.sensors.SensorVoltage;
import com.aboni.utils.TimestampProvider;
import com.aboni.utils.Utils;
import net.sf.marineapi.nmea.parser.SentenceFactory;
import net.sf.marineapi.nmea.sentence.SentenceId;
import net.sf.marineapi.nmea.sentence.TalkerId;
import net.sf.marineapi.nmea.sentence.XDRSentence;
import net.sf.marineapi.nmea.util.Measurement;

import javax.inject.Inject;

public class NMEAVoltageSensor extends NMEAAgentImpl {

    public static final String VOLTAGE_AGENT_CATEGORY = "VoltageAgent";
    private int errCounter;
    private SensorVoltage voltageSensor;

    @Inject
    public NMEAVoltageSensor(TimestampProvider tp, Log log, RouterMessageFactory messageFactory) {
        super(log, tp, messageFactory, true, false);
    }

    @Override
    public String getType() {
        return "OnBoard Voltage Sensor";
    }

    @Override
    public String toString() {
        return getType();
    }

    @Override
    public String getDescription() {
        return "Volt(" + (voltageSensor == null ? "-" : "*") + ")";
    }

    @Override
    protected boolean onActivate() {
        if (!isStarted()) {
            voltageSensor = createVoltage();
            if (voltageSensor != null) {
                try {
                    voltageSensor.init();
                    return true;
                } catch (SensorException e) {
                    getLog().errorForceStacktrace(LogStringBuilder.start(VOLTAGE_AGENT_CATEGORY).wO("init").toString(), e);
                    voltageSensor = null;
                }
            }
            return false;
        } else {
            return true;
        }
    }

    private SensorVoltage createVoltage() {
        try {
            return new SensorVoltage(getLog());
        } catch (Exception e) {
            getLog().errorForceStacktrace(LogStringBuilder.start(VOLTAGE_AGENT_CATEGORY).wO("init").toString(), e);
            return null;
        }
    }


    @Override
    public void onTimer() {
        super.onTimer();
        doLF();
    }

    @Override
    protected synchronized void onDeactivate() {
        errCounter = 0;
        voltageSensor = null;
    }

    private synchronized void doLF() {
        if (isStarted() && readSensors()) {
            sendVoltage();
        }
    }


    private boolean readSensors() {
        try {
            voltageSensor.read();
            errCounter = 0;
            return true;
        } catch (Exception e) {
            getLog().errorForceStacktrace(LogStringBuilder.start(VOLTAGE_AGENT_CATEGORY).wO("read").wV("failures", errCounter)
                    .wV("max failures", 10).toString(), e);
            errCounter++;
            if (errCounter == 10) {
                getLog().errorForceStacktrace(LogStringBuilder.start(VOLTAGE_AGENT_CATEGORY).wO("deactivate").toString(), e);
                stop();
            }
            return false;
        }
    }

    private void sendVoltage() {
        if (voltageSensor != null) {
            try {
                XDRSentence xdr = (XDRSentence) SentenceFactory.getInstance().createParser(TalkerId.II, SentenceId.XDR.toString());
                xdr.addMeasurement(new Measurement("V", Utils.round(voltageSensor.getVoltage0(), 3), "V", "V0"));
                xdr.addMeasurement(new Measurement("V", Utils.round(voltageSensor.getVoltage1(), 3), "V", "V1"));
                xdr.addMeasurement(new Measurement("V", Utils.round(voltageSensor.getVoltage2(), 3), "V", "V2"));
                xdr.addMeasurement(new Measurement("V", Utils.round(voltageSensor.getVoltage3(), 3), "V", "V3"));
                postMessage(xdr);
            } catch (Exception e) {
                getLog().errorForceStacktrace(LogStringBuilder.start(VOLTAGE_AGENT_CATEGORY).wO("message").toString(), e);
            }
        }
    }


}
