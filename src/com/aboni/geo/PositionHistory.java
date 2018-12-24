package com.aboni.geo;

import java.util.LinkedList;
import java.util.ListIterator;

public class PositionHistory {

	class PosAndCourse {
		public PosAndCourse(GeoPositionT t) {
			p = t;
		}
		
		public PosAndCourse(GeoPositionT t, GeoPositionT previous) {
			p = t;
			c = new Course(previous, t);
		}
		
		final GeoPositionT p;
		Course c;
	}
	
	private final LinkedList<PosAndCourse> positions;
	private final int max;
	private long totalTime;
	private double totalDistance;
	
	public PositionHistory() {
		this(10000);
	}
	
	public PositionHistory(int maxSamples) {
		positions = new LinkedList<>();
		max = maxSamples;
	}
	
	public void addPosition(GeoPositionT p) {
		synchronized (positions) {
			if (positions.isEmpty()) {
				positions.add(new PosAndCourse(p));
			} else {
				setTotalTime(getTotalTime() + p.getTimestamp() - positions.getLast().p.getTimestamp());
				positions.add(new PosAndCourse(p, positions.getLast().p));
				setTotalDistance(getTotalDistance() + positions.getLast().c.getDistance());
			}
			
			if (max>0 && positions.size() > max) {
				positions.removeFirst();
			}
		}
	}
	
	public void reset() {
		positions.clear();
		setTotalDistance(0.0);
		setTotalTime(0);
	}

	/**
	 * @return the totalTime
	 */
	public long getTotalTime() {
		return totalTime;
	}

	private void setTotalTime(long totalTime) {
		this.totalTime = totalTime;
	}

	/**
	 * @return the totalDistance
	 */
	public double getTotalDistance() {
		return totalDistance;
	}

	private void setTotalDistance(double totalDistance) {
		this.totalDistance = totalDistance;
	}
	
	/**
	 * Get the course for the last period. Passing the period X (in milliseconds) it will calculate the course from X up to now.
	 * @param period Last X milliseconds
	 * @return The course data.
	 */
	public Course getCourse(long period) {
		return getCourse(period, System.currentTimeMillis());
	}

	/**
	 * Get the course for the last period. Passing the period X (in milliseconds) it will calculate the course from X up to now.
	 * @param period Last X milliseconds
	 * @param now 	 timestamp reference (use current time for current course)
	 * @return The course data.
	 */
	public Course getCourse(long period, long now) {
		synchronized (positions) {
			if (positions.size() >= 2) {
				GeoPositionT p0 = null;
				GeoPositionT p1 = null;
				ListIterator<PosAndCourse> i = positions.listIterator(positions.size());
				while (i.hasPrevious()) {
					GeoPositionT p = i.previous().p;
					if ( p1 == null ) {
						p1 = p;
						if (now==-1) {
							now = p1.getTimestamp();
						}
					} else { 
						if ( (p.getTimestamp() + period) < now) 
							break;
						else
							p0 = p;
					}
				}
				if ( p1!=null && p0!=null) {
					return new Course(p0, p1);
				} else {
					return null;
				}
			} else {
				return null;
			}
		}
	}
	
	public GeoPositionT getLastKnownPosition() {
		synchronized (positions) {
			if (positions.size() > 0) {
				return positions.getLast().p;
			} else {
				return null;
			}
		}
	}
	
	public Course getLastKnownCourse() {
		synchronized (positions) {
			if (positions.size() > 0) {
				return positions.getLast().c;
			} else {
				return null;
			}
		}
	}
	
	public void iterate(DoWithPoint doer) {
		synchronized (positions) {
			if (doer!=null) {
				for (PosAndCourse position : positions) {
					doer.doWithPoint(position.p);
				}
			}
		}
	}
	
	public interface DoWithPoint {
		void doWithPoint(GeoPositionT p);
	}
}
