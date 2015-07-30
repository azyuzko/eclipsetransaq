package ru.eclipsetrader.transaq.core.trades;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;

import ru.eclipsetrader.transaq.core.data.DataManager;
import ru.eclipsetrader.transaq.core.event.ListObserver;
import ru.eclipsetrader.transaq.core.instruments.TQInstrumentService;
import ru.eclipsetrader.transaq.core.library.TransaqLibrary;
import ru.eclipsetrader.transaq.core.model.TQSymbol;
import ru.eclipsetrader.transaq.core.model.internal.Tick;
import ru.eclipsetrader.transaq.core.model.internal.TickTrade;
import ru.eclipsetrader.transaq.core.server.command.SubscribeCommand;
import ru.eclipsetrader.transaq.core.server.command.SubscribeTicks;
import ru.eclipsetrader.transaq.core.services.ITQTickTradeService;

public class TQTickTradeService implements ITQTickTradeService {
	
	SubscribeTicks subscribeTicks = new SubscribeTicks();
	ArrayBlockingQueue<List<TickTrade>> queue = new ArrayBlockingQueue<>(300);
	
	Thread dbWriteThread = new Thread(new Runnable() {
		@Override
		public void run() {
			while (!Thread.interrupted()) {
				try {
					List<TickTrade> list = queue.take();
					DataManager.batchTickList(list);
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
	 * ������� ������ (� �.�. ������� ������� �����)
	 */
	ListObserver<TickTrade> ticksObserver = new ListObserver<TickTrade>() {
		@Override
		public void update(List<TickTrade> list) {
			putInQueue(list);
			Map<TQSymbol, List<Tick>> map = createMap(list);
			for (TQSymbol symbol : map.keySet()) {
				TQInstrumentService.getInstance().getDefaultTickListEvent().notifyObservers(symbol, map.get(symbol));
			}
		}
	};

	
	/**
	 *  ������������ ������ ����� �� �������
	 * @return
	 */
	public static Map<TQSymbol, List<Tick>> createMap(List<TickTrade> list) {
		HashMap<TQSymbol, List<Tick>> map = new HashMap<>();
		for (TickTrade temp : list) {
			TQSymbol symbol = new TQSymbol(temp.getBoard(), temp.getSeccode());
			List<Tick> tempList = null;
			if (map.containsKey(symbol)) {
				tempList = map.get(symbol);
			} else {
				tempList = new ArrayList<Tick>();
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
	
	public ListObserver<TickTrade> getTickObserver() {
		return ticksObserver;
	}
	
	public TQTickTradeService() {
		dbWriteThread.setDaemon(true);
		dbWriteThread.start();
	}

	@Override
	public void subscribeAllTrades(TQSymbol symbol) {
		SubscribeCommand cmd = new SubscribeCommand();
		cmd.subscribeAllTrades(symbol);
		TransaqLibrary.SendCommand(cmd.createConnectCommand());
	}
	
	public void subscribeAllTrades(List<TQSymbol> symbols) {
		SubscribeCommand cmd = new SubscribeCommand();
		for (TQSymbol symbol : symbols) {
			cmd.subscribeAllTrades(symbol);
		}
		TransaqLibrary.SendCommand(cmd.createConnectCommand());
	}

	@Override
	public void unsubscribeAllTrades(TQSymbol symbol) {
		SubscribeCommand cmd = new SubscribeCommand();
		cmd.subscribeAllTrades(symbol);
		TransaqLibrary.SendCommand(cmd.createUnsubscribeCommand());
	}
	
	@Override
	public void subscribeTicks(TQSymbol symbol) {
		subscribeTicks.addSubscription(new TickSubscription(symbol));
		TransaqLibrary.SendCommand(subscribeTicks.createCommand());
	}
	
	public void subscribeTicks(List<TQSymbol> symbols) {
		for (TQSymbol symbol : symbols) {
			subscribeTicks.addSubscription(new TickSubscription(symbol));
		}
		TransaqLibrary.SendCommand(subscribeTicks.createCommand());
	}

	@Override
	public void unsubscribeTicks(TQSymbol symbol) {
		subscribeTicks.removeSubscription(symbol);
		TransaqLibrary.SendCommand(subscribeTicks.createCommand());
	}

	@Override
	public List<Tick> getCurrentDayTickData(TQSymbol symbol) {
		return new ArrayList<>(null);
	}

}
