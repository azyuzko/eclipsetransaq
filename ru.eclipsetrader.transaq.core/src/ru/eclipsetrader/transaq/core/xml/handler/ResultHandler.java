package ru.eclipsetrader.transaq.core.xml.handler;

import java.util.Stack;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import ru.eclipsetrader.transaq.core.model.internal.CommandResult;

public class ResultHandler extends DefaultHandler {

	protected Stack<String> elementStack = new Stack<String>();
	protected Stack<CommandResult> objectStack  = new Stack<CommandResult>();
	
	private CommandResult result = null;
	
	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException
	{
		elementStack.push(qName);
		switch (QNAME.valueOf(qName)) {
		
		case error: {
			CommandResult commandResult = new CommandResult();
			commandResult.setSuccess(false);
			objectStack.push(commandResult);
			break;
		}
		
		case result: {
			CommandResult commandResult = new CommandResult();
			commandResult.setSuccess(Boolean.valueOf(attributes.getValue("success")));
			if (attributes.getIndex("transactionid") > -1) {
				commandResult.setTransactionId(attributes.getValue("transactionid"));
			}
			objectStack.push(commandResult);
			break;
		}

		default:
			break;
		}
	}
	
	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		elementStack.pop();
		switch (QNAME.valueOf(qName)) {
		case error:
		case result:
			result = objectStack.pop();
			break;

		default:
			break;
		}
	}
	
	@Override
	public void characters(char[] paramArrayOfChar, int start, int length)
			throws SAXException {
		String value = new String(paramArrayOfChar, start, length).trim();
		switch (QNAME.valueOf(elementStack.peek())) {
		case error:
		case message:
			objectStack.peek().setMessage(value);
			break;

		default:
			break;
		}
	}
	
	public CommandResult getCommandResult() {
		return result;
	}
}
