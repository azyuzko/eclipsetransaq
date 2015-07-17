package ru.eclipsetrader.transaq.core.data;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class DBEventNotifier extends Thread {

	static BlockingQueue<XMLDataEvent> readDataQueue = new ArrayBlockingQueue<XMLDataEvent>(
			10000);
	
	//public Event<XMLDataEvent> eventNotifier = new Event<XMLDataEvent>("DBEventNotifier.eventNotifier");

	public static void putNotifyEvent(XMLDataEvent event) {
		try {
			readDataQueue.put(event);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		/*try {
			while (!Thread.interrupted()) {
				XMLDataEvent event = readDataQueue.take();
				eventNotifier.notifyObservers(event);
			}
		} catch (InterruptedException e) {
			readDataQueue.clear()
	
		}*/
	}
}
