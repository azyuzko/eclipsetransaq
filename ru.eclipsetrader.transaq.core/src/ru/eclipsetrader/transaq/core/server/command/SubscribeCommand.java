package ru.eclipsetrader.transaq.core.server.command;

import java.util.HashSet;
import java.util.Set;

import ru.eclipsetrader.transaq.core.model.TQSymbol;

/**
 * Класс для формирования подписки на инструменты 
 * @author Zyuzko-AA
 *
 */
public class SubscribeCommand extends BaseCommand {
	
	Set<TQSymbol> allTrades = new HashSet<TQSymbol>();
	Set<TQSymbol> quotations = new HashSet<TQSymbol>();
	Set<TQSymbol> quotes = new HashSet<TQSymbol>();
	
	public void subscribeAllTrades(TQSymbol symbol) {
		allTrades.add(symbol);
	}

	public void subscribeQuotations(TQSymbol symbol) {
		quotations.add(symbol);
	}

	public void subscribeQuotes(TQSymbol symbol) {
		quotes.add(symbol);
	}

	public Set<TQSymbol> getAllTrades() {
		return allTrades;
	}

	public Set<TQSymbol> getQuotations() {
		return quotations;
	}

	public Set<TQSymbol> getQuotes() {
		return quotes;
	}

	public String createSubscribeCommand() {
		return createCommand("subscribe");
	}
	
	public String createUnsubscribeCommand() {
		return createCommand("unsubscribe");
	}
	
	private String createCommand(String type) {
		StringBuilder sb = new StringBuilder();
		sb.append("<command id=\"");
		sb.append(type.toLowerCase());
		sb.append("\">");
		if (allTrades.size() > 0) {
			sb.append("<alltrades>");
				for (TQSymbol security : allTrades) {
					sb.append("<security>");
						sb.append("<board>"); sb.append(security.getBoard()); sb.append("</board>"); 
						sb.append("<seccode>"); sb.append(security.getSeccode()); sb.append("</seccode>"); 						
					sb.append("</security>");
				}
			sb.append("</alltrades>");
		}
		
		if (quotations.size() > 0) {
			sb.append("<quotations>");
			for (TQSymbol security : quotations) {
				sb.append("<security>");
					sb.append("<board>"); sb.append(security.getBoard()); sb.append("</board>"); 
					sb.append("<seccode>"); sb.append(security.getSeccode()); sb.append("</seccode>"); 						
				sb.append("</security>");
			}
			sb.append("</quotations>");
		}
		
		if (quotes.size() > 0) {
			sb.append("<quotes>");
			for (TQSymbol security : quotes) {
				sb.append("<security>");
					sb.append("<board>"); sb.append(security.getBoard()); sb.append("</board>"); 
					sb.append("<seccode>"); sb.append(security.getSeccode()); sb.append("</seccode>"); 						
				sb.append("</security>");
			}
			sb.append("</quotes>");
		}
		
		sb.append("</command>");
		
		return sb.toString();
	}
	
}
