package ru.eclipsetrader.transaq.core.xml.handler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import ru.eclipsetrader.transaq.core.event.Event;
import ru.eclipsetrader.transaq.core.model.Candle;
import ru.eclipsetrader.transaq.core.model.internal.CandleGraph;
import ru.eclipsetrader.transaq.core.model.internal.CandleStatus;
import ru.eclipsetrader.transaq.core.util.Utils;

public class CandleHandler extends BaseXMLProcessor<CandleGraph> {

	public static Logger logger = LogManager.getLogger(CandleHandler.class);
	
	public CandleHandler(Event<CandleGraph> bundleNotifier) {
		super(bundleNotifier);
	}

	@Override
	void startElement(String qName, Attributes attributes) throws SAXException {

		elementStack.push(qName);

		switch (QNAME.valueOf(qName)) {
		case candles :
			CandleGraph candleGraph = new CandleGraph();
			candleGraph.setPeriod(Integer.valueOf(attributes.getValue("period")));
			candleGraph.setStatus(CandleStatus.getFromValue(attributes.getValue("status")));
			candleGraph.setBoard(attributes.getValue("board"));
			candleGraph.setSeccode(attributes.getValue("seccode"));
			objectStack.push(candleGraph);
			break;
			
		case candle	:
			Candle candle = new Candle();
			candle.setDate(Utils.parseDate(attributes.getValue("date")));
			candle.setOpen(Double.valueOf(attributes.getValue("open")));
			candle.setHigh(Double.valueOf(attributes.getValue("high")));
			candle.setLow(Double.valueOf(attributes.getValue("low")));
			candle.setClose(Double.valueOf(attributes.getValue("close")));
			candle.setVolume(Integer.valueOf(attributes.getValue("volume")));
			String oi = attributes.getValue("oi");
			if (oi != null) {
				candle.setOi(Integer.valueOf(oi));
			}
			objectStack.peek().getCandles().add(candle);
			break;
		default:
			break;
		}
	}

	@Override
	void endElement(String qName) throws SAXException {

		elementStack.pop();
		
		switch (QNAME.valueOf(qName)) {
		
		case candles :
			notifyCompleteElement(objectStack.pop());
			break;

		default:
			break;
		}
	}

	@Override
	void characters(String value) throws SAXException {
		
	}

}
