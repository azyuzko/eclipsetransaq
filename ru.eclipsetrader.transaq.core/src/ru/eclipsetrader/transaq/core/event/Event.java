package ru.eclipsetrader.transaq.core.event;

import java.lang.Thread.State;
import java.util.Vector;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Event<T> implements Runnable {

	public static int QUEUE_SIZE = 3000;
	
	Logger logger;
	Vector<Observer<T>> obs = new Vector<Observer<T>>();
	BlockingQueue<T> notifyObjectQueue = new ArrayBlockingQueue<T>(QUEUE_SIZE);
	Thread thread;
	
	String name;

	public static ThreadGroup eventThreadGroup = new ThreadGroup("Event threads");
	
	public Event(String name) {
		this.name = name;
		this.logger = LogManager.getFormatterLogger("Event."+name);
		this.thread = new Thread(eventThreadGroup, this);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void run() {
		try {
			logger.entry("run");
			while (!Thread.interrupted()) {
				// ждем новый объект
				T obj = notifyObjectQueue.take();
				
				if (logger.isDebugEnabled()) {
					logger.debug("Queue <" + name  + "> received object %s from queue", obj.getClass().getSimpleName());
				}
				
				if (notifyObjectQueue.size() > (QUEUE_SIZE/3))  {
					logger.warn("Queue <" + name  + "> size = " + notifyObjectQueue.size() + ". Maximum size = " + QUEUE_SIZE);
				}

				Object[] arrayOfObservers;
				synchronized (this) {
					arrayOfObservers = obs.toArray();
				}
				for (int i = arrayOfObservers.length - 1; i >= 0; --i) {
					Observer<T> observer = ((Observer<T>) arrayOfObservers[i]);
					if (logger.isDebugEnabled()) {
						logger.debug("Queue <" + name  + "> start update observer %d: class = %s", i, observer.getClass().getName());
					}
					observer.update(obj);
					if (logger.isDebugEnabled()) {
						logger.debug("Queue <" + name  + "> end update observer %d: class = %s", i, observer.getClass().getName());
					}
				}

			}
		} catch (InterruptedException e) {
			notifyObjectQueue.clear();
			logger.error("Queue <%s> Thread was interrupted. Object queue flushed", name);
		}
		logger.exit();
	}

	public synchronized void addObserver(Observer<T> paramObserver) {
		logger.entry("addObserver");
		if (paramObserver == null)
			throw new NullPointerException();
		if (obs.contains(paramObserver))
			return;
		obs.addElement(paramObserver);
		logger.exit();
	}

	public synchronized void deleteObserver(Observer<T> paramObserver) {
		if (logger.isDebugEnabled()) {
			logger.debug("Queue <" + name  + "> deleteObserver = " + paramObserver);
		}
		obs.removeElement(paramObserver);
	}

	public void notifyObservers() {
		notifyObservers(null);
	}

	public void notifyObservers(T paramObject) {
		synchronized (obs) {
			if (obs.size() == 0) {
				logger.debug("No observers");
				logger.exit();
				return;
			}
		}

		synchronized (thread) {
			if (thread.getState() == State.NEW) {
				if (logger.isDebugEnabled()) {
					logger.debug("starting event thread");
				}
				thread.setDaemon(true);
				thread.start();
			}
		}

		try {
			if (notifyObjectQueue.remainingCapacity() == 0) {
				String error = String.format("Queue <" + name  + "> is full! obs.size = %d, notifyObjectQueue.size = %d, eventThreadGroup.activeCount() = %d, paramObject.class = %s "
						, obs.size(), notifyObjectQueue.size(), thread.getThreadGroup().activeCount(), paramObject.getClass().getName());
				throw new RuntimeException(error);
			}

			notifyObjectQueue.put(paramObject);
		} catch (InterruptedException e) {
			logger.throwing(e);
		}

	}

	public synchronized void deleteObservers() {
		logger.debug("Queue <" + name  + "> deleteObservers");
		obs.removeAllElements();
	}

	public synchronized int countObservers() {
		return obs.size();
	}

}
