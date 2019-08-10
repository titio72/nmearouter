package com.aboni.geo;

import java.util.LinkedList;
import java.util.ListIterator;

public class PositionHistory<PAYLOAD> {

	class PosAndCourse {
        PosAndCourse(GeoPositionT t, PAYLOAD payload) {
			p = t;
            prev = null;
            this.payload = payload;
		}

        PosAndCourse(GeoPositionT t, GeoPositionT previous, PAYLOAD payload) {
			p = t;
            prev = previous;
            this.payload = payload;
		}
		
		final GeoPositionT p;
        final GeoPositionT prev;
        final PAYLOAD payload;

        double getDistance() {
            if (p != null && prev != null) {
                return new Course(prev, p).getDistance();
            } else {
                return 0.0;
            }
        }

        long getTime() {
            if (p != null && prev != null) {
                return p.getTimestamp() - prev.getTimestamp();
            } else {
                return 0L;
            }
        }

        PAYLOAD getPayload() {
            return payload;
        }

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
        addPosition(p, null);
    }

    public void addPosition(GeoPositionT p, PAYLOAD payload) {
        synchronized (positions) {
            PosAndCourse newSample = positions.isEmpty() ? new PosAndCourse(p, payload) : new PosAndCourse(p, positions.getLast().p, payload);
            positions.add(newSample);
            setTotalTime(getTotalTime() + newSample.getTime());
            setTotalDistance(getTotalDistance() + newSample.getDistance());
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
		synchronized (positions) {
			long t1 = 0L;
			if (positions.size() >= 2) {
				GeoPositionT p0 = null;
				GeoPositionT p1 = null;
				ListIterator<PosAndCourse> i = positions.listIterator(positions.size());
				while (i.hasPrevious()) {
					GeoPositionT p = i.previous().p;
					if ( p1 == null ) {
						p1 = p;
						t1 = p1.getTimestamp();
					} else {
						if ( (p.getTimestamp() + period) < t1) {
							break;
						}
						p0 = p;
					}
				}
				return getCourse(p0, p1);
			} else {
				return null;
			}
		}
	}

    private static Course getCourse(GeoPositionT p0, GeoPositionT p1) {
		if ( p1!=null && p0!=null) {
			return new Course(p0, p1);
		} else {
			return null;
		}
	}

    public void iterate(DoWithPoint<PAYLOAD> doer) {
		synchronized (positions) {
			if (doer!=null) {
				for (PosAndCourse position : positions) {
                    doer.doWithPoint(position.p, position.getPayload());
				}
                doer.finish();
			}
		}
	}

    public interface DoWithPoint<P> {
        void doWithPoint(GeoPositionT p, P payload);

        void finish();
	}
}
