package com.aboni.utils;

public class Pair<T, U> {
	public T first;
	public U second;

	public Pair(T first, U second) {
		this.first = first;
		this.second = second;
	}
	
	@Override
	public boolean equals(Object x) {
		if (x!=null && x instanceof Pair<?, ?>) {
			if (first!=null && second!=null)
				return first.equals(  ((Pair<?, ?>)(x)).first) && 
					second.equals(  ((Pair<?, ?>)(x)).second);
			else if (first==null)
				return ((Pair<?, ?>)(x)).first==null && 
						second.equals(  ((Pair<?, ?>)(x)).second);
			else if (second==null)
				return first.equals(  ((Pair<?, ?>)(x)).first) && 
						((Pair<?, ?>)(x)).second == null;
			else
				return ((Pair<?, ?>)(x)).first == null && 
						((Pair<?, ?>)(x)).second == null;
		} else {
			return false;
		}
	}
	
	@Override
	public int hashCode() {
		return ((first!=null)?first.hashCode():0) + ((second!=null)?second.hashCode():0);
	}
 }
