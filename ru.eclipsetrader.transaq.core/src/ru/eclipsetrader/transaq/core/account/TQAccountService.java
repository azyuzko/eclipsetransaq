package ru.eclipsetrader.transaq.core.account;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import ru.eclipsetrader.transaq.core.Constants;
import ru.eclipsetrader.transaq.core.data.DataManager;
import ru.eclipsetrader.transaq.core.event.Observer;
import ru.eclipsetrader.transaq.core.interfaces.IAccount;
import ru.eclipsetrader.transaq.core.interfaces.ITQPosition;
import ru.eclipsetrader.transaq.core.interfaces.ITQSecurity;
import ru.eclipsetrader.transaq.core.model.BoardType;
import ru.eclipsetrader.transaq.core.model.BuySell;
import ru.eclipsetrader.transaq.core.model.PositionType;
import ru.eclipsetrader.transaq.core.model.TQSymbol;
import ru.eclipsetrader.transaq.core.model.internal.FortsMoneyPosition;
import ru.eclipsetrader.transaq.core.model.internal.FortsPosition;
import ru.eclipsetrader.transaq.core.model.internal.MoneyPosition;
import ru.eclipsetrader.transaq.core.model.internal.Order;
import ru.eclipsetrader.transaq.core.model.internal.SecurityPosition;
import ru.eclipsetrader.transaq.core.orders.OrderRequest;
import ru.eclipsetrader.transaq.core.orders.TQOrderTradeService;
import ru.eclipsetrader.transaq.core.services.ITQAccountService;
import ru.eclipsetrader.transaq.core.util.Holder;
import ru.eclipsetrader.transaq.core.util.Utils;

public class TQAccountService implements ITQAccountService, Observer<Holder<PositionType,Map<String,String>>> {
	
	Map<String, SecurityPosition> securityPosition = new HashMap<String, SecurityPosition>();
	Map<String, MoneyPosition> moneyPosition = new HashMap<String, MoneyPosition>();
	Map<String, FortsPosition> fortsPosition = new HashMap<String, FortsPosition>();
	Map<String, FortsMoneyPosition> fortsMoneyPosition = new HashMap<String, FortsMoneyPosition>();

	String serverId;
	
	static TQAccountService instance; 
	public static TQAccountService getInstance() {
		if (instance == null) {
			instance = new TQAccountService(Constants.DEFAULT_SERVER_ID);
		}
		return instance;
	}
	
	public TQAccountService(String serverId) {
		this.serverId = serverId;
	}
	
	IAccount fortsAccount = new IAccount() {
		
		@Override
		public QuantityCost sell(TQSymbol symbol, int quantity) {
			OrderRequest or = OrderRequest.createByMarketRequest(symbol, BuySell.S, quantity);
			Order order = TQOrderTradeService.getInstance().createOrder(or);
			return new QuantityCost(order.getQuantity(), order.getPrice());
		}
		
		@Override
		public void reset() {
			
		}
		
		@Override
		public Map<TQSymbol, QuantityCost> getPositions() {
			Map<TQSymbol, QuantityCost> result = new HashMap<>();
			for (String seccode : fortsPosition.keySet()) {
				result.put(new TQSymbol(BoardType.FUT, seccode), new QuantityCost(fortsPosition.get(seccode).getTotalnet(), 0));
			}
			return result;
		}
		
		@Override
		public double getFree() {
			return fortsMoneyPosition.values().toArray(new FortsMoneyPosition[0])[0].getFree();
		}
		
		@Override
		public QuantityCost close(TQSymbol symbol, double price) {

			return null;
		}
		
		@Override
		public QuantityCost buy(TQSymbol symbol, int quantity) {
			OrderRequest or = OrderRequest.createByMarketRequest(symbol, BuySell.B, quantity);
			Order order = TQOrderTradeService.getInstance().createOrder(or);
			return new QuantityCost(order.getQuantity(), order.getPrice());
		}

		@Override
		public Map<TQSymbol, QuantityCost> getInitialPositions() {
			// TODO Auto-generated method stub
			return null;
		}
	};
	
	public IAccount getAccount(TQSymbol symbol) {
		if (symbol.getBoard() == BoardType.FUT) {
			return fortsAccount;
		}
		return null;
	}
	
