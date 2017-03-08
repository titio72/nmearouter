package com.aboni.nmea.router.agent;

import java.util.Timer;
import java.util.TimerTask;

import com.aboni.misc.Sample;
import com.aboni.nmea.router.impl.NMEAAgentImpl;
import com.aboni.sensors.hw.CPUTemp;
import com.aboni.sensors.hw.Fan;
import com.aboni.utils.ServerLog;

import net.sf.marineapi.nmea.sentence.Sentence;

public class FanAgent extends NMEAAgentImpl {

	private Timer timer;
	private static final int TIMER = 1000;
	private static final double FAN_THRESHOLD_ON = 55.0;
	private static final double FAN_THRESHOLD_OFF = 52.0;
    private Fan fan;
    
	public FanAgent(String name, QOS qos) {
		super(name, qos);
		timer = null;
		fan = new Fan();
	}
	
	@Override
	public String getDescription() {
		return "CPU Temp " + CPUTemp.getTemp() + "° Fan " + (fan.isFanOn()?"On":"Off") + " [" + FAN_THRESHOLD_OFF + "/" + FAN_THRESHOLD_ON + "]";
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
		double temp = CPUTemp.getTemp();
		if (fan.isFanOn() && temp<FAN_THRESHOLD_OFF) fan(false);
		else if (!fan.isFanOn() && temp>FAN_THRESHOLD_ON) fan(true);

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
}
