package ru.eclipsetrader.transaq.core.event;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Event<T>  {
	
	public static int QUEUE_SIZE = 30;
	public static int POOL_SIZE = 5;

	Logger logger;
	Vector<Observer<T>> obs = new Vector<Observer<T>>();
	
	String name;

	List<NotificationThread> threads = new ArrayList<NotificationThread>(POOL_SIZE);
	
	class NotificationThread extends Thread {
		
		BlockingQueue<T> notifyObjectQueue = new ArrayBlockingQueue<T>(QUEUE_SIZE);

		public BlockingQueue<T> getNotifyObjectQueue() {
			return notifyObjectQueue;
		}
		
		public NotificationThread() {
			super();
			setDaemon(true);
		}

		@SuppressWarnings("unchecked")
		@Override
		public void run() {
			try {
				while (!Thread.interrupted()) {
					// ждем новый объект
					T obj = notifyObjectQueue.take();
					
					if (logger.isDebugEnabled()) {
						logger.debug("Queue <" + name  + "> received object %s (%d)  from queue", obj.getClass().getSimpleName(), obj.hashCode());
					}
					
					if (notifyObjectQueue.size() > (QUEUE_SIZE/2))  {
						logger.warn("Queue <" + name  + "> size = " + notifyObjectQueue.size() + ". Maximum size = " + QUEUE_SIZE);
					}

					Object[] arrayOfObservers;
					synchronized (this) {
						arrayOfObservers = obs.toArray();
					}
					for (int i = arrayOfObservers.length - 1; i >= 0; --i) {
						Observer<T> observer = ((Observer<T>) arrayOfObservers[i]);
						long start = System.currentTimeMillis();
						if (logger.isDebugEnabled()) {
							logger.debug("Queue <" + name  + "> update observer %d: class = %s", i, observer.getClass().getName());
						}
						observer.update(obj);
						if (logger.isDebugEnabled()) {
							long end = System.currentTimeMillis();
							logger.debug("Queue <" + name  + "> end update observer %d: class = %s completed in %d ms", i, observer.getClass().getName(), (end - start));
						}
					}

				}
			} catch (InterruptedException e) {
				notifyObjectQueue.clear();
				logger.error("Queue <%s> Thread was interrupted. Object queue flushed", name);
			}

		}
	};

	
	public Event(String name) {
		this.name = name;
		this.logger = LogManager.getFormatterLogger("Event."+name);
		
		for (int i = 0; i < POOL_SIZE; i++) {
			NotificationThread thread = new NotificationThread();
			threads.add(thread);
			thread.start();
		}
		
	}

	public synchronized void addObserver(Observer<T> paramObserver) {
		if (paramObserver == null)
			throw new NullPointerException();
		if (obs.contains(paramObserver))
			return;
		logger.info("Queue <" + name + "> adding observer " + paramObserver);
		obs.addElement(paramObserver);
	}

	public synchronized void deleteObserver(Observer<T> paramObserver) {
		logger.info("Queue <" + name  + "> deleteObserver = " + paramObserver);
		obs.removeElement(paramObserver);
	}

	public void notifyObservers() {
		notifyObservers(null);
	}

	// round robin algorithm
	int queueIndex = 0;
	public void notifyObservers(T paramObject) {
		synchronized (obs) {
			if (obs.size() == 0) {
				logger.debug("No observers");
				return;
			}
		}

		try {
			
			NotificationThread thread = threads.get(queueIndex);
			
			int remainingCapacity = thread.getNotifyObjectQueue().remainingCapacity();
			if (remainingCapacity == 0) {
				String error = String.format("Queue <" + name  + "> is full! obs.size = %d, notifyObjectQueue.size = %d, paramObject.class = %s "
						, obs.size(), thread.getNotifyObjectQueue().size(), paramObject.getClass().getName());
				logger.error(error);
				// throw new RuntimeException(error);
			} else {
				if (remainingCapacity < QUEUE_SIZE/2) {
					logger.warn("Queue <" + name + "> remaining capacity " + remainingCapacity);
				}
			}

			thread.getNotifyObjectQueue().put(paramObject);
			if (logger.isDebugEnabled()) {
				logger.debug("Queue <" + name  + "> complete put notification in queue");
			}
		} catch (InterruptedException e) {
			logger.throwing(e);
		} finally {
			queueIndex++;
			if (queueIndex > threads.size()-1) {
				queueIndex = 0;
			}
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
