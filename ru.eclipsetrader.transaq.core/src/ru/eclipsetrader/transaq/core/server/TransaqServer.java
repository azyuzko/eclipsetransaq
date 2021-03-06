package ru.eclipsetrader.transaq.core.server;

import java.io.Closeable;
import java.io.StringReader;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;
import java.util.function.Consumer;

import javax.xml.parsers.SAXParser;

import org.apache.commons.lang3.time.DateUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xml.sax.InputSource;

import ru.eclipsetrader.transaq.core.Constants;
import ru.eclipsetrader.transaq.core.Settings;
import ru.eclipsetrader.transaq.core.account.TQAccountService;
import ru.eclipsetrader.transaq.core.candle.TQCandleService;
import ru.eclipsetrader.transaq.core.data.DataManager;
import ru.eclipsetrader.transaq.core.data.DatabaseManager;
import ru.eclipsetrader.transaq.core.datastorage.TQBoardService;
import ru.eclipsetrader.transaq.core.datastorage.TQClientService;
import ru.eclipsetrader.transaq.core.datastorage.TQMarketService;
import ru.eclipsetrader.transaq.core.event.Event;
import ru.eclipsetrader.transaq.core.exception.CommandException;
import ru.eclipsetrader.transaq.core.exception.ConnectionException;
import ru.eclipsetrader.transaq.core.exception.UnimplementedException;
import ru.eclipsetrader.transaq.core.library.TransaqLibrary;
import ru.eclipsetrader.transaq.core.model.ConnectionStatus;
import ru.eclipsetrader.transaq.core.model.internal.CommandResult;
import ru.eclipsetrader.transaq.core.model.internal.Server;
import ru.eclipsetrader.transaq.core.model.internal.ServerSession;
import ru.eclipsetrader.transaq.core.orders.TQOrderTradeService;
import ru.eclipsetrader.transaq.core.quotes.TQQuotationService;
import ru.eclipsetrader.transaq.core.quotes.TQQuoteService;
import ru.eclipsetrader.transaq.core.securities.TQSecurityService;
import ru.eclipsetrader.transaq.core.server.command.Command;
import ru.eclipsetrader.transaq.core.trades.TQTickTradeService;
import ru.eclipsetrader.transaq.core.util.Utils;
import ru.eclipsetrader.transaq.core.xml.handler.XMLHandler;

import com.sun.jna.Pointer;

public class TransaqServer implements com.sun.jna.Callback, Closeable {
	
	static Logger logger = LogManager.getFormatterLogger(TransaqServer.class);
	static Logger xmlLogger = LogManager.getFormatterLogger("XMLTrace");
	
	private ServerSession session;
	private Server server;
	String serverId;
	volatile boolean hasInitialized = false;
	volatile int timeDiff = 0; // ������� ����� ������� � ��������� ��������
	volatile boolean isOvernight = false;
	SAXParser parser;

	MarketSchedule marketSchedule = MarketSchedule.createTransaqSchedule();
	
	private Object statusLock = new Object();
		
	final EventHolder eventHolder = new EventHolder();
	
	private final static TransaqServer INSTANCE = new TransaqServer(Constants.DEFAULT_SERVER_ID);
	public static TransaqServer getInstance() {
		return INSTANCE;
	}
	
	public static final Event<TransaqServer> onConnectEstablished = new Event<TransaqServer>("TransaqServer.onConnectEstablished");
	public static final Event<TransaqServer> onDisconnected = new Event<TransaqServer>("TransaqServer.onDisconnected");
	
