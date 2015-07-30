package ru.eclipsetrader.transaq.core.quotes;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.eclipsetrader.transaq.core.data.DataManager;
import ru.eclipsetrader.transaq.core.event.ListObserver;
import ru.eclipsetrader.transaq.core.instruments.TQInstrumentService;
import ru.eclipsetrader.transaq.core.library.TransaqLibrary;
import ru.eclipsetrader.transaq.core.model.QuotationStatus;
import ru.eclipsetrader.transaq.core.model.TQSymbol;
import ru.eclipsetrader.transaq.core.model.TradingStatus;
import ru.eclipsetrader.transaq.core.model.internal.Quotation;
import ru.eclipsetrader.transaq.core.model.internal.SymbolGapMap;
import ru.eclipsetrader.transaq.core.server.command.SubscribeCommand;
import ru.eclipsetrader.transaq.core.services.ITQQuotationService;
import ru.eclipsetrader.transaq.core.util.Utils;

public class TQQuotationService implements ITQQuotationService {

	Map<TQSymbol, Quotation> quotations = new HashMap<TQSymbol, Quotation>();

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
			for (TQSymbol symbol : gapMap.keySet()) {
				TQInstrumentService.getInstance().getQuotationGapListEvent().notifyObservers(symbol, gapMap.get(symbol));
			}
			DataManager.batchQuotationGapList(quotationGapList);
		}
	};
	
	public List<Quotation> getQuotations() {
		return new ArrayList<Quotation>(quotations.values());
	}
	
	
	public ListObserver<SymbolGapMap> getQuotationGapObserver() {
		return quotationGapObserver;
	}
	
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
		for (TQSymbol symbol : symbols) {
			subscribeCommand.subscribeQuotations(symbol);
		}
		TransaqLibrary.SendCommand(subscribeCommand.createConnectCommand());
		
	}

	@Override
	public void unsubscribe(TQSymbol symbol) {
		SubscribeCommand subscribeCommand = new SubscribeCommand();
		subscribeCommand.subscribeQuotations(symbol);
		TransaqLibrary.SendCommand(subscribeCommand.createUnsubscribeCommand());
	}
	
	public static void applyQuotationGap(List<SymbolGapMap> quotationGapList, Quotation quotation) {
		for (SymbolGapMap gapMap : quotationGapList) {

			@SuppressWarnings("rawtypes")
			Class quotationclass = quotation.getClass();
			
			for (String attr : gapMap.keySet()) {
				Object value = null;
				String stringValue = gapMap.get(attr);
				
				
				if ("point_cost".equals(attr)||
				"open".equals(attr)||
				"waprice".equals(attr)||
				"bid".equals(attr)||
				"offer".equals(attr)||
				"last".equals(attr)||
				"change".equals(attr)||
				"priceminusprevwaprice".equals(attr)||
				"valtoday".equals(attr)||
				"yield".equals(attr)||
				"yieldatwaprice".equals(attr)||
				"marketpricetoday".equals(attr)||
				"highbid".equals(attr)||
				"lowoffer".equals(attr)||
				"high".equals(attr)||
				"low".equals(attr)||
				"closeprice".equals(attr)||
				"closeyield".equals(attr)||
				"buydeposit".equals(attr)||
				"selldeposit".equals(attr)||
				"volatility".equals(attr)||
				"theoreticalprice".equals(attr)) value = Double.valueOf(stringValue);
					
				else if ("accruedintvalue".equals(attr)||
				"biddepth".equals(attr)||
				"numbids".equals(attr)||
				"offerdepth".equals(attr)||
				"offerdeptht".equals(attr)||
				"numoffers".equals(attr)||				
				"numtrades".equals(attr)||				
				"voltoday".equals(attr)||				
				"openpositions".equals(attr)||				
				"deltapositions".equals(attr)||				
				"quantity".equals(attr)) value = Integer.valueOf(stringValue);
					
				else if ("time".equals(attr))  {
					if (stringValue.length() > 12) { // определяем дату или время
						value = Utils.parseDate(stringValue);
					} else {
						value = Utils.parseTime(stringValue);
					}
				}
					
				else if ("status".equals(attr))	value = QuotationStatus.valueOf(stringValue);
	
				else if ("tradingstatus".equals(attr)) {
					switch (stringValue.toCharArray()[0]) {
					case '0': case '1': case '2': case '3': case '4': stringValue = "_" + stringValue;
					}
					value = TradingStatus.valueOf(stringValue);
				}
				
				else 
					continue;
	
				try {
					Field f = quotationclass.getDeclaredField(attr);
					if (!f.isAccessible()) {
						f.setAccessible(true);
					}
					f.set(quotation, value);
				} catch (Exception e) {
					e.printStackTrace();
					return;
				}
			}
		}
	}

}
