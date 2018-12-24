package com.aboni.nmea.router.agent.impl.system;

import com.aboni.nmea.router.NMEACache;
import com.aboni.nmea.router.agent.NMEAAgent;
import com.aboni.nmea.router.agent.QOS;
import com.aboni.nmea.router.agent.impl.NMEAAgentImpl;
import com.aboni.sensors.hw.CPUTemp;
import com.aboni.sensors.hw.Fan;
import com.aboni.utils.HWSettings;

import net.sf.marineapi.nmea.sentence.Sentence;

public class FanAgent extends NMEAAgentImpl {

	private static final double FAN_THRESHOLD_ON = 55.0;
	private static final double FAN_THRESHOLD_OFF = 52.0;
    private final Fan fan;
    
	public FanAgent(NMEACache cache, String name, QOS qos) {
		super(cache, name, qos);
		setSourceTarget(false, false);
		fan = new Fan();
	}
	
	@Override
	public String getDescription() {
		return "CPU Temp " + CPUTemp.getInstance().getTemp() + "C° Fan " + (fan.isFanOn()?"On":"Off") + 
				" [" + getThresholdOff() + "/" + getThresholdOn() + "]";
	}

	@Override
	protected void doWithSentence(Sentence s, NMEAAgent source) {
	}

	@Override
	protected boolean onActivate() {
		return true;
	}

	@Override
	public void onTimer() {
		if (isStarted()) {
			double temp = CPUTemp.getInstance().getTemp();
			if (fan.isFanOn() && temp<getThresholdOff()) fan(false);
			else if (!fan.isFanOn() && temp>getThresholdOn()) fan(true);
		}
	}
	
	private double getThresholdOff() {
		return HWSettings.getPropertyAsDouble("fan.off", FAN_THRESHOLD_OFF);
	}
	
	private double getThresholdOn() {
		return HWSettings.getPropertyAsDouble("fan.on", FAN_THRESHOLD_ON);
	}

	@Override
	protected void onDeactivate() {
	}
	
	private void fan(boolean on) {
		getLogger().Info("Switch fan {" + on + "} RPi {" + fan.isRaspberry() + "}");
		fan.switchFan(on);
	}
	
    @Override
    public boolean isUserCanStartAndStop() {
    	return false;
    }

}
