package ru.eclipsetrader.transaq.core.server;

import java.io.Closeable;
import java.io.StringReader;
import java.util.Date;
import java.util.UUID;

import javax.xml.parsers.SAXParser;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xml.sax.InputSource;

import ru.eclipsetrader.transaq.core.Constants;
import ru.eclipsetrader.transaq.core.CoreActivator;
import ru.eclipsetrader.transaq.core.Settings;
import ru.eclipsetrader.transaq.core.data.DataManager;
import ru.eclipsetrader.transaq.core.data.DatabaseManager;
import ru.eclipsetrader.transaq.core.datastorage.TQBoardService;
import ru.eclipsetrader.transaq.core.datastorage.TQCandleService;
import ru.eclipsetrader.transaq.core.datastorage.TQClientService;
import ru.eclipsetrader.transaq.core.datastorage.TQMarketService;
import ru.eclipsetrader.transaq.core.datastorage.TQPositionService;
import ru.eclipsetrader.transaq.core.event.Event;
import ru.eclipsetrader.transaq.core.event.Observer;
import ru.eclipsetrader.transaq.core.event.osgi.OSGIServerStatusEvent;
import ru.eclipsetrader.transaq.core.exception.CommandException;
import ru.eclipsetrader.transaq.core.exception.ConnectionException;
import ru.eclipsetrader.transaq.core.interfaces.ITransaqServer;
import ru.eclipsetrader.transaq.core.library.TransaqLibrary;
import ru.eclipsetrader.transaq.core.model.ConnectionStatus;
import ru.eclipsetrader.transaq.core.model.Message;
import ru.eclipsetrader.transaq.core.model.internal.Server;
import ru.eclipsetrader.transaq.core.model.internal.ServerSession;
import ru.eclipsetrader.transaq.core.model.internal.ServerStatus;
import ru.eclipsetrader.transaq.core.orders.TQOrderTradeService;
import ru.eclipsetrader.transaq.core.quotes.TQQuotationService;
import ru.eclipsetrader.transaq.core.quotes.TQQuoteService;
import ru.eclipsetrader.transaq.core.securities.TQSecurityService;
import ru.eclipsetrader.transaq.core.server.command.ChangePasswordCommand;
import ru.eclipsetrader.transaq.core.server.command.Command;
import ru.eclipsetrader.transaq.core.trades.TQTickTradeService;
import ru.eclipsetrader.transaq.core.util.Utils;
import ru.eclipsetrader.transaq.core.xml.handler.XMLHandler;

import com.sun.jna.Pointer;

public class TransaqServer implements ITransaqServer, com.sun.jna.Callback, Closeable {
	
	private static final Logger logger = LogManager.getFormatterLogger(TransaqServer.class);
	
	protected String serverId;
	protected String sessionId;
	
	private SAXParser parser;
	
	private Object statusLock = new Object();
	
	private ServerSession session;
	private Server server;
	
	private boolean wasInitialized = false;

	ThreadGroup eventThreadGroup = new ThreadGroup("ThreadGroup");
	final EventHolder eventHolder = new EventHolder(eventThreadGroup);
	
	private static TransaqServer INSTANCE;
	public static TransaqServer getInstance() {
		return INSTANCE;
	}
	public static void setTransaqServer(TransaqServer transaqServer) {
		INSTANCE = transaqServer;
	}
	
	public final Event<ServerSession> onConnectEstablished = new Event<ServerSession>("TransaqServer.onConnectEstablished", eventThreadGroup);
	public final Event<String> onDisconnected = new Event<String>("TransaqServer.onDisconnected", eventThreadGroup);
	/*public final InstrumentMassEvent<Quote> onQuotesUpdate = new InstrumentMassEvent<Quote>("TransaqServer.onQuotesUpdate", eventThreadGroup);
	 TODO refactor
	public final InstrumentEvent<SecurityPosition> onSecurityPositionUpdate = new InstrumentEvent<SecurityPosition>("TransaqServer.onSecurityPositionUpdate");
	public final InstrumentEvent<MoneyPosition> onMoneyPositionUpdate = new InstrumentEvent<MoneyPosition>("TransaqServer.onMoneyPositionUpdate");
	public final InstrumentEvent<FortsPosition> onFortsPositionUpdate = new InstrumentEvent<FortsPosition>("TransaqServer.onFortsPositionUpdate");
	public final Event<FortsMoneyPosition> onFortsMoneyPositionUpdate = new Event<FortsMoneyPosition>("TransaqServer.onFortsMoneyPositionUpdate");
	public final InstrumentEvent<CandleGraph> onCandlesReceive = new InstrumentEvent<CandleGraph>("TransaqServer.onCandlesReceive");
	public final InstrumentEvent<TickTrade> onTickTradesReceive = new InstrumentEvent<TickTrade>("TransaqServer.onTickTradesReceive", eventThreadGroup);
	public final InstrumentEvent<Trade> onTradeReceive = new InstrumentEvent<Trade>("TransaqServer.onTradeReceive", eventThreadGroup);
	public final InstrumentEvent<Order> onOrderReceive = new InstrumentEvent<Order>("TransaqServer.onOrderReceive", eventThreadGroup);
	*/
	
	/*public TransaqServer(Server server) {
		this.server = server;
		this.session = new ServerSession(server.getId());
		this.transaqLibrary = new TransaqLibrary();
		initExtentions();		

		try {
			this.parser = Utils.getSAXParserFactory().newSAXParser();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private void initExtentions() {
		IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint("ru.eclipsetrader.transaq.core.events.OnTransaqServerEvent");
		List<IConnectionStatus> connectionStatusEvents = new ArrayList<IConnectionStatus>();		
		for (IConfigurationElement configElement : extensionPoint.getConfigurationElements()) {
			try {
				Object connectionStatusInstance = configElement.createExecutableExtension("onConnectionStatus");
				if (connectionStatusInstance instanceof IConnectionStatus) {
					connectionStatusEvents.add((IConnectionStatus)connectionStatusInstance);
				}
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
	}*/
	
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

