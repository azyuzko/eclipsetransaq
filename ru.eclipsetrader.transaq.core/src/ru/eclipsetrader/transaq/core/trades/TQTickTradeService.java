package ru.eclipsetrader.transaq.core.trades;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.eclipsetrader.transaq.core.data.DatabaseManager;
import ru.eclipsetrader.transaq.core.event.ListObserver;
import ru.eclipsetrader.transaq.core.instruments.TQDataFeed;
import ru.eclipsetrader.transaq.core.library.TransaqLibrary;
import ru.eclipsetrader.transaq.core.model.TQSymbol;
import ru.eclipsetrader.transaq.core.model.internal.Tick;
import ru.eclipsetrader.transaq.core.model.internal.TickTrade;
import ru.eclipsetrader.transaq.core.server.command.SubscribeCommand;
import ru.eclipsetrader.transaq.core.server.command.SubscribeTicks;
import ru.eclipsetrader.transaq.core.services.ITQTickTradeService;

public class TQTickTradeService implements ITQTickTradeService {
	
	SubscribeTicks subscribeTicks = new SubscribeTicks();
			
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
	ListObserver<TickTrade> ticksObserver = new ListObserver<TickTrade>() {
		@Override
		public void update(List<TickTrade> list) {
			DatabaseManager.writeTicks(list);
			Map<TQSymbol, List<Tick>> map = createMap(list);
			for (TQSymbol symbol : map.keySet()) {
				TQDataFeed.getInstance().getTicksFeeder().notifyObservers(symbol, map.get(symbol));
			}
		}
	};

	
	/**
	 *  –аскладывает список тиков по бумагам
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
	
	
	
	public ListObserver<TickTrade> getTickObserver() {
		return ticksObserver;
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
