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

	private CPUTemp cpuTemp;
	private Sample temp; 
	private Timer timer;
	private static final int READ_THRESHOLD = 5000;
	private static final int TIMER = 1000;
	private static final double FAN_THRESHOLD = 65.0;
    private Fan fan;
    
	public FanAgent(String name, QOS qos) {
		super(name, qos);
		cpuTemp = new CPUTemp();
		temp = new Sample(0, 0);
		timer = null;
		fan = new Fan();
	}
	
	private double getTemp() {
		synchronized (this) {
			try {
				long t = System.currentTimeMillis();
				if (temp.getAge(t)>READ_THRESHOLD) {
					temp = new Sample(t, cpuTemp.read());
				}
				return temp.getValue();
			} catch (Exception e) {
				ServerLog.getLogger().Error("Error reading cpu temperature", e);
			}
			return 0.0;
		}
	}
	
	@Override
	public String getDescription() {
		return "CPU Temp " + getTemp() + "° Fan " + (fan.isFanOn()?"On":"Off") ;
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
		fan(getTemp()>FAN_THRESHOLD);
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
