package ru.eclipsetrader.transaq.core.candle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ArrayBlockingQueue;

import ru.eclipsetrader.transaq.core.Constants;
import ru.eclipsetrader.transaq.core.data.DataManager;
import ru.eclipsetrader.transaq.core.event.Observer;
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
	
	ArrayBlockingQueue<CandleGraph> dbWriteQueue = new ArrayBlockingQueue<>(300);
	
	Thread dbWriteThread = new Thread(new Runnable() {
		@Override
		public void run() {
			while (!Thread.interrupted()) {
				try {
					CandleGraph candleGraph = dbWriteQueue.take();
					TQSymbol symbol = new TQSymbol(candleGraph.getBoard(), candleGraph.getSeccode());
					persist(symbol, getCandleTypeFromPeriodId(candleGraph.getPeriod()), candleGraph.getCandles());
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	});

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
	
	
	//Object candlesWait = new Object();
	// List<Candle> candleBuffer = Collections.synchronizedList(new ArrayList<Candle>());
	
	WeakHashMap<TQSymbol, List<Candle>> buffer = new WeakHashMap<>();
	
	Observer<CandleGraph> candleGraphObserver = new Observer<CandleGraph>() {
		@Override
		public void update(CandleGraph candleGraph) {
			TQSymbol symbol = new TQSymbol(candleGraph.getBoard(), candleGraph.getSeccode());
			if (buffer.containsKey(symbol)) {
				List<Candle> candleBuffer = buffer.get(symbol);
				candleBuffer.addAll(candleGraph.getCandles());
				System.out.println("candleGraph.getCandles().size = " + candleGraph.getCandles().size() + " status = " + candleGraph.getStatus());
				if (candleGraph.getStatus() == CandleStatus.NO_MORE || candleGraph.getStatus() == CandleStatus.FULL_PROVIDED) {
					synchronized (candleBuffer) {
						System.out.println("Notify waiters");
						candleBuffer.notify();	
					}				
				}
			} else {
				throw new RuntimeException("Unable to find buffer for " + symbol);
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
	
	public List<Candle> getHistoryData(TQSymbol symbol, CandleType candleType, int count) {
		return getHistoryData(symbol, candleType, count, true);
	}
	
	// получим свечи
	public List<Candle> getHistoryData(TQSymbol symbol, CandleType candleType, int count, boolean reset) {		
		System.out.println("Calling getHistoryData for <" + symbol + ">");
		GetHistoryDataCommand getHistoryDataCommand = new GetHistoryDataCommand();
		getHistoryDataCommand.setBoard(symbol.getBoard());
		getHistoryDataCommand.setSeccode(symbol.getSeccode());
		getHistoryDataCommand.setPeriodId(getCandleKindByType(candleType).getId());
		getHistoryDataCommand.setCandleCount(count);
		getHistoryDataCommand.setReset(reset);
		TransaqLibrary.SendCommand(getHistoryDataCommand.createConnectCommand());
		
		if (buffer.containsKey(symbol)) {
			throw new RuntimeException("Already waiting for history data for " + symbol);
		}
		
		// создадим буфер
		List<Candle> candleBuffer = Collections.synchronizedList(new ArrayList<Candle>());
		buffer.put(symbol, candleBuffer);
		
		synchronized (candleBuffer) {
			try {
				candleBuffer.wait(100000);
			} catch (InterruptedException e) {
				throw new RuntimeException("Candles hasn't been received within 100 seconds", e);
			} finally {
				// освободим буфер
				buffer.remove(symbol);
			}
			return candleBuffer;
		}
	}

	@Override
	public void persist(TQSymbol symbol, CandleType candleType,
			List<Candle> candles) {
		DataManager.batchCandles(symbol, candleType, candles);		
	}

	@Override
	public List<Candle> getSavedCandles(TQSymbol symbol,
			CandleType candleType, Date fromDate, Date toDate) {
		return DataManager.getCandles(symbol, candleType, fromDate, toDate);
	}

}
