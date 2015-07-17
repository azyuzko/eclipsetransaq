package ru.eclipsetrader.transaq.core.xml.handler;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import ru.eclipsetrader.transaq.core.server.EventHolder;

public class XMLHandler extends DefaultHandler {

	ErrorHandler errorHandler;
	MessageHandler messageHandler;
	BoardHandler boardHandler;
	MarketHandler marketHandler;
	CandleKindHandler candleKindHandler;
	SecurityHandler securityHandler;
	PitHandler pitHandler;
	ClientHandler clientHandler;
	PositionHandler positionHandler;
	ServerStatusHandler serverStatusHandler;
	OvernightHandler overnightHandler;
	TickHandler allTradesHandler;
	QuotationsHandler quotationsHandler;
	QuotesHandler quotesHandler;
	TickHandler ticksTradeHandler;
	CandleHandler candleHandler;
	OrderHandler orderHandler;
	SecInfoHandler secInfoHandler;
	SecInfoUpdateHandler secInfoUpdateHandler;
	TradeHandler tradeHandler;

	XMLProcessType type;

	public XMLHandler(String serverId, EventHolder eventHolder) {
		this.errorHandler = new ErrorHandler(eventHolder.onErrorReceive);
		this.messageHandler = new MessageHandler(eventHolder.onMessageReceive);
		this.boardHandler = new BoardHandler(serverId, eventHolder.onBoardsChange);
		this.marketHandler = new MarketHandler(serverId, eventHolder.onMarketsChange);
		this.candleKindHandler = new CandleKindHandler(serverId, eventHolder.onCandleKindChange);
		this.securityHandler = new SecurityHandler(serverId, eventHolder.onSecuritiesChange);
		this.pitHandler = new PitHandler(eventHolder.onPitsChange);
		this.clientHandler = new ClientHandler(serverId, eventHolder.onClientReceive);
		this.positionHandler = new PositionHandler(eventHolder.onPositionChange);
		this.serverStatusHandler = new ServerStatusHandler(eventHolder.onServerStatusChange);
		this.overnightHandler = new OvernightHandler(eventHolder.onOvernightChange);
		this.allTradesHandler = new TickHandler(eventHolder.onAllTradeChange);
		this.quotationsHandler = new QuotationsHandler(eventHolder.onQuotationsChange);
		this.quotesHandler = new QuotesHandler(eventHolder.onQuotesChange);
		this.ticksTradeHandler = new TickHandler(eventHolder.onTickTradeChange);
		this.candleHandler = new CandleHandler(eventHolder.onCandleGraphReceive);
		this.secInfoUpdateHandler = new SecInfoUpdateHandler(eventHolder.onSecInfoUpdate);
		this.tradeHandler = new TradeHandler(eventHolder.onTradeChange);
		this.orderHandler = new OrderHandler(eventHolder.onOrderReceive, eventHolder.onStopOrderReceive);
	}

	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {

		
		if (type == null) {

			switch (QNAME.valueOf(qName)) {
			
			case error:
				type = XMLProcessType.ERROR;
				break;
				
			case messages:
				type = XMLProcessType.MESSAGES;
				break;

			case markets:
				type = XMLProcessType.MARKETS;
				break;

			case boards:
				type = XMLProcessType.BOARDS;
				break;
				
			case candlekinds:
				type = XMLProcessType.CANDLEKINDS;
				break;
				
			case securities:
				type = XMLProcessType.SECURITIES;
				break;
				
			case pits:
				type = XMLProcessType.PITS;
				break;
				
			case client:
				type = XMLProcessType.CLIENT;
				break;
				
			case positions:
				type = XMLProcessType.POSITIONS;
				break;

			case server_status:
				type = XMLProcessType.SERVER_STATUS;
				break;
				
			case overnight:
				type = XMLProcessType.OVERNIGHT;
				break;
				
			case alltrades:
				type = XMLProcessType.ALLTRADES;
				break;
				
			case quotations:
				type = XMLProcessType.QUOTATIONS;
				break;
				
			case quotes:
				type = XMLProcessType.QUOTES;
				break;
				
			case ticks:
				type = XMLProcessType.TICKS;
				break;
				
			case candles:
				type = XMLProcessType.CANDLES;
				break;
				
			case sec_info:
				type = XMLProcessType.SEC_INFO;
				break;
				
			case sec_info_upd:
				type = XMLProcessType.SEC_INFO_UPD;
				break;
				
			case orders:
				type = XMLProcessType.ORDERS;
				break;
				
			case news_header:
				type = XMLProcessType.NEWS_HEADER;
				break;
				
			case trades:
				type = XMLProcessType.TRADES;
				break;
				
			default:
				throw new RuntimeException("Unknown data received, attribute = " + qName);
			}
		} 
		
		switch (type) {
		
		case ERROR:
			errorHandler.startElement(qName, attributes);
			break;
		
		case MESSAGES:
			messageHandler.startElement(qName, attributes);
			break;
			
		case MARKETS:
			marketHandler.startElement(qName, attributes);
			break;
			
		case BOARDS:
			boardHandler.startElement(qName, attributes);
			break;
			
		case CANDLEKINDS:
			candleKindHandler.startElement(qName, attributes);
			break;
			
		case SECURITIES:
			securityHandler.startElement(qName, attributes);
			break;
		
		case PITS:
			pitHandler.startElement(qName, attributes);
			break;
			
		case CLIENT:
			clientHandler.startElement(qName, attributes);
			break;
			
		case POSITIONS:
			positionHandler.startElement(qName, attributes);
			break;
			
		case SERVER_STATUS:
			serverStatusHandler.startElement(qName, attributes);
			break;
		
		case OVERNIGHT:
			overnightHandler.startElement(qName, attributes);
			break;
			
		case ALLTRADES:
			allTradesHandler.startElement(qName, attributes);
			break;
			
		case QUOTATIONS:
			quotationsHandler.startElement(qName, attributes);
			break;
			
		case QUOTES:
			quotesHandler.startElement(qName, attributes);
			break;
			
		case TICKS:
			ticksTradeHandler.startElement(qName, attributes);
			break;
		
		case CANDLES:
			candleHandler.startElement(qName, attributes);
			break;
		
		case SEC_INFO:
			secInfoHandler.startElement(qName, attributes);
			break;
			
		case SEC_INFO_UPD:
			secInfoUpdateHandler.startElement(qName, attributes);
			break;
			
		case ORDERS:
			orderHandler.startElement(qName, attributes);
			break;
			
		case NEWS_HEADER:
			break;
			
		case TRADES:
			tradeHandler.startElement(qName, attributes);
			break;
					
		default:
			break;
		}
	}

	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		
		switch (type) {
		
		case ERROR:
			errorHandler.endElement(qName);
			break;
			
		case MESSAGES:
			messageHandler.endElement(qName);
			break;

		case MARKETS:
			marketHandler.endElement(qName);
			break;

		case BOARDS:
			boardHandler.endElement(qName);
			break;
			
		case CANDLEKINDS:
			candleKindHandler.endElement(qName);
			break;
	
		case SECURITIES:
			securityHandler.endElement(qName);
			break;
		
		case PITS:
			pitHandler.endElement(qName);
			break;
			
		case CLIENT:
			clientHandler.endElement(qName);
			break;
			
		case POSITIONS:
			positionHandler.endElement(qName);
			break;
			
		case SERVER_STATUS:
			serverStatusHandler.endElement(qName);
			break;
			
		case OVERNIGHT:
			overnightHandler.endElement(qName);
			break;
			
		case ALLTRADES:
			allTradesHandler.endElement(qName);
			break;
			
		case QUOTATIONS:
			quotationsHandler.endElement(qName);
			break;
			
		case QUOTES:
			quotesHandler.endElement(qName);
			break;
			
		case TICKS:
			ticksTradeHandler.endElement(qName);
			break;
			
		case CANDLES:
			candleHandler.endElement(qName);
			break;
			
		case SEC_INFO:
			secInfoHandler.endElement(qName);
			break;
			
		case SEC_INFO_UPD:
			secInfoUpdateHandler.endElement(qName);
			break;
			
		case ORDERS:
			orderHandler.endElement(qName);
			break;
			
		case NEWS_HEADER:
			break;
			
		case TRADES:
			tradeHandler.endElement(qName);
			break;
			
		default:
			break;
		}
	}

	public void characters(char[] paramArrayOfChar, int start, int length)
			throws SAXException {
		
		String value = new String(paramArrayOfChar, start, length).trim();

		switch (type) {
		
		case ERROR:
			errorHandler.characters(value);
			break;
		
		case MESSAGES:
			messageHandler.characters(value);
			break;

		case MARKETS:
			marketHandler.characters(value);
			break;

		case BOARDS:
			boardHandler.characters(value);
			break;
			
		case CANDLEKINDS:
			candleKindHandler.characters(value);
			break;

		case SECURITIES:
			securityHandler.characters(value);
			break;
			
		case PITS:
			pitHandler.characters(value);
			break;
			
		case CLIENT:
			clientHandler.characters(value);
			break;
			
		case POSITIONS:
			positionHandler.characters(value);
			break;
			
		case SERVER_STATUS:
			serverStatusHandler.characters(value);
			break;
			
		case OVERNIGHT:
			overnightHandler.characters(value);
			break;
		
		case ALLTRADES:
			allTradesHandler.characters(value);
			break;
			
		case QUOTATIONS:
			quotationsHandler.characters(value);
			break;
		
		case QUOTES:
			quotesHandler.characters(value);
			break;
		
		case TICKS:
			ticksTradeHandler.characters(value);
			break;
		
		case CANDLES:
			candleHandler.characters(value);
			break;
			
		case SEC_INFO:
			secInfoHandler.characters(value);
			break;
			
		case SEC_INFO_UPD:
			secInfoUpdateHandler.characters(value);
			break;
			
		case ORDERS:
			orderHandler.characters(value);
			break;
			
		case NEWS_HEADER:
			break;
		
		case TRADES:
			tradeHandler.characters(value);
			break;
			
		default:
			break;
		}

	}
	
	
	@Override
	public void error(SAXParseException e) throws SAXException {
		System.err.println("ERROR " + e.getColumnNumber());
	}
	
	@Override
	public void warning(SAXParseException e) throws SAXException {
		System.err.println("WARNING " + e.getColumnNumber());
	}
	
	@Override
	public void fatalError(SAXParseException e) throws SAXException {
		System.err.println("FATAL " + e.getColumnNumber());
	}

}
