package com.aboni.nmea.router.agent.impl.system;

import com.aboni.nmea.router.NMEACache;
import com.aboni.nmea.router.agent.QOS;
import com.aboni.nmea.router.agent.impl.NMEAAgentImpl;
import com.pi4j.io.gpio.*;
import net.sf.marineapi.nmea.sentence.PositionSentence;
import net.sf.marineapi.nmea.sentence.Sentence;

import java.util.Timer;
import java.util.TimerTask;

public class PowerLedAgent extends NMEAAgentImpl {

	private static final Pin GPS = RaspiPin.GPIO_23;
	private static final Pin PWR = RaspiPin.GPIO_02;
    private final GpioPinDigitalOutput pin;
    private final GpioPinDigitalOutput pinGps;
    private long lastGps;

    public PowerLedAgent(NMEACache cache, String name, QOS qos) {
        super(cache, name, qos);
        lastGps = 0;
        GpioController gpio = GpioFactory.getInstance();
        pin = gpio.provisionDigitalOutputPin(PWR, "pwr", PinState.LOW);
        pinGps = gpio.provisionDigitalOutputPin(GPS, "gps", PinState.LOW);
        pin.setShutdownOptions(true, PinState.LOW);
        pinGps.setShutdownOptions(true, PinState.LOW);
        setSourceTarget(false, true);
    }

    @Override
    public String getDescription() {
        return ((getCache().getNow() - lastGps) < 2000) ? "On Gps[on]" : "On Gps[off]";
    }
    
    @Override
    protected boolean onActivate() {
        powerUp();
        new Timer(true).scheduleAtFixedRate(new TimerTask() {
            
            @Override
            public void run() {
                if ((getCache().getNow() - lastGps) > 2000) {
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
    
    @Override
    protected void doWithSentence(Sentence s, String source) {
        if (s instanceof PositionSentence) {
            lastGps = getCache().getNow();
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
