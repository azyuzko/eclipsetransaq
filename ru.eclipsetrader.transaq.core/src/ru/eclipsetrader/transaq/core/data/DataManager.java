package ru.eclipsetrader.transaq.core.data;

import java.util.Collection;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.PersistenceException;
import javax.persistence.Query;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ru.eclipsetrader.transaq.core.model.internal.Order;
import ru.eclipsetrader.transaq.core.model.internal.ServerObject;
import ru.eclipsetrader.transaq.core.model.internal.SessionObject;
import ru.eclipsetrader.transaq.core.model.internal.Trade;
import ru.eclipsetrader.transaq.core.server.TransaqServer;
import ru.eclipsetrader.transaq.core.util.Holder;

public class DataManager {
	
	static Logger logger = LogManager.getLogger(DataManager.class);
		
	static EntityManagerFactory entityManagerFactory = null;
	
	static ThreadLocal<EntityManager> tlsEm = new ThreadLocal<EntityManager>() {
		protected EntityManager initialValue() {
			logger.info("Init local EM for thread " + Thread.currentThread().getName());
			if (entityManagerFactory == null) {
				entityManagerFactory = Persistence.createEntityManagerFactory("tq4j");
			}
			return entityManagerFactory.createEntityManager();
		}
	};
    
	public static Trade getTrade(String tradeno) {
		EntityManager em = tlsEm.get();
		return em.find(Trade.class, tradeno);
	}
	
	public static Order getOrder(String transactionId) {
		EntityManager em = tlsEm.get();
		return em.find(Order.class, transactionId);
	}

	public static <T> void persist(T t) {
		EntityManager em = tlsEm.get();
		try {
			em.getTransaction().begin();
			em.persist(t);
			em.getTransaction().commit();
		} catch (PersistenceException e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	public static <T extends SessionObject> void mergeList(Collection<T> tList) {
		EntityManager em = tlsEm.get();
		try {
			em.getTransaction().begin();
			TransaqServer inst = TransaqServer.getInstance();
			for (T t : tList) {
				if (inst != null) {
					setSessionId(t, inst.getSessionId());
				}
				t = em.merge(t);
			}
			em.flush();
			em.getTransaction().commit();
		} catch (PersistenceException e) {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			e.printStackTrace();
			throw e;
		}
	}
		
	public static <T extends SessionObject> T merge(T t) {
		EntityManager em = tlsEm.get();
		try {
			em.getTransaction().begin();
			if (TransaqServer.getInstance() != null) {
				setSessionId(t, TransaqServer.getInstance().getSessionId());
			}
			t = em.merge(t);
			em.flush();
			em.getTransaction().commit();
		} catch (PersistenceException e) {
			e.printStackTrace();
			throw e;
		}
		return t;	
	}
	
	public static <T> T get(Class<T> class_, String id) {
		EntityManager em = tlsEm.get();
		return em.find(class_, id);
	}
	
	public static <T> void remove(T t) {
		EntityManager em = tlsEm.get();
		try {
			em.getTransaction().begin();
			em.remove(t);
			em.flush();
			em.getTransaction().commit();
		} catch (PersistenceException e) {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			throw e;
		}
	}
	
	public static <T> void removeList(List<T> list) {
		EntityManager em = tlsEm.get();
		try {
			em.getTransaction().begin();
			for (T t : list) {
				em.remove(t);
			}
			em.flush();
			em.getTransaction().commit();
		} catch (PersistenceException e) {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			throw e;
		}
	}

	
	@SuppressWarnings("unchecked")
	public static <T> List<T> getList(Class<T> class_) {
		EntityManager em = tlsEm.get();
		Query query = em.createQuery("select s from " + class_.getSimpleName() +  " s");
		return (List<T>)query.getResultList();
	}
	
	@SuppressWarnings("unchecked")
	public static <T extends ServerObject> List<T> getServerObjectList(Class<T> class_, String serverId) {
		EntityManager em = tlsEm.get();
		Query query = em.createQuery("select s from " + class_.getSimpleName() +  " s where s.server = :serverId");
		query.setParameter("serverId", serverId);
		return (List<T>)query.getResultList();
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static List executeQuery(String queryString, Holder<String, Object>...params) {
		EntityManager em = tlsEm.get();
		Query query = em.createQuery(queryString);
		if (params != null) {
			for (Holder<String, Object> param : params) {
				query.setParameter(param.getFirst(), param.getSecond());				
			}
		}
		return query.getResultList();
	}
	
	@SuppressWarnings({ "unchecked" })
	public static void executeUpdate(String updateString, Holder<String, Object>...params) {
		EntityManager em = tlsEm.get();
		Query query = em.createQuery(updateString);
		if (params != null) {
			for (Holder<String, Object> param : params) {
				query.setParameter(param.getFirst(), param.getSecond());				
			}
		}
		query.executeUpdate();
	}
	
	public static <T extends ServerObject> void removeServerObjects(String serverId, Class<T> class_) {
		removeList(getServerObjectList(class_, serverId));
	}
	
	public static <T extends SessionObject> void setSessionId(T object, String sessionId) {
		object.setSessionId(sessionId);
	}
	
	public static <T extends SessionObject> void setSessionId(List<T> objectList, String sessionId) {
		for (T t : objectList) {
			t.setSessionId(sessionId);
		}
	}
	
	public static void main(String[] args) {
		
		
		/*for (String id : (List<String>)DataManager.executeQuery("select s.id from Server s")) {
			System.out.println(id);
		}
		
		/*List<Market> ml = DataManager.getServerObjectList(Market.class, "PROD");
		for (Market m : ml) {
			System.out.println(m.toString());
		}
		
		Trade t = new Trade();
		t = Utils.generateStub(t);
		System.out.println(t);
		persistTrade(t);
		
		Order o = new Order();
		o = Utils.generateStub(o);
		persistOrder(o);
	
		XMLDataEvent xmlDataEvent = new XMLDataEvent();
		xmlDataEvent = Utils.generateStub(xmlDataEvent);
		xmlDataEvent.setId(null);
		xmlDataEvent.setSessionId(UUID.randomUUID().toString());
		System.out.println(Utils.toString(xmlDataEvent));
		persist(xmlDataEvent);
		
		SecurityPosition sp = new SecurityPosition();
		sp = Utils.generateStub(sp);
		sp.setRegister(sp.getRegister());
		merge(sp);
		
		
		Signal s = new Signal();
		s.setBuySell(BuySell.B);
		s.setByMarket(false);
		s.setPrice(123.45);
		s.setQuantity(23);
		s.setStrategy("BR1_TST");
		s.setTime(new Date());
		
		DataManager.persist(s);*/
	}
	
	
}
