package ru.eclipsetrader.transaq.core.quotes;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.eclipsetrader.transaq.core.event.Observer;
import ru.eclipsetrader.transaq.core.model.QuotationStatus;
import ru.eclipsetrader.transaq.core.model.TQSymbol;
import ru.eclipsetrader.transaq.core.model.TradingStatus;
import ru.eclipsetrader.transaq.core.model.internal.Quotation;
import ru.eclipsetrader.transaq.core.model.internal.SymbolGapMap;
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
	
	Observer<SymbolGapMap> quotationGapObserver = new Observer<SymbolGapMap>() {
		
		@Override
		public void update(SymbolGapMap quotationGap) {
			applyQuotationGap(quotationGap);
		}
	};
	
	public List<Quotation> getQuotations() {
		return new ArrayList<Quotation>(quotations.values());
	}
	
	
	public Observer<SymbolGapMap> getQuotationGapObserver() {
		return quotationGapObserver;
	}

	@Override
	public void subscribe(TQSymbol symbol) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void unsubscribe(TQSymbol symbol) {
		// TODO Auto-generated method stub
		
	}
	
	public void applyQuotationGap(SymbolGapMap gapMap) {
		TQSymbol symbol = new TQSymbol(gapMap.getBoard(), gapMap.getSeccode());
		
		Quotation quotation = null;
		if (quotations.containsKey(symbol)) {
			quotation = quotations.get(symbol);
		} else {
			quotation = new Quotation();
			quotation.setBoard(gapMap.getBoard());
			quotation.setSeccode(gapMap.getSeccode());
			quotations.put(symbol, quotation);
		}
				
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
				
			else if ("time".equals(attr)) value = Utils.parseDate(stringValue);
				
			else if ("status".equals(attr))	value = QuotationStatus.valueOf(stringValue);

			else if ("tradingstatus".equals(attr)) {
				switch (stringValue.toCharArray()[0]) {
				case 0: case 1: case 2: case 3: case 4: stringValue = "_" + stringValue;
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
