package ru.eclipsetrader.transaq.core.server;

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

public final class EventHolder {

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
	public final Event<SymbolGapMap> onQuotationsChange;
	public final ListEvent<SymbolGapMap> onQuotesChange;
	public final ListEvent<TickTrade> onTickTradeChange;
	public final Event<CandleGraph> onCandleGraphReceive;
	public final Event<SecInfoUpdate> onSecInfoUpdate;
	public final Event<Trade> onTradeChange;
	public final Event<Order> onOrderReceive;
	public final Event<StopOrder> onStopOrderReceive;
	
	public EventHolder(ThreadGroup threadGroup) {
		onErrorReceive = new Event<String>("EventHolder.onErrorReceive", threadGroup);
		onMessageReceive = new Event<Message>("EventHolder.onMessageReceive", threadGroup);
		onServerStatusChange = new Event<ServerStatus>("EventHolder.onServerStatusChange", threadGroup);
		onOvernightChange = new Event<Boolean>("EventHolder.onOvernightChange", threadGroup);
		onMarketsChange = new ListEvent<Market>("EventHolder.onMarketsChange", threadGroup);
		onCandleKindChange = new ListEvent<CandleKind>("EventHolder.onCandleKindChange", threadGroup);
		onBoardsChange = new ListEvent<Board>("EventHolder.onBoardsChange", threadGroup);
		onClientReceive = new Event<Client>("EventHolder.onClientReceive", threadGroup);
		onPitsChange = new ListEvent<Pit>("EventHolder.onPitsChange", threadGroup);
		onSecuritiesChange = new ListEvent<Security>("EventHolder.onSecuritiesChange", threadGroup);
		onPositionChange = new Event<Holder<PositionType,Map<String,String>>>("EventHolder.onPositionChange", threadGroup);
		onAllTradeChange = new ListEvent<TickTrade>("EventHolder.onAllTradeChange", threadGroup);
		onQuotationsChange = new Event<SymbolGapMap>("EventHolder.onQuotationsChange", threadGroup);
		onQuotesChange = new ListEvent<SymbolGapMap>("EventHolder.onQuotesChange", threadGroup);
		onTickTradeChange = new ListEvent<TickTrade>("EventHolder.onTickTradeChange", threadGroup);
		onCandleGraphReceive = new Event<CandleGraph>("EventHolder.onCandleGraphReceive", threadGroup);
		onSecInfoUpdate = new Event<SecInfoUpdate>("EventHolder.onSecInfoUpdate", threadGroup);
		onTradeChange = new Event<Trade>("EventHolder.onTradeChange", threadGroup);
		onOrderReceive = new Event<Order>("EventHolder.onOrderReceive", threadGroup);
		onStopOrderReceive = new Event<StopOrder>("EventHolder.onStopOrderReceive", threadGroup);
	}
	
}
