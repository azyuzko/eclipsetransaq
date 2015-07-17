package ru.eclipsetrader.transaq.core.model.internal;

import java.util.Date;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import ru.eclipsetrader.transaq.core.model.ConnectionStatus;

@Entity
@Table(name = "server_session")
@Access(AccessType.PROPERTY)
public class ServerSession extends SessionObject {

	Date connected;
	Date disconnected;
	String serverId;
	String timezone;
	String error;

	@Enumerated(EnumType.STRING)
	ConnectionStatus status = ConnectionStatus.DISCONNECTED;
	
	public ServerSession() {
		super();
	}
	
	public ServerSession(String serverId) {
		super();
		setServerId(serverId);
	}

	@Id
	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Access(AccessType.FIELD)
	public Date getConnected() {
		return connected;
	}

	public void setConnected(Date connected) {
		this.connected = connected;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Access(AccessType.FIELD)
	public Date getDisconnected() {
		return disconnected;
	}

	public void setDisconnected(Date disconnected) {
		this.disconnected = disconnected;
	}

	public String getTimezone() {
		return timezone;
	}

	public void setTimezone(String timezone) {
		this.timezone = timezone;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	public String getServerId() {
		return serverId;
	}

	public void setServerId(String serverId) {
		this.serverId = serverId;
	}

	public ConnectionStatus getStatus() {
		return status;
	}

	public void setStatus(ConnectionStatus status) {
		this.status = status;
	}

}
