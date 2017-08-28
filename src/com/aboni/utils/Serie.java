package com.aboni.utils;

public abstract class Serie {
	private int id;
	private String tag;
	protected double avg;
    protected double max;
    protected double min;
    protected int samples = 0;
    
    public String getTag() { return tag; }
	public int getId() { return id; }
	public int getSamples() { return samples; }
	public double getAvg() { return avg; }
	public double getMin() { return min; }
	public double getMax() { return max; }
	
	protected Serie(int id, String tag) {
		this.id = id;
		this.tag = tag;
		reset();
	}
	
	public void reset() {
        avg = Double.NaN;
        max = Double.NaN;
        min = Double.NaN;
        samples = 0;
	}
	
	public abstract void add(double v);

	public abstract String getType();
}
