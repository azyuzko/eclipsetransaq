package ru.eclipsetrader.transaq.core.candle;

import java.util.Date;
import java.util.Set;
import java.util.TreeMap;

import ru.eclipsetrader.transaq.core.instruments.Instrument;
import ru.eclipsetrader.transaq.core.interfaces.IProcessingContext;
import ru.eclipsetrader.transaq.core.model.Candle;
import ru.eclipsetrader.transaq.core.model.internal.Tick;

public class CandleStorage  {
	
	IProcessingContext context;
	Instrument instrument;
	private TreeMap<CandleType, CandleList> storage = new TreeMap<>(CandleType.comparator);

	public CandleStorage(Instrument instrument, IProcessingContext context) {
		this.instrument = instrument;
		this.context = context;
	}
	
	public CandleStorage(CandleType[] candleTypes) {
		for (CandleType candleType : candleTypes) {
			storage.put(candleType, new CandleList(candleType));
		}
	}
	
	
	/**
	 * Создает пустой список свечей
	 * @param candleType
	 * @return
	 */
	public CandleList createCandleTypeList(CandleType candleType) {
		for (CandleType ct :getCandleTypes()) {
			if (ct == candleType) {
				throw new RuntimeException("Candle type " + candleType + "already exists");
			}
		}
		CandleList result = new CandleList(candleType);
		addCandleList(result);
		return result;
	}
	
	/**
	 * Удаляет головы всех списоков свечей до даты включительно
	 * @param toDate
	 */
	public void truncateCandlesHead(Date toDate) {
		for (CandleType candleType : storage.keySet()) {
			CandleList candleList = storage.get(candleType);
			candleList.truncHead(toDate);
		}
	}
	
	/**
	 * Удаляет хвосты всех списоков свечей до даты включительно
	 * @param toDate
	 */
	public void truncateCandlesTail(Date toDate) {
		for (CandleType candleType : storage.keySet()) {
			CandleList candleList = storage.get(candleType);
			candleList.truncTail(toDate);
		}
	}
	
	/**
	 * Добавляем в хранилище с уже заполненным списком свечей 
	 * @param candleType
	 * @param candleList
	 */
	public void addCandleList(CandleList candleList) {
		if (!storage.containsKey(candleList.getCandleType())) {
			storage.put(candleList.getCandleType(), candleList);
		}
	}
	
	public void removeCandleType(CandleType candleType) {
		storage.remove(candleType);
	}
	
	public CandleList getCandleList(CandleType candleType) {
		return storage.get(candleType);
	}
	
	public void processTrade(Tick tick) {
		for (final CandleList candleList : storage.values()) {
			candleList.processTickInCandle(tick, new CandleList.ICandleProcessContext() {
				@Override
				public void onCandleOpen(Candle candle) {
					context.onCandleOpen(instrument, candleList, candle);
				}
				@Override
				public void onCandleClose(Candle candle) {
					context.onCandleClose(instrument, candleList, candle);
				}
				@Override
				public void onCandleChange(Candle candle) {
					context.onCandleChange(instrument, candleList, candle);
				}
			});			
		}
	}

	public CandleType[] getCandleTypes() {
		Set<CandleType> set = storage.keySet();
		return set.toArray(new CandleType[set.size()]);	
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (CandleType candleType : storage.keySet()) {
			sb.append(candleType);sb.append(":\n");
			sb.append(storage.get(candleType));
		}
		return sb.toString();
	}
}
