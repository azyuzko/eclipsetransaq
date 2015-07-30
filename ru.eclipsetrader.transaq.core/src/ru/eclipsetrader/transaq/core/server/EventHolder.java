package ru.eclipsetrader.transaq.core.server;

import java.io.Closeable;
import java.io.IOException;
import java.util.Map;

import ru.eclipsetrader.transaq.core.event.Event;
import ru.eclipsetrader.transaq.core.event.ListEvent;
import ru.eclipsetrader.transaq.core.model.Message;
import ru.eclipsetrader.transaq.core.model.PositionType;
import ru.eclipsetrader.transaq.core.model.internal.Board;
import ru.eclipsetrader.transaq.core.model.internal.CandleGraph;
import ru.eclipsetrader.transaq.core.model.internal.CandleKind;
import ru.eclipsetrader.transaq.core.model.internal.Client;
import ru.eclipsetrader.transaq.core.model.internal.Market;
import ru.eclipsetrader.transaq.core.model.internal.Order;
import ru.eclipsetrader.transaq.core.model.internal.Pit;
import ru.eclipsetrader.transaq.core.model.internal.SecInfoUpdate;
import ru.eclipsetrader.transaq.core.model.internal.Security;
import ru.eclipsetrader.transaq.core.model.internal.ServerStatus;
import ru.eclipsetrader.transaq.core.model.internal.StopOrder;
import ru.eclipsetrader.transaq.core.model.internal.SymbolGapMap;
import ru.eclipsetrader.transaq.core.model.internal.TickTrade;
import ru.eclipsetrader.transaq.core.model.internal.Trade;
import ru.eclipsetrader.transaq.core.util.Holder;

public final class EventHolder implements Closeable {

	public final Event<String> onErrorReceive;
	public final Event<Message> onMessageReceive;
	public final Event<ServerStatus> onServerStatusChange;
	public final Event<Boolean> onOvernightChange;
	public final ListEvent<Market> onMarketsChange;
	public final ListEvent<CandleKind> onCandleKindChange;
	public final ListEvent<Board> onBoardsChange;
	public final Event<Client> onClientReceive;
	public final ListEvent<Pit> onPitsChange;
	public final ListEvent<Security> onSecuritiesChange;
	public final Event<Holder<PositionType, Map<String, String>>> onPositionChange;
	public final ListEvent<TickTrade> onAllTradeChange;
	public final ListEvent<SymbolGapMap> onQuotationsChange;
	public final ListEvent<SymbolGapMap> onQuotesChange;
	public final ListEvent<TickTrade> onTickTradeChange;
	public final Event<CandleGraph> onCandleGraphReceive;
	public final Event<SecInfoUpdate> onSecInfoUpdate;
	public final Event<Trade> onTradeChange;
	public final Event<Order> onOrderReceive;
	public final Event<StopOrder> onStopOrderReceive;
	
	public EventHolder() {
		onErrorReceive = new Event<String>("EventHolder.onErrorReceive");
		onMessageReceive = new Event<Message>("EventHolder.onMessageReceive");
		onServerStatusChange = new Event<ServerStatus>("EventHolder.onServerStatusChange");
		onOvernightChange = new Event<Boolean>("EventHolder.onOvernightChange");
		onMarketsChange = new ListEvent<Market>("EventHolder.onMarketsChange");
		onCandleKindChange = new ListEvent<CandleKind>("EventHolder.onCandleKindChange");
		onBoardsChange = new ListEvent<Board>("EventHolder.onBoardsChange");
		onClientReceive = new Event<Client>("EventHolder.onClientReceive");
		onPitsChange = new ListEvent<Pit>("EventHolder.onPitsChange");
		onSecuritiesChange = new ListEvent<Security>("EventHolder.onSecuritiesChange");
		onPositionChange = new Event<Holder<PositionType,Map<String,String>>>("EventHolder.onPositionChange");
		onAllTradeChange = new ListEvent<TickTrade>("EventHolder.onAllTradeChange");
		onQuotationsChange = new ListEvent<SymbolGapMap>("EventHolder.onQuotationsChange");
		onQuotesChange = new ListEvent<SymbolGapMap>("EventHolder.onQuotesChange");
		onTickTradeChange = new ListEvent<TickTrade>("EventHolder.onTickTradeChange");
		onCandleGraphReceive = new Event<CandleGraph>("EventHolder.onCandleGraphReceive");
		onSecInfoUpdate = new Event<SecInfoUpdate>("EventHolder.onSecInfoUpdate");
		onTradeChange = new Event<Trade>("EventHolder.onTradeChange");
		onOrderReceive = new Event<Order>("EventHolder.onOrderReceive");
		onStopOrderReceive = new Event<StopOrder>("EventHolder.onStopOrderReceive");
	}

	@Override
	public void close() throws IOException {
		onErrorReceive.deleteObservers();
		onMessageReceive.deleteObservers();
		onServerStatusChange.deleteObservers();
		onOvernightChange.deleteObservers();
		onMarketsChange.deleteObservers();
		onCandleKindChange.deleteObservers();
		onBoardsChange.deleteObservers();
		onClientReceive.deleteObservers();
		onPitsChange.deleteObservers();
		onSecuritiesChange.deleteObservers();
		onPositionChange.deleteObservers();
		onAllTradeChange.deleteObservers();
		onQuotationsChange.deleteObservers();
		onQuotesChange.deleteObservers();
		onTickTradeChange.deleteObservers();
		onCandleGraphReceive.deleteObservers();
		onSecInfoUpdate.deleteObservers();
		onTradeChange.deleteObservers();
		onOrderReceive.deleteObservers();
		onStopOrderReceive.deleteObservers();
		
	}
	
}
