package com.aboni.utils;

public class Pair<T, U> {
	public final T first;
	public final U second;

	public Pair(T first, U second) {
		this.first = first;
		this.second = second;
	}
	
	@Override
	public boolean equals(Object x) {
		if (x instanceof Pair<?, ?>) {
			Pair<?, ?> _x = (Pair<?, ?>)x;
			if (first!=null && second!=null)
				return first.equals(_x.first) && second.equals(_x.second);
			else if (first==null && second!=null)
				return _x.first==null && second.equals(_x.second);
			else if (first!=null)
				return first.equals(_x.first) && _x.second == null;
			else
				return _x.first == null && _x.second == null;
		} else {
			return false;
		}
	}
	
	@Override
	public int hashCode() {
		return ((first!=null)?first.hashCode():0) + ((second!=null)?second.hashCode():0);
	}
 }
