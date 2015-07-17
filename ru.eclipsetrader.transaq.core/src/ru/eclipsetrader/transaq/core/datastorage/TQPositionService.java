package ru.eclipsetrader.transaq.core.datastorage;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import ru.eclipsetrader.transaq.core.Constants;
import ru.eclipsetrader.transaq.core.data.DataManager;
import ru.eclipsetrader.transaq.core.event.Observer;
import ru.eclipsetrader.transaq.core.interfaces.ITQPosition;
import ru.eclipsetrader.transaq.core.interfaces.ITQSecurity;
import ru.eclipsetrader.transaq.core.model.PositionType;
import ru.eclipsetrader.transaq.core.model.internal.FortsMoneyPosition;
import ru.eclipsetrader.transaq.core.model.internal.FortsPosition;
import ru.eclipsetrader.transaq.core.model.internal.MoneyPosition;
import ru.eclipsetrader.transaq.core.model.internal.SecurityPosition;
import ru.eclipsetrader.transaq.core.services.ITQPositionService;
import ru.eclipsetrader.transaq.core.util.Holder;

public class TQPositionService implements ITQPositionService, Observer<Holder<PositionType,Map<String,String>>> {
	
	Map<String, SecurityPosition> securityPosition = new HashMap<String, SecurityPosition>();
	Map<String, MoneyPosition> moneyPosition = new HashMap<String, MoneyPosition>();
	Map<String, FortsPosition> fortsPosition = new HashMap<String, FortsPosition>();
	Map<String, FortsMoneyPosition> fortsMoneyPosition = new HashMap<String, FortsMoneyPosition>();

	String serverId;
	
	static TQPositionService instance; 
	public static TQPositionService getInstance() {
		if (instance == null) {
			instance = new TQPositionService(Constants.DEFAULT_SERVER_ID);
			instance.load(Constants.DEFAULT_SERVER_ID);
		}
		return instance;
	}
	
	public TQPositionService(String serverId) {
		this.serverId = serverId;
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
		DataManager.mergeList(fortsMoneyPosition.values());
		DataManager.mergeList(fortsPosition.values());
		DataManager.mergeList(securityPosition.values());
		DataManager.mergeList(moneyPosition.values());
	}

	@Override
	public void load(String serverId) {
		clear();
		for (FortsMoneyPosition p : DataManager.getServerObjectList(FortsMoneyPosition.class, serverId)) {
			fortsMoneyPosition.put(p.getKey(), p);
		}
		for (FortsPosition p : DataManager.getServerObjectList(FortsPosition.class, serverId)) {
			fortsPosition.put(p.getKey(), p);
		}
		for (SecurityPosition p : DataManager.getServerObjectList(SecurityPosition.class, serverId)) {
			securityPosition.put(p.getKey(), p);
		}
		for (MoneyPosition p : DataManager.getServerObjectList(MoneyPosition.class, serverId)){
			moneyPosition.put(p.getKey(), p);
		}
	}

	@Override
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
	}

}
