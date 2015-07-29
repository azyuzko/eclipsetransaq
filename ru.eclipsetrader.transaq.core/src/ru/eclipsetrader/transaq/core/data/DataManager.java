package ru.eclipsetrader.transaq.core.data;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.PersistenceException;
import javax.persistence.Query;

import oracle.jdbc.driver.OracleConnection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ru.eclipsetrader.transaq.core.candle.CandleType;
import ru.eclipsetrader.transaq.core.model.BoardType;
import ru.eclipsetrader.transaq.core.model.BuySell;
import ru.eclipsetrader.transaq.core.model.Candle;
import ru.eclipsetrader.transaq.core.model.Quote;
import ru.eclipsetrader.transaq.core.model.TQSymbol;
import ru.eclipsetrader.transaq.core.model.TradePeriod;
import ru.eclipsetrader.transaq.core.model.internal.ServerObject;
import ru.eclipsetrader.transaq.core.model.internal.SessionObject;
import ru.eclipsetrader.transaq.core.model.internal.TickTrade;
import ru.eclipsetrader.transaq.core.server.TransaqServer;
import ru.eclipsetrader.transaq.core.util.Holder;
import ru.eclipsetrader.transaq.core.util.Utils;

public class DataManager {
	
	static Logger logger = LogManager.getLogger(DataManager.class);
		
	public static EntityManagerFactory entityManagerFactory = null;
	
	static ThreadLocal<EntityManager> tlsEm = new ThreadLocal<EntityManager>() {
		protected EntityManager initialValue() {
			logger.info("Init local EM for thread " + Thread.currentThread().getName());
			if (entityManagerFactory == null) {
				entityManagerFactory = Persistence.createEntityManagerFactory("tq4j");
			}
			return entityManagerFactory.createEntityManager();
		}
	};
	
	final static String TICK_INSERT_SQL = "merge /*+ index (t1 ticks_IX1) */ into ticks t1 using (select ? board, ? seccode, ? tradeno, ? time from dual) t2 "
			+ "on (t1.board = t2.board and t1.seccode = t2.seccode and t1.time = t2.time and t1.tradeno = t2.tradeno) "
			+ " when not matched then insert (session_id, server, board, seccode, tradeno, time, buysell, price, quantity, period, openinterest)"
			+ " values (?, ?, t2.board, t2.seccode, t2.tradeno, t2.time, ?, ?, ?, ?, ? )";
	final static String TICK_SELECT_SQL = "select t.tradeno, t.time, t.board, t.seccode, t.buysell, t.price, t.quantity, t.period, t.openinterest"
			+ " from ticks t where t.time between ? and ? order by t.time, t.tradeno";
	
	final static String QUOTE_INSERT_SQL = "insert into quotes (id, time, board, seccode, price, yield, buy, sell) values (quote_seq.nextval, ?, ?, ?, ?, ?, ?, ?)";
	final static String QUOTE_SELECT_SQL = "select time, board, seccode, price, yield, buy, sell from quotes where time between ? and ? order by time, id";
	
	final static String CANDLES_INSERT_SQL = "merge /*+ index(c1 candles_IX1)*/ into candles c1"
			+ " using (select ? board, ? seccode, ? candletype, ? startDate, ? open, ? high, ? low, ? close, ? volume, ? oi from dual) c2 "
			+ " on (c1.board = c2.board and c1.seccode = c2.seccode and c1.candletype = c2.candletype and c1.startDate = c2.startDate)"
			+ " when not matched then insert (board, seccode, candletype, startDate, open, high, low, close, volume, oi)"
			+ " values (c2.board, c2.seccode, c2.candletype, c2.startDate, c2.open, c2.high, c2.low, c2.close, c2.volume, c2.oi)";
	final static String CANDLES_SELECT_SQL = "select startDate, open, high, low, close, volume, oi from candles c where c.board = ? and c.seccode = ? and c.candletype = ?"
			+ " and c.startDate between ? and ?";
	
	final static String MAX_TICK_SQL = "select max(t.tradeno) from ticks t where t.board = ? and t.seccode = ? and t.time between ? and ?";
	
