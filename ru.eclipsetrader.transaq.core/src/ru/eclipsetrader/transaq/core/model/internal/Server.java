package ru.eclipsetrader.transaq.core.model.internal;

import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;

import ru.eclipsetrader.transaq.core.data.BooleanConverter;
import ru.eclipsetrader.transaq.core.interfaces.ITQServer;
import ru.eclipsetrader.transaq.core.server.LogLevel;
import ru.eclipsetrader.transaq.core.server.command.BaseCommand;
import ru.eclipsetrader.transaq.core.util.Utils;

@Entity
@Table(name = "servers")
public class Server extends BaseCommand implements ITQServer {

	@Id
	String id;
	String description;

	String host;
	Integer port;
	String login;
	String password;

	String logDir;

	@Enumerated(EnumType.STRING)
	LogLevel logLevel = LogLevel.STANDARD;

	int requestDelay = 100;

	@Convert(converter = BooleanConverter.class)
	boolean autopos = false;
	@Convert(converter = BooleanConverter.class)
	boolean micex_registers = false;
	@Convert(converter = BooleanConverter.class)
	boolean milliseconds = true;
	@Convert(converter = BooleanConverter.class)
	boolean utc_time = false;
	@Convert(converter = BooleanConverter.class)
	boolean dbLogging = false;

	public String createSubscribeCommand() {
		return ("<command id=\"connect\">" + "<login>$LOGIN$</login>"
				+ "<password>$PASSWORD$</password>" + "<host>$HOST$</host>"
				+ "<port>$PORT$</port>" + "<logsdir>$LOG_DIR$</logsdir>"
				+ "<loglevel>$LOG_LEVEL$</loglevel>"
				+ "<autopos>true</autopos>"
				+ "<micex_registers>false</micex_registers>"
				+ "<milliseconds>true</milliseconds>"
				+ "<utc_time>false</utc_time>" +
				// "<proxy type=\"тип\" addr=\"адрес\" port=\"порт\" login=\"логин\"+password=\"пароль\"/>"+
				"<rqdelay>$RQ_DELAY$</rqdelay>" +
				// "<session_timeout>50</session_timeout>"+
				// "<request_timeout>30</request_timeout>"+
				"</command>")
				.replace("$LOGIN$", login)
				.replace("$PASSWORD$", password)
				.replace("$HOST$", host)
				.replace("$PORT$", String.valueOf(port))
				.replace("$LOG_DIR$", logDir)
				.replace("$LOG_LEVEL$", String.valueOf(logLevel.getLevel() - 1))
				.replace("$RQ_DELAY$", String.valueOf(requestDelay));
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public Integer getPort() {
		return port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getLogDir() {
		return logDir;
	}

	public void setLogDir(String logDir) {
		this.logDir = logDir;
	}

	public LogLevel getLogLevel() {
		return logLevel;
	}

	public void setLogLevel(LogLevel logLevel) {
		this.logLevel = logLevel;
	}

	public int getRequestDelay() {
		return requestDelay;
	}

	public void setRequestDelay(int requestDelay) {
		this.requestDelay = requestDelay;
	}

	public boolean isAutopos() {
		return autopos;
	}

	public void setAutopos(boolean autopos) {
		this.autopos = autopos;
	}

	public boolean isMicex_registers() {
		return micex_registers;
	}

	public void setMicex_registers(boolean micex_registers) {
		this.micex_registers = micex_registers;
	}

	public boolean isMilliseconds() {
		return milliseconds;
	}

	public void setMilliseconds(boolean milliseconds) {
		this.milliseconds = milliseconds;
	}

	public boolean isUtc_time() {
		return utc_time;
	}

	public void setUtc_time(boolean utc_time) {
		this.utc_time = utc_time;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isDbLogging() {
		return dbLogging;
	}

	public void setDbLogging(boolean dbLogging) {
		this.dbLogging = dbLogging;
	}

	public String toString() {
		return Utils.toString(this);
	}


}
