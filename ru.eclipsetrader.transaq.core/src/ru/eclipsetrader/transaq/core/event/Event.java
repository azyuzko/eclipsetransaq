package ru.eclipsetrader.transaq.core.event;

import java.lang.Thread.State;
import java.util.Vector;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Event<T> implements Runnable {

	Logger logger;
	Vector<Observer<T>> obs = new Vector<Observer<T>>();
	BlockingQueue<T> notifyObjectQueue = new ArrayBlockingQueue<T>(3000);
	Thread thread;
	
	String name;

	public Event(String name, ThreadGroup eventThreadGroup) {
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
					logger.debug("received object %s from queue", obj.getClass().getSimpleName());
				}

				Object[] arrayOfObservers;
				synchronized (this) {
					arrayOfObservers = obs.toArray();
				}
				for (int i = arrayOfObservers.length - 1; i >= 0; --i) {
					Observer<T> observer = ((Observer<T>) arrayOfObservers[i]);
					if (logger.isDebugEnabled()) {
						logger.debug("start update observer %d: class = %s", i, observer.getClass().getName());
					}
					observer.update(obj);
					if (logger.isDebugEnabled()) {
						logger.debug("end update observer %d: class = %s", i, observer.getClass().getName());
					}
				}

			}
		} catch (InterruptedException e) {
			notifyObjectQueue.clear();
			/*if (TransaqServer.getInstance() != null && TransaqServer.getInstance().getStatus() != ConnectionStatus.DISCONNECTED
					&& TransaqServer.getInstance().getStatus() != ConnectionStatus.DISCONNECTING) {
				logger.error("Thread %s was interrupted. Object queue flushed", this.getClass().getSimpleName());
			}*/
			logger.error("Thread %s was interrupted. Object queue flushed", this.getClass().getSimpleName());
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
		obs.removeElement(paramObserver);
	}

	public void notifyObservers() {
		logger.entry("notifyObservers");
		notifyObservers(null);
		logger.exit();
	}

	public void notifyObservers(T paramObject) {
		logger.entry("notifyObservers");
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
				thread.start();
			}
		}

		try {
			if (notifyObjectQueue.remainingCapacity() == 0) {
				String error = String.format("Event queue is full! obs.size = %d, notifyObjectQueue.size = %d, eventThreadGroup.activeCount() = %d, paramObject.class = %s "
						, obs.size(), notifyObjectQueue.size(), thread.getThreadGroup().activeCount(), paramObject.getClass().getName());
				/*logger.error(error);
				logger.exit();*/
				throw new RuntimeException(error);
			}

			notifyObjectQueue.put(paramObject);
		} catch (InterruptedException e) {
			logger.throwing(e);
		}
		logger.exit();

	}

	public synchronized void deleteObservers() {
		obs.removeAllElements();
	}

	public synchronized int countObservers() {
		return obs.size();
	}

}
