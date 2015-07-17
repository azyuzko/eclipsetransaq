package ru.eclipsetrader.transaq.core.datastorage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.eclipsetrader.transaq.core.Constants;
import ru.eclipsetrader.transaq.core.data.DataManager;
import ru.eclipsetrader.transaq.core.event.Observer;
import ru.eclipsetrader.transaq.core.interfaces.IPersistable;
import ru.eclipsetrader.transaq.core.model.MarketType;
import ru.eclipsetrader.transaq.core.model.internal.Market;
import ru.eclipsetrader.transaq.core.services.ITQMarketService;

public class TQMarketService implements ITQMarketService, IPersistable, Observer<List<Market>> {
	
	Map<Integer, Market> objects = new HashMap<>();

	static TQMarketService instance;
	public static TQMarketService getInstance() {
		if (instance == null) {
			instance = new TQMarketService();
			instance.load(Constants.DEFAULT_SERVER_ID);
		}
		return instance;
	}
	
	@Override
	public void persist() {
		DataManager.mergeList(getAll());
	}
	
	@Override
	public void load(String serverId) {
		clear();
		for (Market m : DataManager.getServerObjectList(Market.class, serverId)) {
			put(m.getId(), m);
		}
	}

	@Override
	public void clear() {
		objects.clear();
	}

	@Override
	public void put(Integer id, Market object) {
		objects.put(id, object);
	}
	
	@Override
	public void put(Market object) {
		objects.put(object.getId(), object);
	}

	@Override
	public Market get(Integer id) {
		return objects.get(id);
	}

	@Override
	public List<Market> getAll() {
		return new ArrayList<Market>(objects.values());
			
	}

	@Override
	public MarketType getMarketType(Integer marketId) {
		Market market = objects.get(marketId);
		if (market.getName() != null) {
			return MarketType.valueOf(market.getName());
		} else {
			return null;
		}
	}

	@Override
	public void putList(List<Market> objects) {
		for (Market market : objects) {
			put(market);
		}
	}
	
	@Override
	public void update(List<Market> markets) {
		putList(markets);
	}
}
