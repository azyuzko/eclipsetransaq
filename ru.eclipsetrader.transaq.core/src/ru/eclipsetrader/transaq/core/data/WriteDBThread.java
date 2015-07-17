package ru.eclipsetrader.transaq.core.data;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import ru.eclipsetrader.transaq.core.util.XMLFormatter;
import ru.eclipsetrader.transaq.core.xml.handler.XMLProcessType;

public class WriteDBThread extends Thread {
	
	BlockingQueue<XMLDataEvent> writeDataQueue = new ArrayBlockingQueue<XMLDataEvent>(1000);

	static final String INSERT_SQL = "INSERT INTO event_audit (session_id, event_time, direction, operation, data) values (?, ?, ?, ?, ?)";
	
	PreparedStatement xmlEventPutStmt = null;
	
	static File file ;
	static FileOutputStream fos;
	
	public WriteDBThread() {
		super(DatabaseManager.dbThreadGroup, "Write DB Event");
		try {
			try {
				file = new File("C:\\2\\" + System.currentTimeMillis()+".log");
				if (!file.exists()) file.createNewFile();
				fos = new FileOutputStream(file);
			} catch (Exception e) {
				
			}
			
			xmlEventPutStmt = DatabaseManager.getConnection().prepareStatement(INSERT_SQL);
			start();
		} catch (SQLException e) {
			System.err.println("DB Write thread not started");
			e.printStackTrace();
		}
		
	}
	
	public void putEvent(XMLDataEvent event) {
		try {
			writeDataQueue.put(event);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		try {
			while (!Thread.interrupted()) {
				XMLDataEvent event = writeDataQueue.take();
				
				try {
					fos.write(event.getData().getBytes());
					fos.write(System.lineSeparator().getBytes());
					fos.flush();
				} catch (Exception e) {
					
				}
				
				if (event.getOperation() == null) {
					String data = event.getData();
					try {
						int ind1 = data.indexOf(" ");
						int ind2 = data.indexOf(">");
						int index = 0;
						if (ind1 > -1) {
							index = Math.min(ind1, ind2);
						} else {
							index = ind2;
						}
						String sub = data.substring(1, index).toUpperCase();
						event.setOperation(XMLProcessType.valueOf(sub));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				
				xmlEventPutStmt.setString(1, event.getSessionId());
				xmlEventPutStmt.setTimestamp(2, event.getEventTime());
				xmlEventPutStmt.setString(3, event.getDirection().toString());
				xmlEventPutStmt.setString(4, event.getOperation().toString());
				String data = XMLFormatter.format(event.getData());
				StringReader r = new StringReader(data);
				xmlEventPutStmt.setCharacterStream(5, r, data.length());	
				xmlEventPutStmt.execute();

			}
		} catch (InterruptedException ie) {
			writeDataQueue.clear();
			try {
				fos.close();
			} catch (IOException e) {
				
			}
			//TODO refactoring if (TransaqServer.getCurrentStatus() != ConnectionStatus.DISCONNECTED && TransaqServer.getCurrentStatus() != ConnectionStatus.DISCONNECTING) {
			//	ie.printStackTrace();
			//}
		} catch (SQLException  se) {
			se.printStackTrace();
		} /*catch (IOException ioe) {
			ioe.printStackTrace();
		}*/
	}


}
