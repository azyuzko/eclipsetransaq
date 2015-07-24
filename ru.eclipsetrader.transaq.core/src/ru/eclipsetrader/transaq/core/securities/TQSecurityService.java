package ru.eclipsetrader.transaq.core.securities;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ru.eclipsetrader.transaq.core.Constants;
import ru.eclipsetrader.transaq.core.data.DataManager;
import ru.eclipsetrader.transaq.core.event.Observer;
import ru.eclipsetrader.transaq.core.interfaces.IPersistable;
import ru.eclipsetrader.transaq.core.library.TransaqLibrary;
import ru.eclipsetrader.transaq.core.model.BoardType;
import ru.eclipsetrader.transaq.core.model.MarketType;
import ru.eclipsetrader.transaq.core.model.TQSymbol;
import ru.eclipsetrader.transaq.core.model.internal.Pit;
import ru.eclipsetrader.transaq.core.model.internal.SecInfoUpdate;
import ru.eclipsetrader.transaq.core.model.internal.Security;
import ru.eclipsetrader.transaq.core.server.command.Command;
import ru.eclipsetrader.transaq.core.services.ITQSecurityService;

public class TQSecurityService implements ITQSecurityService, IPersistable {

	Map<TQSymbol, Security> objects = new LinkedHashMap<>();
	
	static TQSecurityService instance;
	public static TQSecurityService getInstance() {
		if (instance == null) {
			instance = new TQSecurityService();
			instance.load(Constants.DEFAULT_SERVER_ID);
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
			
		}
	}; 
	
	Observer<SecInfoUpdate> secInfoUpdateObserver = new Observer<SecInfoUpdate>() {
		@Override
		public void update(SecInfoUpdate secInfoUpdate) {
			// System.out.println(secInfoUpdate.getKey());
		}
	};
	
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
		objects.clear();
	}
	
	public void put(Security security) {
		objects.put(security.getSymbol(), security);
	}

	public void putList(List<Security> objects) {
		for (Security security : objects) {
			put(security);
		}
	}
	
	public Security get(TQSymbol symbol) {
		return objects.get(symbol);
	}

	public List<Security> getAll() {
		return new ArrayList<Security>(objects.values());
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
	
	public void callGetSecurities() {
		TransaqLibrary.SendCommand(Command.GET_SECURITIES);
	}

	@Override
	public void persist() {
		DataManager.mergeList(getAll());
	}

	@Override
	public void load(String serverId) {
		clear();
		for (Security s : DataManager.getServerObjectList(Security.class, serverId)) {
			put(s);
		}
	}


}
