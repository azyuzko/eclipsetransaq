package ru.eclipsetrader.transaq.core.trades;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;

import org.apache.commons.collections4.queue.CircularFifoQueue;

import ru.eclipsetrader.transaq.core.data.DataManager;
import ru.eclipsetrader.transaq.core.event.MassObserver;
import ru.eclipsetrader.transaq.core.instruments.TQInstrumentService;
import ru.eclipsetrader.transaq.core.library.TransaqLibrary;
import ru.eclipsetrader.transaq.core.model.TQSymbol;
import ru.eclipsetrader.transaq.core.model.internal.Tick;
import ru.eclipsetrader.transaq.core.model.internal.TickTrade;
import ru.eclipsetrader.transaq.core.server.command.SubscribeCommand;
import ru.eclipsetrader.transaq.core.server.command.SubscribeTicks;
import ru.eclipsetrader.transaq.core.services.ITQTickTradeService;

public class TQTickTradeService implements ITQTickTradeService, Closeable {
	
	public static int GET_ALL_TICKS_TIMEOUT = 1000 * 10;
	
	// храним последние 1000 тиков
	CircularFifoQueue<String> knownTicks = new CircularFifoQueue<>(1000);
	
	SubscribeTicks subscribeTicks = new SubscribeTicks();
	ArrayBlockingQueue<List<TickTrade>> queue = new ArrayBlockingQueue<>(50);
	
	Thread dbWriteThread = new Thread(new Runnable() {
		@Override
		public void run() {
			while (!Thread.interrupted()) {
				try {
					List<TickTrade> list = queue.take();
					DataManager.mergeList(list);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	});
			
	static TQTickTradeService instance;
	public static TQTickTradeService getInstance() {
		if (instance == null) {
			instance = new TQTickTradeService();
		}
		return instance;
	}
	
	/**
	 * “иковые данные (в т.ч. дневна€ истори€ тиков)
	 */
	MassObserver<TickTrade> ticksObserver = new MassObserver<TickTrade>() {
		@Override
		public void update(List<TickTrade> list) {
			putInQueue(list);
			Map<TQSymbol, List<TickTrade>> map = createMap(list);
			for (TQSymbol symbol : map.keySet()) {
				TQInstrumentService.getInstance().getITickObserver().update(symbol, map.get(symbol));
			}
		}
	};

	
	/**
	 *  –аскладывает список тиков по бумагам
	 * @return
	 */
	private Map<TQSymbol, List<TickTrade>> createMap(List<TickTrade> list) {
		HashMap<TQSymbol, List<TickTrade>> map = new HashMap<>();
		for (TickTrade temp : list) {
			TQSymbol symbol = new TQSymbol(temp.getBoard(), temp.getSeccode());
			List<TickTrade> tempList = null;
			if (map.containsKey(symbol)) {
				tempList = map.get(symbol);
			} else {
				tempList = new ArrayList<TickTrade>();
				map.put(symbol, tempList);
			}
			tempList.add(temp);				
		}
		return map;
	}
	
	public void putInQueue(List<TickTrade> list) {
		try {
			queue.put(list);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public MassObserver<TickTrade> getTickObserver() {
		return ticksObserver;
	}
	
	public TQTickTradeService() {
		dbWriteThread.start();
	}
	
	@Override
	public void close() {
		dbWriteThread.interrupt();
	}

	@Override
	public void subscribeAllTrades(TQSymbol symbol) {
		SubscribeCommand cmd = new SubscribeCommand();
		cmd.subscribeAllTrades(symbol);
		TransaqLibrary.SendCommand(cmd.createSubscribeCommand());
	}

	@Override
	public void unsubscribeAllTrades(TQSymbol symbol) {
		SubscribeCommand cmd = new SubscribeCommand();
		cmd.subscribeAllTrades(symbol);
		TransaqLibrary.SendCommand(cmd.createUnsubscribeCommand());
	}
	
	@Override
	public void subscribeTicks(TQSymbol symbol) {
		subscribeTicks.addSubscription(new TickSubscription(symbol, "1"));
		TransaqLibrary.SendCommand(subscribeTicks.createCommand());
	}

	@Override
	public void unsubscribeTicks(TQSymbol symbol) {
		subscribeTicks.removeSubscription(symbol);
		TransaqLibrary.SendCommand(subscribeTicks.createCommand());
	}

	@Override
	public List<Tick> getCurrentDayTickData(TQSymbol symbol) {
		// System.out.println("Ticks size = " + ticks.values());
		return new ArrayList<>(null);
	}

}