	public static List<Candle> getCandles(TQSymbol symbol, CandleType candleType, Date fromDate, Date toDate) {
		EntityManager em = tlsEm.get();
		CallableStatement stmt;
		List<Candle> result = new ArrayList<>();
		try {
			em.getTransaction().begin();
			OracleConnection oc = (OracleConnection)em.unwrap(Connection.class);
			stmt = oc.prepareCall(CANDLES_SELECT_SQL);
			stmt.setString(1, symbol.getBoard().toString());
			stmt.setString(2, symbol.getSeccode());
			stmt.setString(3, candleType.toString());
			stmt.setTimestamp(4, new Timestamp(fromDate.getTime()));
			stmt.setTimestamp(5, new Timestamp(toDate.getTime()));
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				Candle candle = new Candle();
				candle.setDate(rs.getTimestamp(1));
				candle.setOpen(rs.getDouble(2));
				candle.setHigh(rs.getDouble(3));
				candle.setLow(rs.getDouble(4));
				candle.setHigh(rs.getDouble(5));
				candle.setVolume(rs.getInt(6));
				candle.setOi(rs.getInt(7));
			}
			rs.close();
			stmt.close();
			em.getTransaction().commit();
		} catch (Exception e) {
			em.getTransaction().rollback();
			e.printStackTrace();
		}
		return result;
	}
	
	public static void batchCandles(TQSymbol symbol, CandleType candleType, List<Candle> candles) {
		EntityManager em = tlsEm.get();
		synchronized (em) {
			PreparedStatement stmt;
			OracleConnection oc = null;
			try {
				em.getTransaction().begin();
				oc = (OracleConnection)em.unwrap(Connection.class);
				oc.setAutoCommit(false);
				stmt = (PreparedStatement) oc.prepareStatement(CANDLES_INSERT_SQL);
				for (Candle candle : candles) {
					stmt.setString(1, symbol.getBoard().toString());
					stmt.setString(2, symbol.getSeccode());
					stmt.setString(3, candleType.toString());
					stmt.setTimestamp(4, new Timestamp(candle.getDate().getTime()));
					stmt.setDouble(5, candle.getOpen());
					stmt.setDouble(6, candle.getHigh());
					stmt.setDouble(7, candle.getLow());
					stmt.setDouble(8, candle.getClose());
					stmt.setInt(9, candle.getVolume());
					stmt.setInt(10, candle.getOi());
					stmt.addBatch();
				}
				stmt.executeBatch();
				stmt.close();
				em.getTransaction().commit();
			} catch (SQLException e) {
				em.getTransaction().rollback();
				e.printStackTrace();
			} 
		}
	}
	
	public static String getMaxTickNo(TQSymbol symbol) {
		return getMaxTickNo(symbol, new Date());
	}
	
	public static String getMaxTickNo(TQSymbol symbol, Date toDate) {
		EntityManager em = tlsEm.get();
		CallableStatement stmt;
		String result = null;
		try {
			em.getTransaction().begin();
			OracleConnection oc = (OracleConnection)em.unwrap(Connection.class);
			stmt = oc.prepareCall(MAX_TICK_SQL);
			stmt.setString(1, symbol.getBoard().toString());
			stmt.setString(2, symbol.getSeccode());
			stmt.setDate(3, new java.sql.Date(toDate.getTime()));
			stmt.setTimestamp(4, new Timestamp(toDate.getTime()));
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				result = rs.getString(1);
			}
			rs.close();
			stmt.close();
			em.getTransaction().commit();
		} catch (Exception e) {
			em.getTransaction().rollback();
			e.printStackTrace();
		}
		return result;
	}
	
	public static void batchTickList(List<TickTrade> list) {
		EntityManager em = tlsEm.get();
		PreparedStatement stmt;
		OracleConnection oc = null;
		try {
			em.getTransaction().begin();
			oc = (OracleConnection)em.unwrap(Connection.class);
			oc.setAutoCommit(false);
			stmt = (PreparedStatement) oc.prepareStatement(TICK_INSERT_SQL);
			for (TickTrade tt : list) {
				Timestamp ts = new Timestamp(tt.getTime().getTime());
				stmt.setString(1, tt.getBoard().toString());
				stmt.setString(2, tt.getSeccode());
				stmt.setString(3, tt.getTradeno());
				stmt.setTimestamp(4, ts);
				stmt.setString(5, TransaqServer.getInstance().getSessionId());
				stmt.setString(6, TransaqServer.getInstance().getId());
				stmt.setString(7, tt.getBuysell().toString());
				stmt.setDouble(8, tt.getPrice());
				stmt.setInt(9, tt.getQuantity());
				if (tt.getPeriod() != null) {
					stmt.setString(10, tt.getPeriod().toString());
				} else {
					stmt.setNull(10, Types.VARCHAR);
				}
				stmt.setInt(11, tt.getOpeninterest());
				stmt.addBatch();
			}
			stmt.executeBatch();
			stmt.close();
			em.getTransaction().commit();
		} catch (SQLException e) {
			em.getTransaction().rollback();
			e.printStackTrace();
		} 
	}
	
	public static List<TickTrade> getTickList(Date dateFrom, Date dateTo) {
		EntityManager em = tlsEm.get();
		PreparedStatement stmt;
		OracleConnection oc = null;
		List<TickTrade> result = new ArrayList<TickTrade>();
		try {
			em.getTransaction().begin();
			oc = (OracleConnection)em.unwrap(Connection.class);
			oc.setAutoCommit(false);
			stmt = (PreparedStatement) oc.prepareStatement(TICK_SELECT_SQL);
			stmt.setTimestamp(1, new java.sql.Timestamp(dateFrom.getTime()));
			stmt.setTimestamp(2, new java.sql.Timestamp(dateTo.getTime()));
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				TickTrade t = new TickTrade();
				t.setTradeno(rs.getString(1));
				t.setTime(rs.getTimestamp(2));
				t.setBoard(BoardType.valueOf(rs.getString(3)));
				t.setSeccode(rs.getString(4));
				t.setBuysell(BuySell.valueOf(rs.getString(5)));
				t.setPrice(rs.getDouble(6));
				t.setQuantity(rs.getInt(7));
				String tradePeriod = rs.getString(8);
				if (tradePeriod != null) {
					t.setPeriod(TradePeriod.valueOf(tradePeriod));
				}
				t.setOpeninterest(rs.getInt(9));
				result.add(t);
			}
			rs.close();
			stmt.close();
			em.getTransaction().commit();
		} catch (SQLException e) {
			em.getTransaction().rollback();
			e.printStackTrace();
		} 
		return result;
	}

	
	public static void batchQuoteList(List<Quote> list) {
		EntityManager em = tlsEm.get();
		PreparedStatement stmt;
		OracleConnection oc = null;
		try {
			em.getTransaction().begin();
			oc = (OracleConnection)em.unwrap(Connection.class);
			oc.setAutoCommit(false);
			stmt = oc.prepareCall(QUOTE_INSERT_SQL);
			for (Quote q : list) {
				stmt.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
				stmt.setString(2, q.getBoard().toString());
				stmt.setString(3, q.getSeccode());
				stmt.setDouble(4, q.getPrice());
				stmt.setInt(5, q.getYield());
				stmt.setInt(6, q.getBuy());
				stmt.setInt(7, q.getSell());
				stmt.addBatch();
			}
			stmt.executeBatch();
			stmt.close();
			em.getTransaction().commit();
		} catch (SQLException e) {
			em.getTransaction().rollback();
			e.printStackTrace();
		} 
	}
	
	public static List<Quote> getQuoteList(Date dateFrom, Date dateTo) {
		EntityManager em = tlsEm.get();
		PreparedStatement stmt;
		OracleConnection oc = null;
		List<Quote> result = new ArrayList<Quote>();
		try {
			em.getTransaction().begin();
			oc = (OracleConnection)em.unwrap(Connection.class);
			oc.setAutoCommit(false);
			stmt = (PreparedStatement) oc.prepareStatement(QUOTE_SELECT_SQL);
			stmt.setTimestamp(1, new java.sql.Timestamp(dateFrom.getTime()));
			stmt.setTimestamp(2, new java.sql.Timestamp(dateTo.getTime()));
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				Quote q = new Quote(rs.getTimestamp(1), BoardType.valueOf(rs.getString(2)), rs.getString(3));
				q.setPrice(rs.getDouble(4));
				q.setYield(rs.getInt(5));
				q.setBuy(rs.getInt(6));
				q.setSell(rs.getInt(7));
				result.add(q);
			}
			rs.close();
			stmt.close();
			em.getTransaction().commit();
		} catch (SQLException e) {
			em.getTransaction().rollback();
			e.printStackTrace();
		} 
		return result;
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
		
	public static <T> T merge(T t) {
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
	
	public static <T> void setSessionId(T object, String sessionId) {
		if (object instanceof SessionObject) {
			((SessionObject)object).setSessionId(sessionId);
		}
	}
	
	public static <T extends SessionObject> void setSessionId(List<T> objectList, String sessionId) {
		for (T t : objectList) {
			t.setSessionId(sessionId);
		}
	}
	
	public static void main(String[] args) {
		
		List<TickTrade> list = new ArrayList<TickTrade>();
		TickTrade tt = new TickTrade();
		Utils.generateStub(tt);
		list.add(tt);
		
		batchTickList(list);
		
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
