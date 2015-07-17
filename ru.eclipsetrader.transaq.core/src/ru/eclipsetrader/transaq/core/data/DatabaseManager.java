package ru.eclipsetrader.transaq.core.data;

import java.sql.DriverManager;

import oracle.jdbc.driver.OracleConnection;
import ru.eclipsetrader.transaq.core.Settings;
import ru.eclipsetrader.transaq.core.data.XMLDataEvent.Direction;

public class DatabaseManager  {

	public static ThreadGroup dbThreadGroup = new ThreadGroup("DB thread group");

	
	static OracleConnection connection = null;
	static WriteDBThread writeThread;
	static ReadDBThread readThread;
	static DBEventNotifier notifyEventThread;

	public static OracleConnection getConnection()
	{
		try {
			if (connection == null) {
				Class.forName("oracle.jdbc.OracleDriver");
				connection = (OracleConnection) DriverManager.getConnection(
				        Settings.DB_CONNECTION_STRING,
				        Settings.DB_USERNAME,
				        Settings.DB_PASSWORD);
			}
			return connection;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public DatabaseManager() {

	}
	
	public static void writeInputEvent(String sessionId, String data) {
		XMLDataEvent event = new XMLDataEvent();
		event.setData(data);
		event.setDirection(Direction.IN);
		event.setEventTime(new java.sql.Timestamp(System.currentTimeMillis()));
		event.setSessionId(sessionId);
		writeEvent(event);
	}
	
	public static void writeOutputEvent(String data) {
		XMLDataEvent event = new XMLDataEvent();
		event.setData(data);
		event.setDirection(Direction.OUT);
		event.setEventTime(new java.sql.Timestamp(System.currentTimeMillis()));
		//TODO refactoring event.setSessionId(TransaqServer.INSTANCE.getServerSession().getSessionId());
		writeEvent(event);
	}
	
	public static void writeEvent(XMLDataEvent event) {
		if (getConnection() == null) {
			System.err.println("Database not available.");
			return;
		}
		
		if (writeThread == null) {
			writeThread = new WriteDBThread();
		}
		
		writeThread.putEvent(event);
	}

	
	/*public static void suscribeReadEvent(String sessionId, Observer<XMLDataEvent> observer) {
		if (readThread == null) {
			readThread = new ReadDBThread(sessionId);
		}
		if (notifyEventThread == null) {
			notifyEventThread = new DBEventNotifier();
		}
		notifyEventThread.eventNotifier.addObserver(observer);
	}*/
	
	
}
