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
			Pair<?, ?> pair = (Pair<?, ?>)x;
			if (first!=null && second!=null)
				return first.equals(pair.first) && second.equals(pair.second);
			else if (first==null && second!=null)
				return pair.first==null && second.equals(pair.second);
			else if (first!=null)
				return first.equals(pair.first) && pair.second == null;
			else
				return pair.first == null && pair.second == null;
		} else {
			return false;
		}
	}
	
	@Override
	public int hashCode() {
		return ((first!=null)?first.hashCode():0) + ((second!=null)?second.hashCode():0);
	}
 }
