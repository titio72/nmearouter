package com.aboni.nmea.router.agent.impl;

import com.aboni.misc.Utils;
import com.aboni.nmea.router.NMEACache;
import com.aboni.sensors.SensorException;
import com.aboni.sensors.SensorVoltage;
import net.sf.marineapi.nmea.parser.SentenceFactory;
import net.sf.marineapi.nmea.sentence.SentenceId;
import net.sf.marineapi.nmea.sentence.TalkerId;
import net.sf.marineapi.nmea.sentence.XDRSentence;
import net.sf.marineapi.nmea.util.Measurement;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;

public class NMEAVoltageSensor extends NMEAAgentImpl {

    private int errCounter;
    private SensorVoltage voltageSensor;

    @Inject
    public NMEAVoltageSensor(@NotNull NMEACache cache) {
        super(cache);
        setSourceTarget(true, false);
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
                    getLogger().errorForceStacktrace("Voltage Sensor: Error initializing voltage sensor ", e);
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
            return new SensorVoltage();
        } catch (Exception e) {
            getLogger().errorForceStacktrace("Voltage Sensor: Error creating voltage sensor ", e);
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
            getLogger().errorForceStacktrace("Voltage Sensor: Error reading voltage data", e);
            errCounter++;
            if (errCounter == 10) {
                getLogger().warning("Voltage Sensor: Max error count reached - deactivating");
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
                notify(xdr);
            } catch (Exception e) {
                getLogger().error("Voltage Sensor: error posting voltage values", e);
            }
        }
    }


}
