package ru.eclipsetrader.transaq.core;

import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

import ru.eclipsetrader.transaq.core.candle.Candle;
import ru.eclipsetrader.transaq.core.candle.CandleList;
import ru.eclipsetrader.transaq.core.candle.CandleType;
import ru.eclipsetrader.transaq.core.candle.TQCandleService;
import ru.eclipsetrader.transaq.core.data.DataManager;
import ru.eclipsetrader.transaq.core.indicators.ADX;
import ru.eclipsetrader.transaq.core.indicators.MA;
import ru.eclipsetrader.transaq.core.indicators.MACD;
import ru.eclipsetrader.transaq.core.indicators.MAMA;
import ru.eclipsetrader.transaq.core.indicators.Momentum;
import ru.eclipsetrader.transaq.core.indicators.RSI;
import ru.eclipsetrader.transaq.core.model.PriceType;
import ru.eclipsetrader.transaq.core.model.TQSymbol;
import ru.eclipsetrader.transaq.core.util.Utils;

import com.tictactec.ta.lib.MAType;

public class Test {
	public static void main(String[] args) {
		TQSymbol symbol = TQSymbol.RIZ5;
		CandleType ct_1H = CandleType.CANDLE_1H;
		CandleType ct_15M = CandleType.CANDLE_15M;
		CandleType ct_1M = CandleType.CANDLE_1M;
		Date fromDate = Utils.parseDate("31.08.2015 00:00:00.000");
		Date toDate = Utils.parseDate("02.09.2015 23:00:00.000");
		
		CandleList cl = new CandleList(symbol, ct_1H);
		List<Candle> list = DataManager.getCandles(symbol, ct_15M, fromDate, toDate);
		list = TQCandleService.convertCandleList(ct_15M, CandleType.CANDLE_30M, list);
		System.out.println(list.size());
		cl.setMaxSize(100);
		cl.appendCandles(list);
		
		double[] values = cl.streamPrice(PriceType.MED).toArray();
		
		Momentum mom = new Momentum(6);
		mom.evaluate(values);
		
		RSI rsi = new RSI(14);
		rsi.evaluate(values);
		
		ADX adx = new ADX(14);
		adx.evaluate(cl.streamPrice(PriceType.HIGH), cl.streamPrice(PriceType.LOW), cl.streamPrice(PriceType.CLOSE));
		
		MA trima = new MA(6, MAType.Trima);
		trima.evaluate(values);

		MA ema = new MA(6, MAType.Ema);
		ema.evaluate(values);

		MA kama = new MA(6, MAType.Kama);
		kama.evaluate(values);

		MA wma = new MA(6, MAType.Wma);
		wma.evaluate(values);

		MA t3 = new MA(6, MAType.T3);
		t3.evaluate(values);

		MAMA mama = new MAMA(0.5, 0.15);
		mama.evaluate(values);

		MACD macd = new MACD(6, 14, 6);
		macd.evaluate(values, MAType.Ema);

		MACD macdTr = new MACD(6, 14, 6);
		macdTr.evaluate(values, MAType.Trima);

		MACD macdSma = new MACD(6, 14, 6);
		macdSma.evaluate(values, MAType.Sma);

		MACD macdT3 = new MACD(6, 14, 6);
		macdT3.evaluate(values, MAType.T3);

		MACD macdKama = new MACD(6, 14, 6);
		macdKama.evaluate(values, MAType.Kama);

		double[] macdMama = Utils.minusArray(mama.getOutMAMA(), mama.getOutFAMA());

		Stream<Object> res = cl.stream().map(c -> c.getDate());
		
		System.out.println("\n"
				+ 		   "dates  :" + Utils.printArray(res, "%1$10tR"));
		System.out.println();
		System.out.println("inReal :" + Utils.printArray(cl.streamPrice(PriceType.CLOSE).toArray(), "%10.0f"));
		System.out.println("mom    :" + Utils.printArray(mom.getOutReal(), "%10.0f"));
		System.out.println("rsi    :" + Utils.printArray(rsi.getOutReal(), "%10.2f"));
		System.out.println("adx    :" + Utils.printArray(adx.getOutReal(), "%10.2f"));
		System.out.println();
		System.out.println("trima  :" + Utils.printArray(Utils.minusArray(values, trima.getOutReal()) , "%10.0f"));
		System.out.println("ema    :" + Utils.printArray(Utils.minusArray(values, ema.getOutReal()), "%10.0f"));
		System.out.println("kama   :" + Utils.printArray(Utils.minusArray(values, kama.getOutReal()), "%10.0f"));
		System.out.println("wma    :" + Utils.printArray(Utils.minusArray(values, wma.getOutReal()), "%10.0f"));
		System.out.println("t3     :" + Utils.printArray(Utils.minusArray(values, t3.getOutReal()), "%10.0f"));
		System.out.println("mama   :" + Utils.printArray(Utils.minusArray(values, mama.getOutMAMA()), "%10.0f"));
		// System.out.println("fama   :" + Utils.printArray(Utils.minusArray(values, mama.getOutFAMA()), "%10.2f"));
		System.out.println();
		System.out.println("macd h :" + Utils.printArray(macd.getOutMACDHist(), "%10.4f"));
		System.out.println("macdtrh:" + Utils.printArray(macdTr.getOutMACDHist(), "%10.4f"));
		System.out.println("macds h:" + Utils.printArray(macdSma.getOutMACDHist(), "%10.4f"));
		System.out.println("macdkah:" + Utils.printArray(macdKama.getOutMACDHist(), "%10.4f"));
		// System.out.println("macdt3h:" + Utils.printArray(macdT3.getOutMACDHist(), "%10.2f"));
		System.out.println("macdmah:" + Utils.printArray(macdMama, "%10.4f"));
		
		System.exit(0);
	}
	}
