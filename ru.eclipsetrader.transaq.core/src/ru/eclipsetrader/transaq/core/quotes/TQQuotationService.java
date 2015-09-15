package ru.eclipsetrader.transaq.core.quotes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.eclipsetrader.transaq.core.data.DatabaseManager;
import ru.eclipsetrader.transaq.core.event.ListObserver;
import ru.eclipsetrader.transaq.core.feed.TQDataFeed;
import ru.eclipsetrader.transaq.core.library.TransaqLibrary;
import ru.eclipsetrader.transaq.core.model.TQSymbol;
import ru.eclipsetrader.transaq.core.model.internal.SymbolGapMap;
import ru.eclipsetrader.transaq.core.server.command.SubscribeCommand;
import ru.eclipsetrader.transaq.core.services.ITQQuotationService;

public class TQQuotationService implements ITQQuotationService {

	static TQQuotationService instance;
	public static TQQuotationService getInstance() {
		if (instance == null) {
			instance = new TQQuotationService();
		}
		return instance;
	}
		
	private TQQuotationService() {
	}
	
	ListObserver<SymbolGapMap> quotationGapObserver = new ListObserver<SymbolGapMap>() {
		
		@Override
		public void update(List<SymbolGapMap> quotationGapList) {
			Map<TQSymbol, List<SymbolGapMap>> gapMap = createMap(quotationGapList); // разложим гэпы по инструментам
			gapMap.forEach((symbol, gap) -> TQDataFeed.getInstance().getQuotationGapsFeeder().notifyObservers(symbol, gap));
			DatabaseManager.writeQuotations(quotationGapList);
		}
	};
	
	
	public ListObserver<SymbolGapMap> getQuotationGapObserver() {
		return quotationGapObserver;
	}
	
	// TODO refactor java 8
	public static Map<TQSymbol, List<SymbolGapMap>> createMap(List<SymbolGapMap> quotationGapList) {
		Map<TQSymbol, List<SymbolGapMap>> result = new HashMap<>();
		for (SymbolGapMap symbolGapMap : quotationGapList) {
			TQSymbol key = new TQSymbol(symbolGapMap.getBoard(), symbolGapMap.getSeccode());
			List<SymbolGapMap> list = result.get(key);
			if (list == null) {
				list = new ArrayList<SymbolGapMap>();
				result.put(key, list);
			}
			list.add(symbolGapMap);
		}
		return result;
	}

	@Override
	public void subscribe(TQSymbol symbol) {
		SubscribeCommand subscribeCommand = new SubscribeCommand();
		subscribeCommand.subscribeQuotations(symbol);
		TransaqLibrary.SendCommand(subscribeCommand.createConnectCommand());
		
	}
	
	public void subscribe(List<TQSymbol> symbols) {
		SubscribeCommand subscribeCommand = new SubscribeCommand();
		symbols.forEach(symbol -> subscribeCommand.subscribeQuotations(symbol));
		TransaqLibrary.SendCommand(subscribeCommand.createConnectCommand());
		
	}

	@Override
	public void unsubscribe(TQSymbol symbol) {
		SubscribeCommand subscribeCommand = new SubscribeCommand();
		subscribeCommand.subscribeQuotations(symbol);
		TransaqLibrary.SendCommand(subscribeCommand.createUnsubscribeCommand());
	}

}
