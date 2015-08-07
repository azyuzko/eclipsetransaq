package ru.eclipsetrader.transaq.core.candle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.WeakHashMap;
import java.util.concurrent.ArrayBlockingQueue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ru.eclipsetrader.transaq.core.Constants;
import ru.eclipsetrader.transaq.core.data.DataManager;
import ru.eclipsetrader.transaq.core.event.Observer;
import ru.eclipsetrader.transaq.core.library.TransaqLibrary;
import ru.eclipsetrader.transaq.core.model.TQSymbol;
import ru.eclipsetrader.transaq.core.model.internal.CandleGraph;
import ru.eclipsetrader.transaq.core.model.internal.CandleKind;
import ru.eclipsetrader.transaq.core.model.internal.CandleStatus;
import ru.eclipsetrader.transaq.core.server.command.GetHistoryDataCommand;
import ru.eclipsetrader.transaq.core.services.ITQCandleService;
import ru.eclipsetrader.transaq.core.util.Holder;
import ru.eclipsetrader.transaq.core.util.Utils;

public class TQCandleService implements ITQCandleService {
	
	Logger logger = LogManager.getLogger(TQCandleService.class);

	// Ключ - период свечи в секундах
	TreeMap<Integer, CandleKind> candleKinds = new TreeMap<Integer, CandleKind>();
	
	WeakHashMap<Holder<TQSymbol, CandleType>, List<Candle>> buffer = new WeakHashMap<>();
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
			candleKindList.clear();
			for (CandleKind ck : candleKindList) {
				candleKinds.put(ck.getPeriod(), ck);
			}
			persist();
		}
	}; 
		
	Observer<CandleGraph> candleGraphObserver = new Observer<CandleGraph>() {
		@Override
		public void update(CandleGraph candleGraph) {
			TQSymbol symbol = new TQSymbol(candleGraph.getBoard(), candleGraph.getSeccode());
			CandleType candleType = getCandleTypeFromPeriodId(candleGraph.getPeriod());
			Holder<TQSymbol, CandleType> key = new Holder<TQSymbol, CandleType>(symbol, candleType);
			if (buffer.containsKey(key)) {
				List<Candle> candleBuffer = buffer.get(key);
				candleBuffer.addAll(candleGraph.getCandles());
				
				if (logger.isDebugEnabled()) {
					logger.debug(symbol + "  " + candleType + " candleGraph.getCandles().size = " + candleGraph.getCandles().size() + " status = " + candleGraph.getStatus());
				}
				if (candleGraph.getStatus() == CandleStatus.NO_MORE || candleGraph.getStatus() == CandleStatus.FULL_PROVIDED) {
					synchronized (candleBuffer) {
						logger.debug(symbol + "  " + candleType + " notify waiters");
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
		for (CandleKind candleKind : candleKinds.values()) {
			result.add(getCandleTypeByKind(candleKind));
		}
		return result;
	}

	@Override
	public void persist() {
		DataManager.mergeList(candleKinds.values());
	}

	@Override
	public void load(String serverId) {
		for (CandleKind ck : DataManager.getList(CandleKind.class)) {
			candleKinds.put(ck.getPeriod(), ck);
		}
	}

	public CandleType getCandleTypeByKind(CandleKind candleKind) {
		return CandleType.fromSeconds(candleKind.getPeriod());
	}
	
	public CandleKind getCandleKindByType(CandleType candleType) {
		if (candleKinds.containsKey(candleType.getSeconds())) {
			return candleKinds.get(candleType.getSeconds());
		}
		return null;
	}
	
	public CandleType getCandleTypeFromPeriodId(Integer periodId) {	
		for (CandleKind candleKind : candleKinds.values()) {
			if (candleKind.getId() == periodId) {
				return CandleType.fromSeconds(candleKind.getPeriod());
			}
		}
		throw new IllegalArgumentException("CandleType for periodId = " + periodId + " not found!");
	}
	
	public List<Candle> getHistoryData(TQSymbol symbol, CandleType candleType, int count) {
		return getHistoryData(symbol, candleType, count, true);
	}
	
	public static List<Candle> convertCandleList(CandleType fromCandleType, CandleType toCandleType, List<Candle> list) {
		List<Candle> result = new ArrayList<Candle>();
		for (Candle c : list) {
			Candle last = null;
			if (result.size() > 0) {
				last = result.get(result.size()-1);
			} else {
			}
			last = new Candle();
			last.setDate(c.getDate());
			last.setOpen(c.getOpen());
		}
		return result;
	}
	
	public static void main(String[] args) {
		Date fromDate = Utils.parseDate("03.08.2015 10:30:00.000");
		Date toDate = Utils.parseDate("06.08.2015 10:40:00.000");
		List<Candle> list = TQCandleService.getInstance().getSavedCandles(TQSymbol.SiU5, CandleType.CANDLE_1M, fromDate, toDate);
		list = convertCandleList(CandleType.CANDLE_1M, CandleType.CANDLE_2M, list);
		for (Candle c : list) {
			System.out.println(c);
		}
	}
	
	public List<Candle> getHistoryData(TQSymbol symbol, CandleType candleType, int count, boolean reset) {		
		// Проверим, какой тип свечи - реальный или синтетический
		if (getCandleKindByType(candleType) != null) {
			return callHistoryData(symbol, candleType, count, reset);
		} else {
			Entry<Integer, CandleKind> entry = candleKinds.floorEntry(candleType.getSeconds());
			if (entry != null) {
				CandleType tempCandleType = getCandleTypeByKind(entry.getValue());
				logger.info("Converting candles from " + tempCandleType + " to " + candleType);
				List<Candle> tempList = getHistoryData(symbol, tempCandleType, count, reset);
				return convertCandleList(tempCandleType, candleType, tempList);
			} else {
				logger.warn("Cannot retrieve and convert candles for type = " + candleType);
				return new ArrayList<>();
			}
		}
	}
	
	// получим свечи
	private List<Candle> callHistoryData(TQSymbol symbol, CandleType candleType, int count, boolean reset) {		
		
		Holder<TQSymbol, CandleType> key = new Holder<TQSymbol, CandleType>(symbol, candleType);
		logger.debug("Calling getHistoryData for <" + key + ">");
		if (buffer.containsKey(key)) {
			throw new RuntimeException("Already waiting for history data for " + key);
		}

		GetHistoryDataCommand getHistoryDataCommand = new GetHistoryDataCommand();
		getHistoryDataCommand.setBoard(symbol.getBoard());
		getHistoryDataCommand.setSeccode(symbol.getSeccode());
		getHistoryDataCommand.setPeriodId(getCandleKindByType(candleType).getId());
		getHistoryDataCommand.setCandleCount(count);
		getHistoryDataCommand.setReset(reset);
		TransaqLibrary.SendCommand(getHistoryDataCommand.createConnectCommand());

		// создадим буфер
		List<Candle> candleBuffer = Collections.synchronizedList(new ArrayList<Candle>());
		buffer.put(key, candleBuffer);
		
		synchronized (candleBuffer) {
			try {
				candleBuffer.wait(100000);
			} catch (InterruptedException e) {
				throw new RuntimeException("Candles hasn't been received within 100 seconds", e);
			} finally {
				// освободим буфер
				buffer.remove(key);
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
