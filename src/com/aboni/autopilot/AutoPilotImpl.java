package com.aboni.autopilot;

public class AutoPilotImpl implements Autopilot {

	private Heading target;
	private AutoPilotQoS qos;

	private static double MAGIC_NO = 1.0;
	
	private BoatData data;
	
	public AutoPilotImpl(AutoPilotQoS qos) {
		this.qos = qos;
		this.data = new BoatData();
	}
	
	@Override
	public void setHeading(Heading h, double speed, long timestamp) {
		if (h != null) {
			data.setHeading(h, speed, timestamp);
		}
	}
	
	public Heading getLastHeading() {
		return data.getLastHeading();
	}
	
	public double getLastSpeed() {
		return data.getLastSpeed();
	}

	@Override
	public void setRudder(Rudder r, long timestamp) {
		if (r != null) {
			data.setRudder(r, timestamp);
		}
	}	
	
	public Rudder getLastRudder() {
		return data.getLastRudder();
	}
	
	@Override
	public void setCOG(Heading h, double sog, long timestamp) {
		if (h != null) {
			data.setCOG(h, sog, timestamp);
		}

	}

	@Override
	public void setCorrectionListener(HeadingListener listener) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setTarget(Heading h) {
		target = h;
	}

	@Override
	public Heading getTarget() {
		return target;
	}
	
	public Rudder calcCorrection() {
		Heading diff = target.diff(data.getLastHeading());
		double age = Math.max(1, System.currentTimeMillis() - data.getLastHeadingTimeStamp());

		double correction = 
				(qos.getReactiveness() / 5.0) /* reactiveness factor - the more reactive the more intense is the correction*/ 
				* diff.getAngle()  /* of course the difference between heading and target drives the correction*/
				* (1.0 - 0.5/age) /* put a weight for how much time you spend on a wrong heading */
				* MAGIC_NO;

		return getCorrectionRudder(correction);
	}

	private Rudder getCorrectionRudder(double correction) {
		Rudder newRudder;
		
		if ( correction > qos.getMaxRudder().getAngle() ) {
			newRudder = qos.getMaxRudder();
		} else if ( correction < qos.getMinRudder().getAngle() ) {
			newRudder = qos.getMinRudder();
		} else {
			newRudder = new Rudder(correction);
		}
		
		return newRudder;
	}
	
	public static void main(String[] args) {
		
		AutoPilotQoS q = new AutoPilotQoS();
		q.setReactiveness(3);
		AutoPilotImpl p = new AutoPilotImpl(q);
		
		p.setTarget(Heading.fromDegree(180));
		
		long now = System.currentTimeMillis();
		p.setRudder(Rudder.fromDegree(0), now);
		p.setHeading(Heading.fromDegree(140), 5, now);
		
		
		System.out.println(p.calcCorrection().toDegree());
	}

}
