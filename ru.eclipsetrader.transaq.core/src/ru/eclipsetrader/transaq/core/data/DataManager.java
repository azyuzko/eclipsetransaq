package ru.eclipsetrader.transaq.core.data;

import java.io.StringReader;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.PersistenceException;
import javax.persistence.Query;

import oracle.jdbc.driver.OracleConnection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ru.eclipsetrader.transaq.core.candle.Candle;
import ru.eclipsetrader.transaq.core.candle.CandleType;
import ru.eclipsetrader.transaq.core.model.BoardType;
import ru.eclipsetrader.transaq.core.model.BuySell;
import ru.eclipsetrader.transaq.core.model.Quote;
import ru.eclipsetrader.transaq.core.model.TQSymbol;
import ru.eclipsetrader.transaq.core.model.TradePeriod;
import ru.eclipsetrader.transaq.core.model.XMLDataEvent;
import ru.eclipsetrader.transaq.core.model.internal.ServerObject;
import ru.eclipsetrader.transaq.core.model.internal.SymbolGapMap;
import ru.eclipsetrader.transaq.core.model.internal.TickTrade;
import ru.eclipsetrader.transaq.core.server.TransaqServer;
import ru.eclipsetrader.transaq.core.util.Holder;
import ru.eclipsetrader.transaq.core.util.Utils;
import ru.eclipsetrader.transaq.core.util.XMLFormatter;
import ru.eclipsetrader.transaq.core.xml.handler.XMLProcessType;

public class DataManager {
	
	static Logger logger = LogManager.getLogger("DataManager");
		
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
	
	final static String TICK_INSERT_SQL = "merge into ticks t1 using (select ? board, ? seccode, ? tradeno, ? time from dual) t2 "
			+ "on (t1.board = t2.board and t1.seccode = t2.seccode and t1.time = t2.time and t1.tradeno = t2.tradeno) "
			+ " when not matched then insert (server, board, seccode, tradeno, time, buysell, price, quantity, period, openinterest, received)"
			+ " values (?, t2.board, t2.seccode, t2.tradeno, t2.time, ?, ?, ?, ?, ?, ?)";
	final static String TICK_SELECT_SQL = "select t.tradeno, t.time, t.board, t.seccode, t.buysell, t.price, t.quantity, t.period, t.openinterest, t.received"
			+ " from ticks t where t.time between ? and ?";
	final static String MAX_TICK_SQL = "select max(t.tradeno) from ticks t where t.board = ? and t.seccode = ? and t.time between ? and ?";
	
	final static String QUOTE_INSERT_SQL = "insert into quotes (id, time, board, seccode, price, yield, buy, sell) values (quote_seq.nextval, ?, ?, ?, ?, ?, ?, ?)";
	final static String QUOTE_SELECT_SQL = "select time, board, seccode, price, yield, buy, sell from quotes q"
			+ " where q.time between ? and ? ";
	
	final static String CANDLES_INSERT_SQL = "merge /*+ index(c1 candles_IX1)*/ into candles c1"
			+ " using (select ? board, ? seccode, ? candletype, ? startDate, ? open, ? high, ? low, ? close, ? volume, ? oi from dual) c2 "
			+ " on (c1.board = c2.board and c1.seccode = c2.seccode and c1.candletype = c2.candletype and c1.startDate = c2.startDate)"
			+ " when not matched then insert (board, seccode, candletype, startDate, open, high, low, close, volume, oi)"
			+ " values (c2.board, c2.seccode, c2.candletype, c2.startDate, c2.open, c2.high, c2.low, c2.close, c2.volume, c2.oi)"
			+ " when matched then update set open = c2.open, close = c2.close, low = c2.low, high = c2.high, volume = c2.volume, oi = c2.oi";
	final static String CANDLES_SELECT_SQL = "select startDate, open, high, low, close, volume, oi from candles c where c.board = ? and c.seccode = ? and c.candletype = ?"
			+ " and c.startDate between ? and ?";
	
