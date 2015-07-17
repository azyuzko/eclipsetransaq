package ru.eclipsetrader.transaq.core.osgi;

import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;

import ru.eclipsetrader.transaq.core.CoreActivator;
import ru.eclipsetrader.transaq.core.Settings;
import ru.eclipsetrader.transaq.core.candle.CandleStorage;
import ru.eclipsetrader.transaq.core.candle.CandleType;
import ru.eclipsetrader.transaq.core.instruments.Instrument;
import ru.eclipsetrader.transaq.core.instruments.TQInstrumentService;
import ru.eclipsetrader.transaq.core.interfaces.ITransaqServer;
import ru.eclipsetrader.transaq.core.model.BoardType;
import ru.eclipsetrader.transaq.core.model.TQSymbol;
import ru.eclipsetrader.transaq.core.model.internal.Order;
import ru.eclipsetrader.transaq.core.model.internal.Quotation;
import ru.eclipsetrader.transaq.core.model.internal.Security;
import ru.eclipsetrader.transaq.core.model.internal.Trade;
import ru.eclipsetrader.transaq.core.orders.TQOrderTradeService;
import ru.eclipsetrader.transaq.core.quotes.TQQuotationService;
import ru.eclipsetrader.transaq.core.quotes.TQQuoteService;
import ru.eclipsetrader.transaq.core.securities.TQSecurityService;
import ru.eclipsetrader.transaq.core.services.ITransaqServerManager;
import ru.eclipsetrader.transaq.core.trades.TQTickTradeService;

public class TransaqCommandProvider implements CommandProvider {

	enum OnState {
		ON(true), OFF(false);
		
		boolean state = false;
		OnState(boolean isOn) {
			this.state = isOn;
		}
		
		boolean getBoolean() {
			return state;
		}
	}
	
	public void _transaq(CommandInterpreter ci) {
		String arg = ci.nextArgument();
		if (arg == null) {
			ci.println("Argument required");
			return;
		}

		ITransaqServerManager tsm = CoreActivator
				.getServiceInstance(ITransaqServerManager.class);

		switch (arg.toLowerCase()) {
		case "connect": {
			String serverId = ci.nextArgument();
			if (serverId == null) {
				ci.println("Server ID required");
			}
			tsm.connect(new String(serverId).toUpperCase());
			break;
		}

		case "disconnect": {
			tsm.disconnect();
			break;
		}

		case "status": {
			ITransaqServer ts = tsm.getActiveTransaqServer();
			if (ts != null) {
				ci.println("Connected to " + ts.getId() + " status=<" + ts.getStatus()+">");
			} else {
				ci.println("Not connected to server");
			}
			break;
		}
		
		case "subscribe" :	{
			String what = ci.nextArgument();
			final String board = ci.nextArgument();
			final String seccode = ci.nextArgument();
			if (what == null || board == null || seccode == null) {
				ci.println("Argument required");
				return;
			}
			switch (what) {
			case "alltrades":
				TQTickTradeService.getInstance().subscribeAllTrades(new TQSymbol(board, seccode));	
				break;
			
			case "ticks":
				TQTickTradeService.getInstance().subscribeTicks(new TQSymbol(board, seccode));	
				break;
				
			case "quotations":
				TQQuotationService.getInstance().subscribe(new TQSymbol(board, seccode));
				break;
				
			case "quotes" :
				TQQuoteService.getInstance().subscribe(new TQSymbol(board, seccode));
				break;

			default:
				break;
			}
			
			break;
		}
		
		case "show" : {
			String what = ci.nextArgument();
			if (what == null) {
				ci.println("Argument required");
				return;
			}
			switch (what) {
			case "orders": {
				TQOrderTradeService ots = TQOrderTradeService.getInstance();
				for (Order o : ots.getOrders()) {
					ci.println(o.toString().replaceAll("\n", ""));
				}
				break;
			}
			case "trades" : {
				TQOrderTradeService ots = TQOrderTradeService.getInstance();
				for (Trade t : ots.getTrades()) {
					ci.println(t.toString().replaceAll("\n", ""));
				}
				break;
			}
			case "securities" : {
				TQSecurityService ss = TQSecurityService.getInstance();
				for (Security s : ss.getAll()) {
					ci.println(s.toString().replaceAll("\n", ""));
				}
				break;
			}
			case "quotations" : {
				TQQuotationService qs = TQQuotationService.getInstance();
				for (Quotation q : qs.getQuotations()) {
					ci.println(q.toString());
				}
				break;
			}			
			case "trace": {
				String onOff = ci.nextArgument();
				if (onOff == null) {
					ci.println(Settings.SHOW_CONSOLE_TRACE);
					return;
				}
				Settings.SHOW_CONSOLE_TRACE = OnState.valueOf(onOff.toUpperCase()).getBoolean();
				ci.println("Show console trace has set <" + Settings.SHOW_CONSOLE_TRACE + ">");
				break;
			}
			
			case "inst": {
				final String board = ci.nextArgument();
				final String seccode = ci.nextArgument();
				
				Instrument i = TQInstrumentService.getInstance().getInstrument(new TQSymbol(board, seccode));
				ci.println( i.getQuoteGlass().toString() );
				
				CandleStorage cs = i.getCandleStorage();
				ci.println(cs.toString());
				
				break;
			}
			
			default:
				break;
			}
			break;
		}
		
		case "test": {
			TQSymbol symbol = new TQSymbol(BoardType.FUT, "SiU5");
			/*Instrument i = TQInstrumentService.getInstance().createInstrument(symbol);
			i.addCandleType(CandleType.CANDLE_1H);*/
			TQTickTradeService.getInstance().subscribeTicks(symbol);	
			break;
		}

		default:
			ci.println("Unknown command " + arg);
		}

	}

	public String getHelp() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("---Transaq Commands---\n");
		buffer.append("\ttransaq connect <ID>\n");
		buffer.append("\ttransaq disconnect\n");
		buffer.append("\ttransaq status\n");
		buffer.append("\ttransaq show\n");
		buffer.append("\t\t\t orders\n");
		buffer.append("\t\t\t trades\n");
		buffer.append("\t\t\t securities\n");
		return buffer.toString();
	}
}