package ru.eclipsetrader.transaq.core.data;

import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import oracle.jdbc.driver.OracleConnection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ru.eclipsetrader.transaq.core.Settings;
import ru.eclipsetrader.transaq.core.model.Quote;
import ru.eclipsetrader.transaq.core.model.XMLDataEvent;
import ru.eclipsetrader.transaq.core.model.XMLDataEvent.Direction;
import ru.eclipsetrader.transaq.core.model.internal.SymbolGapMap;
import ru.eclipsetrader.transaq.core.model.internal.TickTrade;
import ru.eclipsetrader.transaq.core.util.Utils;

public class DatabaseManager  {

	// Logger logger = LogManager.getLogger("DatabaseManager");
	
	public static ThreadGroup dbThreadGroup = new ThreadGroup("DB thread group");
	
	public static final int DB_QUEUE_SIZE = 200;
	public static final int DB_XMLDATA_QUEUE_SIZE = 1000;
	
	static ArrayList<List<TickTrade>> tickTradeQueue = new ArrayList<>(DB_QUEUE_SIZE);
	static ArrayList<List<Quote>> quoteQueue = new ArrayList<List<Quote>>(DB_QUEUE_SIZE);
	static ArrayList<List<SymbolGapMap>> quotationQueue = new ArrayList<List<SymbolGapMap>>(DB_QUEUE_SIZE);
	static ArrayList<XMLDataEvent> xmlDataQueue = new ArrayList<XMLDataEvent>(DB_XMLDATA_QUEUE_SIZE);
	
	static class TicksWriteThread extends Thread {
		Logger logger = LogManager.getLogger("DatabaseManager.TicksWriteThread");
		public TicksWriteThread() {
			setName("Ticks DB write thread");
			setDaemon(true);
		}
		@Override
		public void run() {
			while (!Thread.interrupted()) {
				try {
					
					List<TickTrade> buffer = new ArrayList<TickTrade>();

					synchronized (tickTradeQueue) {
						
						tickTradeQueue.wait();
						
						if (tickTradeQueue.size() > DB_QUEUE_SIZE)  {
							logger.warn("Queue became too big, current size = " + tickTradeQueue.size());
						}
	
						Iterator<List<TickTrade>> it = tickTradeQueue.iterator();
						List<TickTrade> list;
						while (it.hasNext()) {
							list = it.next();
							if (logger.isDebugEnabled()) {
								logger.debug("taken list size = " + list.size() + ", full size of queue = " + tickTradeQueue.size());
							}
							buffer.addAll(list);
							it.remove();
						}
					}

					if (buffer.size() > 0) {
						if (logger.isDebugEnabled()) {
							Date startDate = buffer.get(0).getTime();
							Date endDate = buffer.get(buffer.size()-1).getTime();
							logger.debug("Taken list from tick queue. Buffer size = " + buffer.size() 
									+ " from " + Utils.formatDate(startDate) + " to " + Utils.formatDate(endDate) + ". Queue length = " + tickTradeQueue.size());
						}
						DataManager.batchTickList(buffer);
						Thread.sleep(1000);
					} else {
						logger.warn("Something wrong - empty ticks buffer!");
					}

				} catch (InterruptedException e) {
					logger.throwing(e);
					e.printStackTrace();
				}
			}
		}
	}
	
	static class QuotesWriteThread extends Thread {
		Logger logger = LogManager.getLogger("DatabaseManager.QuotesWriteThread");
		public QuotesWriteThread() {
			setName("Quotes DB write thread");
			setDaemon(true);
		}
		@Override
		public void run() {
			while (!Thread.interrupted()) {
				try {
					List<Quote> buffer = new ArrayList<Quote>();
					
					synchronized (quoteQueue) {
						
						quoteQueue.wait();
						
						if (quoteQueue.size() > DB_QUEUE_SIZE)  {
							logger.warn("Queue became too big, current size = " + quoteQueue.size());
						}
												
						Iterator<List<Quote>> it = quoteQueue.iterator();
						List<Quote> list;
						while (it.hasNext()) {
							list = it.next();
							if (logger.isDebugEnabled()) {
								logger.debug("taken list size = " + list.size() + ", full size of queue = " + quoteQueue.size());
							}
							buffer.addAll(list);
							it.remove();
						}
					}
					
					
					if (buffer.size() > 0) {
						if (logger.isDebugEnabled()) {
							Date startDate = buffer.get(0).getTime();
							Date endDate = buffer.get(buffer.size()-1).getTime();
							logger.debug("Taken list from quote queue. Buffer size = " + buffer.size()
									+ " from " + Utils.formatDate(startDate) + " to " + Utils.formatDate(endDate) + ". Queue length = " + quoteQueue.size());
						}
						DataManager.batchQuoteList(buffer);
						Thread.sleep(1000);
					} else {
						logger.warn("Something wrong - empty quote buffer!");
					}

				} catch (InterruptedException e) {
					logger.throwing(e);
					e.printStackTrace();
				}
			}
		}
	}
	
