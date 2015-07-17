package ru.eclipsetrader.transaq.core.xml.handler;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import ru.eclipsetrader.transaq.core.event.Event;
import ru.eclipsetrader.transaq.core.model.ConnectionStatus;
import ru.eclipsetrader.transaq.core.model.internal.ServerStatus;

public class ServerStatusHandler extends BaseXMLProcessor<ServerStatus> {

	public ServerStatusHandler(Event<ServerStatus> notifier) {
		super(notifier);
	}

	private boolean isError = false;

	public void startElement(String qName, Attributes attributes)
			throws SAXException {
		String connectedValue = attributes.getValue("connected");
		ServerStatus serverStatus = new ServerStatus();
		if ("true".equals(connectedValue)) {
			serverStatus.setStatus(ConnectionStatus.CONNECTED);
			serverStatus.setTimeZone(attributes.getValue("server_tz"));
			String sId = attributes.getValue("id");
			if (sId != null) {
				serverStatus.setId(Integer.valueOf(sId));
			}
		} else if ("error".equals(connectedValue)) {
			isError = true;
		} else {
			serverStatus.setStatus(ConnectionStatus.DISCONNECTED);
		}
		objectStack.push(serverStatus);
	}

	public void endElement(String qName) throws SAXException {
		notifyCompleteElement(objectStack.pop());
	}

	public void characters(String value) throws SAXException {
		if (isError) {
			try {
				objectStack.peek().setError(value);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
