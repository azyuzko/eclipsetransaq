package ru.eclipsetrader.transaq.core.datastorage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.eclipsetrader.transaq.core.Constants;
import ru.eclipsetrader.transaq.core.data.DataManager;
import ru.eclipsetrader.transaq.core.event.Observer;
import ru.eclipsetrader.transaq.core.interfaces.IPersistable;
import ru.eclipsetrader.transaq.core.interfaces.ITQSecurity;
import ru.eclipsetrader.transaq.core.model.internal.Client;
import ru.eclipsetrader.transaq.core.model.internal.Security;
import ru.eclipsetrader.transaq.core.services.ITQClientService;

public class TQClientService implements ITQClientService, IPersistable, Observer<Client> {

	Map<String, Client> clients = new HashMap<String, Client>();
	
	static TQClientService instance;
	
	public static TQClientService getInstance() {
		if (instance == null) {
			instance = new TQClientService();
			instance.load(Constants.DEFAULT_SERVER_ID);
		}
		return instance;
	}

	@Override
	public void clear() {
		clients.clear();
	}

	@Override
	public Client get(String id) {
		return clients.get(id);
	}

	@Override
	public List<Client> getAll() {
		return new ArrayList<Client>(clients.values());
	}

	@Override
	public void put(Client client) {
		clients.put(client.getId(), client);
	}

	@Override
	public void persist() {
		DataManager.mergeList(getAll());
	}

	@Override
	public void load(String serverId) {
		clear();
		for (Client client : DataManager.getServerObjectList(Client.class, serverId)) {
			put(client);
		}
	}

	public static void main(String [] args) {
		TQClientService s = new TQClientService();
		for (Client c : s.getAll()) {
			System.out.println(c.toString());
		}
	}

	@Override
	public void putList(List<Client> objects) {
		for (Client client : objects) {
			put(client);
		}
	}
	
	@Override
	public void update(Client client) {
		put(client);
	}

	@Override
	public String getSecurityClientId(ITQSecurity security) {
		// TODO implement and refactor!
		return new ArrayList<Client>(clients.values()).get(0).getId();
	}
	
}
