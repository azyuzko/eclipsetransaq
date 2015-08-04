package ru.eclipsetrader.transaq.core.model.internal;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class CandleKind extends ServerObject {
	
	public CandleKind(){
		this(null);
	}

	public CandleKind(String serverId) {
		super(serverId);
	}

	@Id
	Integer id;
	int period; // in sec
	String name;
	
	public String getKey() {
		return String.valueOf(id);
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public int getPeriod() {
		return period;
	}

	public void setPeriod(int period) {
		this.period = period;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
}
