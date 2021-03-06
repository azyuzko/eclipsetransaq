package ru.eclipsetrader.transaq.core.osgi;

import java.util.Arrays;
import java.util.List;

import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;

import ru.eclipsetrader.transaq.core.Settings;
import ru.eclipsetrader.transaq.core.candle.Candle;
import ru.eclipsetrader.transaq.core.candle.CandleType;
import ru.eclipsetrader.transaq.core.candle.TQCandleService;
import ru.eclipsetrader.transaq.core.exception.ConnectionException;
import ru.eclipsetrader.transaq.core.model.BoardType;
import ru.eclipsetrader.transaq.core.model.BuySell;
import ru.eclipsetrader.transaq.core.model.TQSymbol;
import ru.eclipsetrader.transaq.core.model.internal.Order;
import ru.eclipsetrader.transaq.core.model.internal.Security;
import ru.eclipsetrader.transaq.core.model.internal.Trade;
import ru.eclipsetrader.transaq.core.orders.OrderCallback;
import ru.eclipsetrader.transaq.core.orders.OrderRequest;
import ru.eclipsetrader.transaq.core.orders.TQOrderTradeService;
import ru.eclipsetrader.transaq.core.quotes.TQQuotationService;
import ru.eclipsetrader.transaq.core.quotes.TQQuoteService;
import ru.eclipsetrader.transaq.core.securities.TQSecurityService;
import ru.eclipsetrader.transaq.core.server.TransaqScheduleService;
import ru.eclipsetrader.transaq.core.server.TransaqServer;
import ru.eclipsetrader.transaq.core.strategy.Strategy;
import ru.eclipsetrader.transaq.core.trades.TQTickTradeService;
import ru.eclipsetrader.transaq.core.util.Utils;

import com.investing.InvestingStrategy;
import com.investing.SignalProcessor;

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

		switch (arg.toLowerCase()) {
		case "connect": {
			try {
				TransaqServer.getInstance().connect( c -> {});
			} catch (ConnectionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;
		}

		case "disconnect": {
			TransaqServer.getInstance().disconnect();
			break;
		}

		case "status": {
			TransaqServer ts = TransaqServer.getInstance();
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
				ci.println("unimplemented");
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
			case "memory": {
				ci.println( Utils.getMemoryDetails() );
				break;
			}
			
			case "candlekinds": {
				for (CandleType candleType : TQCandleService.getInstance().getCandleTypes()) {
					ci.println(candleType);
				}
				break;
			}
			
			case "quoteglass": {
				String board = ci.nextArgument();
				String seccode = ci.nextArgument();
				
				if (board == null && seccode == null) {
					ci.println("Argument required");
					return;
				}
				
				break;
			}
			
			default:
				break;
			}
			break;
		}
		
		case "subscribeall": {
			TQTickTradeService.getInstance().subscribeAllTrades(TQSymbol.workingSymbolSet());
			TQQuoteService.getInstance().subscribe(TQSymbol.workingSymbolSet());
			//TQQuotationService.getInstance().subscribe(TQSymbol.workingSymbolSet());
			break;
		}
		
		case "loadcandles": {
			String code = ci.nextArgument();
			TQSymbol symbol = new TQSymbol(BoardType.FUT, code);
			
			for (CandleType candleType : Arrays.asList(CandleType.CANDLE_1M, CandleType.CANDLE_5M, CandleType.CANDLE_15M, CandleType.CANDLE_1H, CandleType.CANDLE_1D)) {
				System.out.println("get candles for " + symbol + " " + candleType);
				int count = 10000;
				List<Candle> candles = TQCandleService.getInstance().getHistoryData(symbol, candleType, count, true);
				System.out.println("received candles from " + candles.get(0).getDate() + " to " + candles.get(candles.size()-1).getDate());
			}
			break;
		}
	
		case "createorder": {
			/*SignalProcessor sp = new SignalProcessor(TQSymbol.EDZ5, 1);
			sp.init();*/
			OrderRequest orderRequest = OrderRequest.createRequest(TQSymbol.EDZ5, BuySell.S, 1.1255, 1);
			TQOrderTradeService.getInstance().createOrder(orderRequest, new OrderCallback());
			/*sp.processOrder(orderRequest);*/
			break;
		}
		
		case "moveorder": {
			String orderno = ci.nextArgument();
			Double price = Double.valueOf(ci.nextArgument());
			//Integer quantity = Integer.valueOf(ci.nextArgument());
			TQOrderTradeService.getInstance().moveOrder(orderno, price);
			break;
		}
		
		case "start": {
			TransaqScheduleService tss = new TransaqScheduleService();
			tss.start();
			break;
		}
		
		case "run": {
			InvestingStrategy hrd = new InvestingStrategy();
			hrd.start();
			break;
		}

		default:
			ci.println("Unknown command " + arg);
		}

	}

	Strategy strategy;

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