	public void applyPositionGap(String serverId, Holder<PositionType, Map<String, String>> gapHolder) {

		String key = null;

		PositionType positionType = gapHolder.getFirst();
		
		Object position;

		switch (positionType) {
		case SECURITY:
			key = gapHolder.getSecond().get("seccode");
			position = securityPosition.containsKey(key) ? securityPosition
					.get(key) : new SecurityPosition(serverId);
			securityPosition.put(key, (SecurityPosition)position);
			break;
		case MONEY: 
			key = gapHolder.getSecond().get("asset") + Constants.SEPARATOR + gapHolder.getSecond().get("register");
			position = moneyPosition.containsKey(key) ? moneyPosition.get(key)
					: new MoneyPosition(serverId);
			moneyPosition.put(key, (MoneyPosition)position);
			break;
		case FORTS:
			key = gapHolder.getSecond().get("market") + Constants.SEPARATOR + gapHolder.getSecond().get("seccode");
			position = fortsPosition.containsKey(key) ? fortsPosition.get(key)
					: new FortsPosition(serverId);
			fortsPosition.put(key, (FortsPosition)position);
			break;
		case FORTS_MONEY:
			key = gapHolder.getSecond().get("market");
			position = fortsMoneyPosition.containsKey(key) ? fortsMoneyPosition
					.get(key) : new FortsMoneyPosition(serverId);
			fortsMoneyPosition.put(key, (FortsMoneyPosition)position);
			break;
		default:
			throw new RuntimeException("Unknown position = " + positionType);
		}

		@SuppressWarnings("rawtypes")
		Class positionClass = position.getClass();

		Map<String, String> keyValueMap = gapHolder.getSecond();
		for (String attr : keyValueMap.keySet()) {
			Object value = null;
			String stringValue = keyValueMap.get(attr);
			if ("client".equals(attr) || "shortname".equals(attr)
					|| "seccode".equals(attr) || "asset".equals(attr)
					|| "register".equals(attr))
				value = stringValue;

			else if ("ordbuycond".equals(attr) || "comission".equals(attr)
					|| "optmargin".equals(attr) || "varmargin".equals(attr)
					|| "usedsellspotlimit".equals(attr)
					|| "sellspotlimit".equals(attr) || "netto".equals(attr)
					|| "kgo".equals(attr) || "blocked".equals(attr)
					|| "free".equals(attr) || "current".equals(attr))
				value = Double.valueOf(stringValue);

			else if ("saldoin".equals(attr) || "saldomin".equals(attr)
					|| "bought".equals(attr) || "sold".equals(attr)
					|| "saldo".equals(attr) || "ordbuy".equals(attr)
					|| "ordsell".equals(attr)) {
				if (positionType == PositionType.SECURITY) {
					value = Integer.valueOf(stringValue);
				} else {
					value = Double.valueOf(stringValue);
				}
			}

			else if ("market".equals(attr) || "secid".equals(attr)
					|| "startnet".equals(attr) || "openbuys".equals(attr)
					|| "opensells".equals(attr) || "totalnet".equals(attr)
					|| "todaybuy".equals(attr) || "todaysell".equals(attr)
					|| "expirationpos".equals(attr))
				value = Integer.valueOf(stringValue);

			else
				throw new IllegalArgumentException("Неизвестное поле " + attr);

			// System.out.println(attr + " - " + stringValue + " - " +
			// positions.size());

			try {
				Field f = null;
				try {
					f = positionClass.getDeclaredField(attr);
				} catch (NoSuchFieldException fe) {
					f = positionClass.getSuperclass().getDeclaredField(attr);
				}
				if (!f.isAccessible()) {
					f.setAccessible(true);
				}
				f.set(position, value);
			} catch (NoSuchFieldException fe) {
				throw new RuntimeException("Неизвестный атрибут: " + attr);
			} catch (IllegalArgumentException e) {
				System.out.println("gap = " + gapHolder.getFirst());
				System.out.println("attr = " + attr);
				System.out.println("value = " + value);
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}

	}

	@Override
	public void persist() {
		try {
			DataManager.removeList(DataManager.getList(FortsMoneyPosition.class));
			DataManager.removeList(DataManager.getList(FortsPosition.class));
			DataManager.removeList(DataManager.getList(MoneyPosition.class));
			DataManager.removeList(DataManager.getList(SecurityPosition.class));
			DataManager.mergeList(fortsMoneyPosition.values());
			DataManager.mergeList(fortsPosition.values());
			DataManager.mergeList(securityPosition.values());
			DataManager.mergeList(moneyPosition.values());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void load(String serverId) {
		clear();
		for (FortsMoneyPosition p : DataManager.getServerObjectList(FortsMoneyPosition.class, serverId)) {
			fortsMoneyPosition.put(p.getKey(), p);
		}
		for (FortsPosition p : DataManager.getServerObjectList(FortsPosition.class, serverId)) {
			fortsPosition.put(p.getSeccode(), p);
		}
		for (SecurityPosition p : DataManager.getServerObjectList(SecurityPosition.class, serverId)) {
			securityPosition.put(p.getKey(), p);
		}
		for (MoneyPosition p : DataManager.getServerObjectList(MoneyPosition.class, serverId)){
			moneyPosition.put(p.getKey(), p);
		}
	}

	public void clear() {
		fortsMoneyPosition.clear();
		fortsPosition.clear();
		securityPosition.clear();
		moneyPosition.clear();
	}

	@Override
	public ITQPosition getPosition(ITQSecurity security) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void update(Holder<PositionType, Map<String, String>> gap) {
		applyPositionGap(serverId, gap);
		persist() ;
	}

}