	final static String QUOTATION_GAP_INSERT = "insert into quotation_gap (id, board, seccode, time, hashmap) values (quotation_seq.nextval, ?, ?, ?, ?)";
	final static String QUOTATION_GAP_SELECT = "select time, board, seccode, hashmap from quotation_gap where time between ? and ? ";
	
	static final String XMLDATA_INSERT_SQL = "INSERT INTO event_audit (session_id, event_time, direction, operation, data) values (?, ?, ?, ?, ?)";
	
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
			while (rs.next()) {
				Candle candle = new Candle();
				candle.setDate(rs.getTimestamp(1));
				candle.setOpen(rs.getDouble(2));
				candle.setHigh(rs.getDouble(3));
				candle.setLow(rs.getDouble(4));
				candle.setClose(rs.getDouble(5));
				candle.setVolume(rs.getInt(6));
				candle.setOi(rs.getInt(7));
				result.add(candle);
			}
			rs.close();
			stmt.close();
			em.getTransaction().commit();
		} catch (Exception e) {
			em.getTransaction().rollback();
			e.printStackTrace();
		}
		Collections.sort(result, new Comparator<Candle>() {
			@Override
			public int compare(Candle o1, Candle o2) {
				return o1.getDate().compareTo(o2.getDate());
			}
		});
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
				stmt.setString(5, TransaqServer.getInstance().getId());
				if (tt.getBuysell() != null) {
					stmt.setString(6, tt.getBuysell().toString());
				} else {
					stmt.setNull(6, Types.VARCHAR);
				}
				stmt.setDouble(7, tt.getPrice());
				stmt.setInt(8, tt.getQuantity());
				if (tt.getPeriod() != null) {
					stmt.setString(9, tt.getPeriod().toString());
				} else {
					stmt.setNull(9, Types.VARCHAR);
				}
				stmt.setInt(10, tt.getOpeninterest());
				stmt.setTimestamp(11, new Timestamp(tt.getReceived().getTime()));
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
	
	public static List<TickTrade> getTickList(Date dateFrom, Date dateTo, TQSymbol[] symbols) {
		if (logger.isDebugEnabled()) {
			logger.debug("getTickList: from " + Utils.formatDate(dateFrom) + " to " + Utils.formatDate(dateTo) + " symbols = " + Arrays.toString(symbols));
		}
		EntityManager em = tlsEm.get();
		PreparedStatement stmt;
		OracleConnection oc = null;
		List<TickTrade> result = new ArrayList<TickTrade>();
		try {
			em.getTransaction().begin();
			oc = (OracleConnection)em.unwrap(Connection.class);
			oc.setAutoCommit(false);
			
			String sql = TICK_SELECT_SQL;
			if (symbols.length > 0) {
				sql += " and (";
				for (int i = 0; i < symbols.length; i++) {
					sql += (i > 0 ? " or (" : " (") + "board = ? and seccode = ?)"; 
				}
				sql += ")";
			}
			
			logger.debug(sql);
			
			stmt = (PreparedStatement) oc.prepareStatement(sql);
			stmt.setTimestamp(1, new java.sql.Timestamp(dateFrom.getTime()));
			stmt.setTimestamp(2, new java.sql.Timestamp(dateTo.getTime()));
			
			int i = 0;
			for (TQSymbol symbol : symbols) {
				stmt.setString(3+i, symbol.getBoard().toString());
				stmt.setString(4+i, symbol.getSeccode());
				i += 2;
			}
			ResultSet rs = stmt.executeQuery();
			logger.debug("getTickList: executed. Fetching...");
			while (rs.next()) {
				TickTrade t = new TickTrade();
				t.setTradeno(rs.getString(1));
				t.setTime(rs.getTimestamp(2));
				t.setBoard(BoardType.valueOf(rs.getString(3)));
				t.setSeccode(rs.getString(4));
				String buySell = rs.getString(5);
				if (buySell != null) {
					t.setBuysell(BuySell.valueOf(buySell));
				}
				t.setPrice(rs.getDouble(6));
				t.setQuantity(rs.getInt(7));
				String tradePeriod = rs.getString(8);
				if (tradePeriod != null) {
					t.setPeriod(TradePeriod.valueOf(tradePeriod));
				}
				t.setOpeninterest(rs.getInt(9));
				t.setReceived(rs.getTimestamp(10));
				result.add(t);
			}
			rs.close();
			stmt.close();
			em.getTransaction().commit();
			logger.debug("getTickList: complete");
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
	
	public static List<Quote> getQuoteList(Date dateFrom, Date dateTo, TQSymbol[] symbols) {
		if (logger.isDebugEnabled()) {
			logger.debug("getQuoteList from " + Utils.formatDate(dateFrom) + " to " + Utils.formatDate(dateTo) + " symbols = " + Arrays.toString(symbols));
		}
		EntityManager em = tlsEm.get();
		PreparedStatement stmt;
		OracleConnection oc = null;
		List<Quote> result = new ArrayList<Quote>();
		try {
			em.getTransaction().begin();
			oc = (OracleConnection)em.unwrap(Connection.class);
			oc.setAutoCommit(false);
			String sql = QUOTE_SELECT_SQL;
			
			if (symbols.length > 0) {
				sql += " and (";
				for (int i = 0; i < symbols.length; i++) {
					sql += (i > 0 ? "or (" : "(") + " board = ? and seccode = ?)";
				}
				sql += ") ";
			}
			logger.debug(sql);
			stmt = (PreparedStatement) oc.prepareStatement(sql);
			stmt.setTimestamp(1, new java.sql.Timestamp(dateFrom.getTime()));
			stmt.setTimestamp(2, new java.sql.Timestamp(dateTo.getTime()));
			
			int i = 0;
			for (TQSymbol symbol : symbols) {
				stmt.setString(3+i, symbol.getBoard().toString());
				stmt.setString(4+i, symbol.getSeccode());
				i += 2;
			}
			
			ResultSet rs = stmt.executeQuery();
			logger.debug("getQuoteList executed. Fetching...");
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
			logger.debug("getQuoteList complete!");
		} catch (SQLException e) {
			em.getTransaction().rollback();
			e.printStackTrace();
		} 
		return result;
	}
	
	public static void batchQuotationGapList(List<SymbolGapMap> list) {
		EntityManager em = tlsEm.get();
		PreparedStatement stmt;
		OracleConnection oc = null;
		try {
			em.getTransaction().begin();
			oc = (OracleConnection)em.unwrap(Connection.class);
			oc.setAutoCommit(false);
			stmt = oc.prepareCall(QUOTATION_GAP_INSERT);
			for (SymbolGapMap gap : list) {
				stmt.setString(1, gap.getBoard().toString());
				stmt.setString(2, gap.getSeccode());
				stmt.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
				stmt.setString(4, SymbolGapMap.mapToString(gap.getGaps()));
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
	
	public static List<SymbolGapMap> getQuotationGapList(Date dateFrom, Date dateTo, TQSymbol[] symbols) {
		
		if (logger.isDebugEnabled()) {
			logger.debug("getQuotationGapList from " + Utils.formatDate(dateFrom) + " to " + Utils.formatDate(dateTo) + " symbols = " + Arrays.toString(symbols));
		}
		
		EntityManager em = tlsEm.get();
		PreparedStatement stmt;
		OracleConnection oc = null;
		List<SymbolGapMap> result = new ArrayList<SymbolGapMap>();
		try {
			em.getTransaction().begin();
			oc = (OracleConnection)em.unwrap(Connection.class);
			oc.setAutoCommit(false);
			
			String sql = QUOTATION_GAP_SELECT;
			if (symbols.length > 0) {
				sql += " and (";
				for (int i = 0; i < symbols.length; i++) {
					sql += (i > 0 ? "or (" : "(") + " board = ? and seccode = ?)";
				}
				sql += ") ";
			}			
			logger.debug(sql);
			
			stmt = (PreparedStatement) oc.prepareStatement(sql);
			stmt.setTimestamp(1, new java.sql.Timestamp(dateFrom.getTime()));
			stmt.setTimestamp(2, new java.sql.Timestamp(dateTo.getTime()));
			
			int i = 0;
			for (TQSymbol symbol : symbols) {
				stmt.setString(3+i, symbol.getBoard().toString());
				stmt.setString(4+i, symbol.getSeccode());
				i += 2;
			}
			
			ResultSet rs = stmt.executeQuery();
			logger.debug("getQuotationGapList executed. Fetching...");
			while (rs.next()) {
				SymbolGapMap s = new SymbolGapMap(rs.getTimestamp(1));
				s.setBoard(BoardType.valueOf(rs.getString(2)));
				s.setSeccode(rs.getString(3));
				Map<String, String> map = SymbolGapMap.stringToMap(rs.getString(4));
				for (String key : map.keySet()) {
					s.getGaps().put(key, map.get(key));
				}
				result.add(s);
			}
			rs.close();
			stmt.close();
			em.getTransaction().commit();
			logger.debug("getQuotationGapList complete!");
		} catch (SQLException e) {
			em.getTransaction().rollback();
			e.printStackTrace();
		} 
		return result;
	}
	
	public static void batchXMLDataEventList(List<XMLDataEvent> list) {
		EntityManager em = tlsEm.get();
		PreparedStatement stmt;
		OracleConnection oc = null;
		try {
			em.getTransaction().begin();
			oc = (OracleConnection)em.unwrap(Connection.class);
			oc.setAutoCommit(false);
			stmt = oc.prepareCall(XMLDATA_INSERT_SQL);
			for (XMLDataEvent event : list) {
				if (event.getOperation() == null) {
					String data = event.getData();
					try {
						int ind1 = data.indexOf(" ");
						int ind2 = data.indexOf(">");
						int index = 0;
						if (ind1 > -1) {
							index = Math.min(ind1, ind2);
						} else {
							index = ind2;
						}
						String sub = data.substring(1, index).toUpperCase();
						event.setOperation(XMLProcessType.valueOf(sub));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				
				stmt.setString(1, event.getSessionId());
				stmt.setTimestamp(2, event.getEventTime());
				stmt.setString(3, event.getDirection().toString());
				stmt.setString(4, event.getOperation().toString());
				String data = XMLFormatter.format(event.getData());
				StringReader r = new StringReader(data);
				stmt.setCharacterStream(5, r, data.length());
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
	
	public static <T> void mergeList(Collection<T> tList) {
		EntityManager em = tlsEm.get();
		try {
			em.getTransaction().begin();
			for (T t : tList) {
				try {
					T t_m = em.merge(t);
				} catch (Exception e) {
					System.err.println("Merge error on :" + Utils.toString(t) + "\n--\n");
					throw e;
				}
			}
			em.flush();
			em.getTransaction().commit();
		} catch (PersistenceException e) {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			e.printStackTrace();
		}
	}
		
	public static <T> void merge(T t) {
		EntityManager em = tlsEm.get();
		try {
			em.getTransaction().begin();
			synchronized (t) {
				em.merge(t);				
			}
			em.flush();
			em.getTransaction().commit();
		} catch (PersistenceException e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	public static <T> T get(Class<T> class_, Object id) {
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
				System.out.println("Removed from DB: " + t);
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
	
	public static void main(String[] args) {
		
		TQSymbol symbol = TQSymbol.BRV5;
		Date date = Utils.parseDate("04.08.2015 10:00:00.000");
		CandleType candleType = CandleType.CANDLE_1H;
		Candle candle = new Candle();
		candle.setDate(date);
		// <candle date="04.08.2015 10:00:00.000" open="50.42" close="50.64" high="50.77" oi="20676" low="50.41" volume="2397"/>
		candle.setOpen(50.42);
		candle.setClose(50.64);
		candle.setHigh(50.77);
		candle.setLow(50.41);
		candle.setVolume(2397);
		candle.setOi(20676);
		
		batchCandles(symbol, candleType, Arrays.asList(candle));
	
	}
	
	
}
