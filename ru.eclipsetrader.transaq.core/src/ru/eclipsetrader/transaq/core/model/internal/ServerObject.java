package ru.eclipsetrader.transaq.core.model.internal;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;


/**
 * Interface for objects, linked with server
 * @author Zyuzko-AA
 *
 */
@MappedSuperclass
public abstract class ServerObject extends SessionObject {
		
	@Column(name="SERVER")
	String server;

	public ServerObject(String serverId) {
		this.server = serverId;
	}

	public String getServer() {
		return server;
	}
	
	public void setServer(String value) {
		this.server = value;
	}

}
