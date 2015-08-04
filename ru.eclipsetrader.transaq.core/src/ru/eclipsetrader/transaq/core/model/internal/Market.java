package ru.eclipsetrader.transaq.core.model.internal;

import javax.persistence.Entity;
import javax.persistence.Id;

import ru.eclipsetrader.transaq.core.util.Utils;

@Entity
public class Market extends ServerObject {
	
	public Market() {
		super(null);
	}

	public Market(String serverId) {
		super(serverId);
	}

	@Id
	Integer id;
	String name;
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public String toString() {
		return Utils.toString(this);
	}

}