		eventHolder.onServerStatusChange.addObserver(new Observer<ServerStatus>() {
			@Override
			public void update(ServerStatus newStatus) {
				try {
					updateServerStatus(newStatus);
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					synchronized (statusLock) {
						statusLock.notify();
					}
				}
			}
		});
		
		eventHolder.onMessageReceive.addObserver(new Observer<Message>() {
			@Override
			public void update( Message message) {
				System.err.println(String.format("MESSAGE FROM <%s>: %s", message.getFrom(), message.getText()));				
			}
		});
		
		
		eventHolder.onOvernightChange.addObserver(new Observer<Boolean>() {
			@Override
			public void update(Boolean isOvernight) {
				System.err.println(String.format("isOvernight is %s", isOvernight));
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
		
		eventHolder.onPositionChange.addObserver(TQPositionService.getInstance());
		eventHolder.onOrderReceive.addObserver(TQOrderTradeService.getInstance().getOrderObserver());
		
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
	
	public void persistState() {
		TQBoardService.getInstance().persist();
		TQCandleService.getInstance().persist();
		TQClientService.getInstance().persist();
		TQMarketService.getInstance().persist();
		TQSecurityService.getInstance().persist();
		TQPositionService.getInstance().persist();
	}
	
	public void updateServerStatus(ServerStatus newStatus) {
		if (session.getStatus() != newStatus.getStatus()) {
			session.setStatus(newStatus.getStatus());
			session.setTimezone(newStatus.getTimeZone());
			logger.info("New server status: " + newStatus.getStatus());
			if (session.getStatus() == ConnectionStatus.CONNECTED) {
				session.setConnected(new Date());
				session.setDisconnected(null);
				session.setError(null);
				DataManager.merge(session);
				onConnectEstablished.notifyObservers(session);
			} else if (session.getStatus() == ConnectionStatus.DISCONNECTED) {
				if (newStatus.getError() != null) {
					System.err.println(newStatus.getError());
				}
				session.setError(newStatus.getError());
				session.setDisconnected(new Date());
				session.setStatus(ConnectionStatus.DISCONNECTED);
				DataManager.merge(session);
				onDisconnected.notifyObservers(newStatus.getError());
			}
			
			CoreActivator.getEventAdmin().postEvent(OSGIServerStatusEvent.getEvent(serverId, newStatus));
			
		}
	}
	
	static int counter = 0;
	
	// typedef bool (*tcallback)(BYTE* pData);
	public void callback (Pointer pData) {

		String data = pData.getString(0);	
		TransaqLibrary.FreeMemory(pData);
		
		if ( Settings.SHOW_CONSOLE_TRACE) {
			System.out.println(data);
		}
		
		// DatabaseManager.writeInputEvent(sessionId, data);
		
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
	
	private void init() {
		if (wasInitialized) {
			logger.info("Transaq library was already initialized");
			return;
		}
		TransaqLibrary.Initialize(server.getLogDir(), server.getLogLevel().getLevel());
		if (!TransaqLibrary.SetCallbackEx(this)) {
			TransaqLibrary.UnInitialize();
			throw new RuntimeException("SetCallbackEx failed!");
		};
		wasInitialized = true;
	}
	
	private void initNewSession() {
		sessionId = UUID.randomUUID().toString();
		session.setSessionId(sessionId);		
		session.setStatus(ConnectionStatus.CONNECTING);
	}

	public void connect(String serverId) throws ConnectionException {
		long startTime = System.currentTimeMillis();
		
		if (!wasInitialized) {
			logger.info("Initializing library");
			init();
		}
		
		initNewSession();
		
		try {
			TransaqLibrary.SendCommand(server.createSubscribeCommand());
		} catch (CommandException ex) {
			throw new ConnectionException("Ошибка при подключении к серверу: <" + ex.getMessage()+ ">", ex);
		}
		
		synchronized (statusLock) {
			try {
				statusLock.wait(Constants.CONNECT_TIMEOUT);
				session.setStatus(ConnectionStatus.CONNECTED);
				long endTime = System.currentTimeMillis();
				
				System.err.println(String.format("Connected in %.2fs", (endTime-startTime)/1000.0));
				
			} catch (InterruptedException e) {
				session.setStatus(ConnectionStatus.DISCONNECTED);
				DataManager.merge(session);
				throw new ConnectionException("Подключение не установлено в течение " + Constants.CONNECT_TIMEOUT/1000 + " секунд");
			}
		}
	}

	@Override
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

	public void changePass(String newPass) throws CommandException {
		logger.warn("Changing password!");
		ChangePasswordCommand changePasswordCommand = new ChangePasswordCommand(server.getPassword(), newPass);
		TransaqLibrary.SendCommand(changePasswordCommand.createSubscribeCommand());
	}
	
	public void close() {
		if (wasInitialized) {
			TransaqLibrary.UnInitialize();
		}
		eventThreadGroup.interrupt();
		DatabaseManager.dbThreadGroup.interrupt();
	}
	
	public void callUpdateStatus() {
		TransaqLibrary.SendCommand(Command.SERVER_STATUS);
	}

	@Override
	public String getId() {
		return serverId;
	}
	
	@Override
	public String getSessionId() {
		return sessionId;
	}

}