	static class QuotationsWriteThread extends Thread {
		Logger logger = LogManager.getLogger("DatabaseManager.QuotationsWriteThread");
		public QuotationsWriteThread() {
			setName("Quotations DB write thread");
			setDaemon(true);
		}
		@Override
		public void run() {
			while (!Thread.interrupted()) {
				try {
					List<SymbolGapMap> buffer = new ArrayList<SymbolGapMap>();
					
					synchronized (quotationQueue) {
						
						quotationQueue.wait();
						
						if (quotationQueue.size() > DB_QUEUE_SIZE)  {
							logger.warn("Queue became too big, current size = " + quotationQueue.size());
						}
						
						Iterator<List<SymbolGapMap>> it = quotationQueue.iterator();
						List<SymbolGapMap> list;
						while (it.hasNext()) {
							list = it.next();
							if (logger.isDebugEnabled()) {
								logger.debug("taken list size = " + list.size() + ", full size of queue = " + quoteQueue.size());
							}
							buffer.addAll(list);
							it.remove();
						}
					}
						
					if (buffer.size() > 0) {
						if (logger.isDebugEnabled()) {
							Date startDate = buffer.get(0).getTime();
							Date endDate = buffer.get(buffer.size()-1).getTime();
							logger.debug("Taken list from quotation queue. List size = " + buffer.size() + " from "
									+ Utils.formatDate(startDate) + " to " + Utils.formatDate(endDate) + ". Queue length = " + quotationQueue.size());
						}
						DataManager.batchQuotationGapList(buffer);
						Thread.sleep(1000);
					} else {
						logger.warn("Something wrong - empty quotation buffer!");
					}

				} catch (InterruptedException e) {
					logger.throwing(e);
					e.printStackTrace();
				}
			}
		}
	}
	
	static class XMLDataWriteThread extends Thread {
		Logger logger = LogManager.getLogger("DatabaseManager.XMLDataWriteThread");
		public XMLDataWriteThread() {
			setName("XML data DB write thread");
			setDaemon(true);
		}
		@Override
		public void run() {
			while (!Thread.interrupted()) {
				try {
					
					List<XMLDataEvent> buffer = new ArrayList<XMLDataEvent>();

					synchronized (xmlDataQueue) {
						
						xmlDataQueue.wait();
						
						if (xmlDataQueue.size() > DB_XMLDATA_QUEUE_SIZE)  {
							logger.warn("Queue became too big, current size = " + xmlDataQueue.size());
						}
	
						Iterator<XMLDataEvent> it = xmlDataQueue.iterator();
						XMLDataEvent xml;
						while (it.hasNext()) {
							xml = it.next();
							buffer.add(xml);
							it.remove();
						}
					}

					if (buffer.size() > 0) {
						logger.debug("Write XML data buffer size = " + buffer.size());
						DataManager.batchXMLDataEventList(buffer);
					} else {
						logger.warn("Something wrong - empty xml data buffer!");
					}
					Thread.sleep(1000);

				} catch (InterruptedException e) {
					logger.throwing(e);
					e.printStackTrace();
				}
			}
		}
	}

	public static void writeTicks(List<TickTrade> list) {
		synchronized (tickTradeQueue) {
			tickTradeQueue.add(list);
			tickTradeQueue.notify();
		}
	}
	
	public static void writeQuotes(List<Quote> quotesList) {
		synchronized (quoteQueue) {
			quoteQueue.add(quotesList);
			quoteQueue.notify();			
		}
	}
	
	public static void writeQuotations(List<SymbolGapMap> list) {
		synchronized (quotationQueue) {
			quotationQueue.add(list);
			quotationQueue.notify();
		}
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
		writeEvent(event);
	}
	
	public static void writeEvent(XMLDataEvent event) {
		synchronized (xmlDataQueue) {
			xmlDataQueue.add(event);
			xmlDataQueue.notify();
		}
	}
	
	static {
		TicksWriteThread ticksWriteThread = new TicksWriteThread();
		ticksWriteThread.start();
		QuotationsWriteThread quotationsWriteThread = new QuotationsWriteThread();
		quotationsWriteThread.start();
		QuotesWriteThread quotesWriteThread = new QuotesWriteThread();
		quotesWriteThread.start();
		XMLDataWriteThread dataWriteThread = new XMLDataWriteThread();
		dataWriteThread.start();
	}
	
	
}
