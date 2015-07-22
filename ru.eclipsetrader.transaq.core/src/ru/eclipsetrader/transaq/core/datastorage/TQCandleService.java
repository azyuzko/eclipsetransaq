package ru.eclipsetrader.transaq.core.datastorage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ru.eclipsetrader.transaq.core.Constants;
import ru.eclipsetrader.transaq.core.candle.CandleType;
import ru.eclipsetrader.transaq.core.data.DataManager;
import ru.eclipsetrader.transaq.core.event.Observer;
import ru.eclipsetrader.transaq.core.interfaces.ITQSymbol;
import ru.eclipsetrader.transaq.core.library.TransaqLibrary;
import ru.eclipsetrader.transaq.core.model.Candle;
import ru.eclipsetrader.transaq.core.model.TQSymbol;
import ru.eclipsetrader.transaq.core.model.internal.CandleGraph;
import ru.eclipsetrader.transaq.core.model.internal.CandleKind;
import ru.eclipsetrader.transaq.core.model.internal.CandleStatus;
import ru.eclipsetrader.transaq.core.server.command.GetHistoryDataCommand;
import ru.eclipsetrader.transaq.core.services.ITQCandleService;

public class TQCandleService implements ITQCandleService {
	
	Set<CandleKind> candleKinds = new HashSet<CandleKind>();

	static TQCandleService instance;
	public static TQCandleService getInstance() {
		if (instance == null) {
			instance = new TQCandleService();
			instance.load(Constants.DEFAULT_SERVER_ID);
		}
		return instance;
	}
	
	private TQCandleService() {
	}
	
	Observer<List<CandleKind>> candleKindObserver = new Observer<List<CandleKind>>() {
		@Override
		public void update(List<CandleKind> candleKindList) {
			candleKinds.addAll(candleKindList);
		}
	}; 
	
	
	Object candlesWait = new Object();
	List<Candle> candleBuffer = Collections.synchronizedList(new ArrayList<Candle>());
	Observer<CandleGraph> candleGraphObserver = new Observer<CandleGraph>() {
		@Override
		public void update(CandleGraph candleGraph) {
			candleBuffer.addAll(candleGraph.getCandles());
			System.out.println("candleGraph.getCandles().size = " + candleGraph.getCandles().size() + " status = " + candleGraph.getStatus());
			if (candleGraph.getStatus() == CandleStatus.NO_MORE || candleGraph.getStatus() == CandleStatus.FULL_PROVIDED) {
				synchronized (candlesWait) {
					System.out.println("Notify waiters");
					candlesWait.notify();	
				}				
			}
		}
	};
	
	public Observer<List<CandleKind>> getCandleKindObserver() {
		return candleKindObserver;
	}
	
	public Observer<CandleGraph> getCandleGraphObserver() {
		return candleGraphObserver;
	}

	@Override
	public List<CandleType> getCandleTypes() {
		List<CandleType> result = new ArrayList<CandleType>();
		for (CandleKind candleKind : candleKinds) {
			result.add(getCandleTypeByKind(candleKind));
		}
		return result;
	}

	@Override
	public void persist() {
		DataManager.mergeList(candleKinds);
	}

	@Override
	public void load(String serverId) {
		candleKinds.addAll(DataManager.getList(CandleKind.class));
	}

	public CandleType getCandleTypeByKind(CandleKind candleKind) {
		return CandleType.fromSeconds(candleKind.getPeriod());
	}
	
	public CandleKind getCandleKindByType(CandleType candleType) {
		for (CandleKind candleKind : candleKinds) {
			if (candleKind.getPeriod() == candleType.getSeconds()) {
				return candleKind;
			}
		}
		throw new RuntimeException("Candletype = " + candleType + " NOT found!");
	}
	
	public CandleType getCandleTypeFromPeriodId(Integer periodId) {	
		for (CandleKind candleKind : candleKinds) {
			if (candleKind.getId() == periodId) {
				return CandleType.fromSeconds(candleKind.getPeriod());
			}
		}
		throw new IllegalArgumentException("CandleType for periodId = " + periodId + " not found!");
	}
	
	public List<Candle> getHistoryData(ITQSymbol security, CandleType candleType, int count) {
		return getHistoryData(security, candleType, count, true);
	}
	
	// получим свечи
	public List<Candle> getHistoryData(ITQSymbol security, CandleType candleType, int count, boolean reset) {		
		System.out.println("Calling getHistoryData for <" + TQSymbol.symbolKey(security) + ">");
		GetHistoryDataCommand getHistoryDataCommand = new GetHistoryDataCommand();
		getHistoryDataCommand.setBoard(security.getBoard());
		getHistoryDataCommand.setSeccode(security.getSeccode());
		getHistoryDataCommand.setPeriodId(getCandleKindByType(candleType).getId());
		getHistoryDataCommand.setCandleCount(count);
		getHistoryDataCommand.setReset(reset);
		TransaqLibrary.SendCommand(getHistoryDataCommand.createConnectCommand());
		// опчистим буфер
		candleBuffer.clear();
		
		synchronized (candlesWait) {
			try {
				candlesWait.wait(100000);
				return candleBuffer;
			} catch (InterruptedException e) {
				throw new RuntimeException("Candles hasn't been received due 10 seconds", e);
			}
		}
	}

	@Override
	public void persist(TQSymbol symbol, CandleType candleType,
			List<Candle> candles) {
		DataManager.batchCandles(symbol, candleType, candles);		
	}

}
