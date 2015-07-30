package ru.eclipsetrader.transaq.core.securities;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ru.eclipsetrader.transaq.core.data.DataManager;
import ru.eclipsetrader.transaq.core.event.Observer;
import ru.eclipsetrader.transaq.core.model.BoardType;
import ru.eclipsetrader.transaq.core.model.MarketType;
import ru.eclipsetrader.transaq.core.model.TQSymbol;
import ru.eclipsetrader.transaq.core.model.internal.Pit;
import ru.eclipsetrader.transaq.core.model.internal.SecInfoUpdate;
import ru.eclipsetrader.transaq.core.model.internal.Security;
import ru.eclipsetrader.transaq.core.services.ITQSecurityService;

public class TQSecurityService implements ITQSecurityService {
	
	Logger logger = LogManager.getLogger(TQSecurityService.class);

	Map<TQSymbol, Security> securities = new LinkedHashMap<>();
	
	static TQSecurityService instance;
	public static TQSecurityService getInstance() {
		if (instance == null) {
			instance = new TQSecurityService();
		}
		return instance;
	}
	
	Observer<List<Security>> securityObserver = new Observer<List<Security>>() {
		@Override
		public void update(List<Security> securities) {
			putList(securities);
		}
	};
	
	Observer<List<Pit>> pitObserver = new Observer<List<Pit>>() {
		
		@Override
		public void update(List<Pit> pits) {
			// TODO надо думать
			for (Pit pit : pits) {
				// assingPit(pit);
			}
		}
	}; 
	
	Observer<SecInfoUpdate> secInfoUpdateObserver = new Observer<SecInfoUpdate>() {
		@Override
		public void update(SecInfoUpdate secInfoUpdate) {
			// System.out.println(secInfoUpdate.getKey());
		}
	};
	
	public void assingPit(Pit pit) {
		Security security = securities.get(pit.getSymbol());
		if (security == null) {
			logger.info(securities.keySet());
			logger.error("Security not found! + " + pit);
			System.exit(1);
		}
		security.setMinStep(pit.getMinStep());
		security.setLotSize(pit.getLotSize());
		security.setDecimals(pit.getDecimals());
		security.setPoint_cost(pit.getPoint_cost());
	}
	
	public Observer<SecInfoUpdate> getSecInfoUpdateObserver() {
		return secInfoUpdateObserver;
	}
	
	public Observer<List<Security>> getSecurityObserver() {
		return securityObserver;
	}
	
	public Observer<List<Pit>> getPitObserver() {
		return pitObserver;
	}

	public void clear() {
		securities.clear();
	}
	
	public void put(Security security) {
		securities.put(security.getSymbol(), security);
	}

	public void putList(List<Security> objects) {
		for (Security security : objects) {
			put(security);
		}
	}
	
	public Security get(TQSymbol symbol) {
		return securities.get(symbol);
	}

	public List<Security> getAll() {
		return new ArrayList<Security>(securities.values());
	}


	/**
	 * Get securities by market Id
	 * @param marketId
	 * @return
	 */
	@Override
	public List<Security> getMarketSecurities(MarketType market) {
		List<Security> result = new ArrayList<Security>();
		for (Security s : getAll()) {
			if (s.getMarket() == market) {
				result.add(s);
			}
		}
		return result;
	}
	
	/**
	 * Get securities by board
	 * @param board
	 * @return
	 */
	@Override
	public List<Security> getBoardSecurities(BoardType board) {
		List<Security> result = new ArrayList<Security>();
		for (Security s : getAll()) {
			if (s.getBoard() == board) {
				result.add(s);
			}
		}
		return result;
	}

	@Override
	public Security getSecurity(TQSymbol symbol) {
		return get(symbol);
	}
	
	/*public void callGetSecurities() {
		TransaqLibrary.SendCommand(Command.GET_SECURITIES);
	}*/

	public void persist() {
		DataManager.mergeList(getAll());
	}



}
