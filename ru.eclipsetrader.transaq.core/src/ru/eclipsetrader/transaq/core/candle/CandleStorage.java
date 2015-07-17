package ru.eclipsetrader.transaq.core.candle;

import java.util.Date;
import java.util.Set;
import java.util.TreeMap;

import ru.eclipsetrader.transaq.core.interfaces.ITQTickTrade;
import ru.eclipsetrader.transaq.core.model.Candle;

public class CandleStorage  {

	private TreeMap<CandleType, CandleList> storage = new TreeMap<>(CandleType.comparator);

	public CandleStorage() {
		
	}
	
	public CandleStorage(CandleType[] candleTypes) {
		for (CandleType candleType : candleTypes) {
			storage.put(candleType, new CandleList(candleType));
		}
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
	
	public void processTrade(ITQTickTrade trade) {
		for (CandleList candleList : storage.values()) {
			Candle lastCandle = candleList.processTradeInCandle(trade);
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
