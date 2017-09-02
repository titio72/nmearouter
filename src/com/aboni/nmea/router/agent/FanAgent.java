package com.aboni.nmea.router.agent;

import java.util.Timer;
import java.util.TimerTask;

import com.aboni.nmea.router.NMEACache;
import com.aboni.nmea.router.NMEAStream;
import com.aboni.nmea.router.agent.impl.NMEAAgentImpl;
import com.aboni.sensors.hw.CPUTemp;
import com.aboni.sensors.hw.Fan;
import com.aboni.utils.HWSettings;

import net.sf.marineapi.nmea.sentence.Sentence;

public class FanAgent extends NMEAAgentImpl {

	private Timer timer;
	private static final int TIMER = 1000;
	private static final double FAN_THRESHOLD_ON = 55.0;
	private static final double FAN_THRESHOLD_OFF = 52.0;
    private Fan fan;
    
	public FanAgent(NMEACache cache, NMEAStream stream, String name, QOS qos) {
		super(cache, stream, name, qos);
		setSourceTarget(false, false);
		timer = null;
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
		if (timer==null) {
			timer = new Timer(true);
			timer.scheduleAtFixedRate(new TimerTask() {
				
				@Override
				public void run() {
					onTimer();
				}
			}, 0, TIMER);
		}
		return true;
	}

	protected void onTimer() {
		double temp = CPUTemp.getInstance().getTemp();
		if (fan.isFanOn() && temp<getThresholdOff()) fan(false);
		else if (!fan.isFanOn() && temp>getThresholdOn()) fan(true);

	}
	
	private double getThresholdOff() {
		return HWSettings.getPropertyAsDouble("fan.off", FAN_THRESHOLD_OFF);
	}
	
	private double getThresholdOn() {
		return HWSettings.getPropertyAsDouble("fan.on", FAN_THRESHOLD_ON);
	}

	@Override
	protected void onDeactivate() {
		if (timer!=null) {
			fan(false);
			timer.cancel();
			timer.purge();
		}
		timer = null;
	}
	
	private void fan(boolean on) {
		fan.switchFan(on);
	}
	
    @Override
    public boolean isUserCanStartAndStop() {
    	return false;
    }

}
