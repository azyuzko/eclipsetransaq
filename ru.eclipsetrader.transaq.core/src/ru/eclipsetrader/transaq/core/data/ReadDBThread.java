package ru.eclipsetrader.transaq.core.data;

import java.io.Reader;
import java.io.StringWriter;
import java.sql.Clob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import ru.eclipsetrader.transaq.core.data.XMLDataEvent.Direction;
import ru.eclipsetrader.transaq.core.xml.handler.XMLProcessType;

public class ReadDBThread extends Thread  {

	static final String SELECT_SQL = "select id, session_id, event_time, direction, operation, data "
			+ " from event_audit ea where ea.direction = ''IN'' and ea.session_id = ? and ea.id > ?";

	boolean isRunning = false;
	
	String readSessionId = null;
	
	PreparedStatement xmlEventSelectStmt = null;
	
	long last_seq_id = 0;
	
	public ReadDBThread(String sessionId) {
		super(DatabaseManager.dbThreadGroup, "Read DB Event");
		this.readSessionId = sessionId;
		try {
			xmlEventSelectStmt = DatabaseManager.getConnection().prepareStatement(SELECT_SQL);
			isRunning = true;
			start();
		} catch (SQLException e) {
			System.err.println("DB Read thread not started");
			isRunning = false;
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		while (!Thread.interrupted()) {
			try {
				xmlEventSelectStmt.setString(1, this.readSessionId);
				xmlEventSelectStmt.setLong(2, this.last_seq_id);
				ResultSet result = xmlEventSelectStmt.executeQuery();
				while (result.next()) { // process results one row at a time
					XMLDataEvent event = new XMLDataEvent();
					event.setId(result.getLong(1));
					event.setSessionId(result.getString(2));
			        event.setEventTime(result.getTimestamp(3));
			        event.setDirection(Direction.valueOf(result.getString(4)));
			        event.setOperation(XMLProcessType.valueOf(result.getString(5)));
			        Clob c = result.getClob(5);
			        Reader r = c.getCharacterStream();
			        if (r.ready()) {
			        	StringWriter sw = new StringWriter();
			        	int ch;
			        	while ((ch = r.read()) != -1) {
			        		sw.write(ch);
			        	}
			        	event.setData(sw.toString());
			        }
			        c.free();
			        this.last_seq_id = event.getId();
			       
			      }
		        Thread.sleep(300);
			} catch (Exception e) {
				System.err.println("Read thread was stopped!");
				e.printStackTrace();
				break;
			}
		}
	}
	
}
