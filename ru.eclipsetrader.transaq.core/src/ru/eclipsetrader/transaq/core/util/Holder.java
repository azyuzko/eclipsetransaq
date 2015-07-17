package ru.eclipsetrader.transaq.core.util;

public class Holder<T1, T2> {

	T1 t1;
	T2 t2;
	
	public Holder(T1 t1, T2 t2) {
		this.t1 = t1;
		this.t2 = t2;
	}
	
	public T1 getFirst(){
		return t1;
	}
	
	public T2 getSecond() {
		return t2;
	}
	
	@Override
	public String toString() {
		return String.valueOf(t1) + " - " + String.valueOf(t2);
	}
	
	@Override
	public int hashCode() {
		return t1.hashCode() + t2.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Holder<?, ?>) {
			@SuppressWarnings("unchecked")
			Holder<T1, T2> h = (Holder<T1, T2>)obj;
			return t1.equals(h.t1) && t2.equals(h.t2);	
		} else {
			return false;
		}
		
	}

}
