package ru.eclipsetrader.transaq.core.util;

public class Holder3<T1, T2, T3> {

	T1 t1;
	T2 t2;
	T3 t3;
	
	public Holder3(T1 t1, T2 t2, T3 t3) {
		this.t1 = t1;
		this.t2 = t2;
		this.t3 = t3;
	}
	
	public T1 getFirst(){
		return t1;
	}
	
	public T2 getSecond() {
		return t2;
	}
	
	public T3 getThird() {
		return t3;
	}
	
	@Override
	public String toString() {
		return String.valueOf(t1) + " - " + String.valueOf(t2) + " - " + String.valueOf(t3);
	}
	
	@Override
	public int hashCode() {
		return t1.hashCode() + t2.hashCode() + t3.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Holder3<?, ?, ?>) {
			@SuppressWarnings("unchecked")
			Holder3<T1, T2, T3> h = (Holder3<T1, T2, T3>)obj;
			return t1.equals(h.t1) && t2.equals(h.t2) && t3.equals(h.t3);	
		} else {
			return false;
		}
		
	}

}