	public TransaqServer(final String serverId) {
		
		this.serverId = serverId;
		this.server = DataManager.get(Server.class, serverId);
		if (server == null) {
			throw new RuntimeException("Server " + serverId + " not found");
		}
		this.session = new ServerSession(serverId);
		
		try {
			this.parser = Utils.getSAXParserFactory().newSAXParser();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		eventHolder.onServerStatusChange.addObserver((newStatus) -> {
			try {
				updateServerStatus(newStatus.getStatus(), newStatus.getError());
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				synchronized (statusLock) {
					statusLock.notify();
				}
			}
		});
		
		eventHolder.onMessageReceive.addObserver((message) -> {
			System.err.println(String.format("MESSAGE FROM <%s>: %s", message.getFrom(), message.getText()));
		});
		
		eventHolder.onOvernightChange.addObserver((isOvernight) -> {
			if (isOvernight != this.isOvernight) {
				System.err.println(String.format("isOvernight has changed to %s", isOvernight));
			}
		});
		
				
		eventHolder.onCandleKindChange.addObserver(TQCandleService.getInstance().getCandleKindObserver());
		eventHolder.onCandleGraphReceive.addObserver(TQCandleService.getInstance().getCandleGraphObserver());
		
		eventHolder.onBoardsChange.addObserver(TQBoardService.getInstance());
		eventHolder.onMarketsChange.addObserver(TQMarketService.getInstance());
		eventHolder.onClientReceive.addObserver(TQClientService.getInstance());
		
		eventHolder.onSecuritiesChange.addObserver(TQSecurityService.getInstance().getSecurityObserver());
		eventHolder.onPitsChange.addObserver(TQSecurityService.getInstance().getPitObserver());
		eventHolder.onSecInfoUpdate.addObserver(TQSecurityService.getInstance().getSecInfoUpdateObserver());
		
		eventHolder.onPositionChange.addObserver(TQAccountService.getInstance());
		eventHolder.onOrderReceive.addObserver(TQOrderTradeService.getInstance().getOrderObserver());
		eventHolder.onStopOrderReceive.addObserver(TQOrderTradeService.getInstance().getStopOrderObserver());
		
		eventHolder.onTradeChange.addObserver(TQOrderTradeService.getInstance().getTradeObserver());
		eventHolder.onTickTradeChange.addObserver(TQTickTradeService.getInstance().getTickObserver());
		eventHolder.onAllTradeChange.addObserver(TQTickTradeService.getInstance().getTickObserver());
		
		eventHolder.onQuotesChange.addObserver(TQQuoteService.getInstance().getQuoteGapObserver());
		eventHolder.onQuotationsChange.addObserver(TQQuotationService.getInstance().getQuotationGapObserver());
	}
	
	public ConnectionStatus getStatus() {
		if (session == null) {
			return ConnectionStatus.DISCONNECTED;
		} else {
			return session.getStatus();
		}
	}
	
	public void updateServerStatus(ConnectionStatus newStatus, String error) {
		if (session.getStatus() != newStatus) {
			session.setStatus(newStatus);
			// session.setTimezone(newStatus.getTimeZone());
			logger.info("New server status: " + newStatus);
			if (session.getStatus() == ConnectionStatus.CONNECTED) {
				session.setConnected(new Date());
				session.setDisconnected(null);
				session.setError(null);
				// ������� ������� �� ������� � ��������
				CommandResult crDiff = TransaqLibrary.SendCommand(Command.GET_SERVTIME_DIFFERENCE);
				if (crDiff.isSuccess() && crDiff.getDiff() != null) {
					timeDiff = crDiff.getDiff();
				}
				DataManager.merge(session);
				onConnectEstablished.notifyObservers(this);
			} else if (session.getStatus() == ConnectionStatus.DISCONNECTED) {
				session.setError(error);
				session.setDisconnected(new Date());
				session.setStatus(ConnectionStatus.DISCONNECTED);
				DataManager.merge(session);
				onDisconnected.notifyObservers(this);
			}
			
			/*if (CoreActivator.getEventAdmin() != null) {
				CoreActivator.getEventAdmin().postEvent(OSGIServerStatusEvent.getEvent(serverId, newStatus));
			}*/
		}
	}
		
	public void callback (Pointer pData) {

		String data = pData.getString(0);	
		TransaqLibrary.FreeMemory(pData);
		
		if ( Settings.SHOW_CONSOLE_TRACE) {
			xmlLogger.warn(data);
		} else if (xmlLogger.isDebugEnabled()) {
			xmlLogger.debug(data);
		} else if (xmlLogger.isInfoEnabled()) {
			xmlLogger.info(data.substring(0, Math.min(50, data.length()-1)) +  "...");
		}
		
		DatabaseManager.writeInputEvent(session.getSessionId(), data);
		
		try {
			XMLHandler handler = new XMLHandler(serverId, eventHolder);
	    	if (handler != null) {
	    		parser.parse(new InputSource(new StringReader(data)), handler);
	    	}
		} catch (Exception e) {
			System.out.println(data);
			e.printStackTrace();
		}
    }
	
	private void init() throws ConnectionException {
		if (hasInitialized) {
			logger.info("Transaq library was already initialized");
			return;
		}
		try {
			TransaqLibrary.Initialize(server.getLogDir(), server.getLogLevel().getLevel());
			if (!TransaqLibrary.SetCallbackEx(this)) {
				TransaqLibrary.UnInitialize();
				throw new ConnectionException("SetCallbackEx failed!");
			};
		} catch (Throwable ex) {
			throw new ConnectionException(ex);
		}
		hasInitialized = true;
	}
	
	private void initNewSession() {
		session.setSessionId(UUID.randomUUID().toString());		
		session.setStatus(ConnectionStatus.CONNECTING);
	}
	
	public void reconnect() {
		if (session.getStatus() != ConnectionStatus.DISCONNECTED) {
			throw new RuntimeException("Unable to reconnect from state " + getStatus());
		}
		
		// TODO reconnect
		throw new UnimplementedException();
	}

	public void connect(Consumer<TransaqServer> callback) throws ConnectionException {
		
		if (!marketSchedule.canConnect(LocalDateTime.now())) {
			throw new ConnectionException("Cannot connect now! Closest connection time = " + marketSchedule.closestConnectDateTime(LocalDateTime.now()));
		}
		
		long startTime = System.currentTimeMillis();
		
		if (!hasInitialized) {
			logger.info("Initializing library");
			init();
		}
		
		initNewSession();
		
		try {
			TransaqLibrary.SendCommand(server.createConnectCommand());
		} catch (CommandException ex) {
			throw new ConnectionException("������ ��� ����������� � �������: <" + ex.getMessage()+ ">", ex);
		}
		
		synchronized (statusLock) {
			try {
				statusLock.wait(Constants.CONNECT_TIMEOUT);
				session.setStatus(ConnectionStatus.CONNECTED);
				long endTime = System.currentTimeMillis();
				
				System.err.println(String.format("Connected in %.2fs", (endTime-startTime)/1000.0));
				callback.accept(this);
			} catch (InterruptedException e) {
				session.setStatus(ConnectionStatus.DISCONNECTED);
				DataManager.merge(session);
				throw new ConnectionException("����������� �� ����������� � ������� " + Constants.CONNECT_TIMEOUT/1000 + " ������");
			}
		}
	}

	public void disconnect() {
		if (session.getStatus() == ConnectionStatus.CONNECTED) {
			session.setStatus(ConnectionStatus.DISCONNECTING);
			TransaqLibrary.SendCommand(Command.DISCONNECT);
			synchronized (statusLock) {
				try {
					statusLock.wait(Constants.DISCONNECT_TIMEOUT);
					close();
				} catch (InterruptedException e) {
					e.printStackTrace();
					throw new RuntimeException(e);
				}
			}
		} else {
			throw new RuntimeException("Not connected to server!");
		}
	}

	public void close() {
		if (hasInitialized) {
			TransaqLibrary.UnInitialize();
			hasInitialized = false;
		}
	}
	
	public ConnectionStatus updateStatus() {
		TransaqLibrary.SendCommand(Command.SERVER_STATUS);
		synchronized (statusLock) {
			try {
				statusLock.wait(Constants.CHECK_CONNECTION_TIMEOUT);
			} catch (InterruptedException e) {
				updateServerStatus(ConnectionStatus.DISCONNECTED, "TransaqServer.updateStatus timeout, connection has lost");
			}
			return getStatus();
		}
	}

	public Date getServerTime() {
		return DateUtils.addSeconds(new Date(), timeDiff);
	}

	public String getId() {
		return serverId;
	}
	
	public String getSessionId() {
		return session.getServerId();
	}

	public MarketSchedule getMarketSchedule() {
		return marketSchedule;
	}

	public void onConnect() {
		//LoadCandlesSchedule.scheduleLoadCandles();
	}
	
	public void onDisconnect() {
		
	}
	
	static {
		onConnectEstablished.addObserver(ts -> ts.onConnect());
		onDisconnected.addObserver(ts -> ts.onDisconnect());
	}

}
