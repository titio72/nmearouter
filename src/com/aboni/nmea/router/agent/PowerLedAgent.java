package com.aboni.nmea.router.agent;

import java.util.Timer;
import java.util.TimerTask;

import com.aboni.nmea.router.impl.NMEAAgentImpl;
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;

import net.sf.marineapi.nmea.sentence.PositionSentence;
import net.sf.marineapi.nmea.sentence.Sentence;

public class PowerLedAgent extends NMEAAgentImpl {

    private final GpioController gpio;
    private final GpioPinDigitalOutput pin, pinGps;
    private long lastGps;

    public PowerLedAgent(String name, QOS qos) {
        super(name, qos);
        gpio = GpioFactory.getInstance();
        pin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_02, "pwr", PinState.LOW);
        pinGps = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_03, "gps", PinState.LOW);
        pin.setShutdownOptions(true, PinState.LOW);
        pinGps.setShutdownOptions(true, PinState.LOW);
        setSourceTarget(false, true);
    }

    @Override
    public String getDescription() {
    	return "";
    }
    
    @Override
    protected boolean onActivate() {
        powerUp();
        new Timer(true).scheduleAtFixedRate(new TimerTask() {
            
            @Override
            public void run() {
                if ((System.currentTimeMillis()-lastGps) > 2000) {
                    powerGPSDown();
                } else {
                    //blinkGPS();
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
    
    @Override
    protected void doWithSentence(Sentence s, NMEAAgent source) {
        if (s instanceof PositionSentence) {
            lastGps = System.currentTimeMillis();
            powerGPSUp();
        }
    }

    private void powerGPSUp() {
       pinGps.high();
    }

    @Override
    public boolean isUserCanStartAndStop() {
    	return false;
    }
}
