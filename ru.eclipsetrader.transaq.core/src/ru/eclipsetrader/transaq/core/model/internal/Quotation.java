package ru.eclipsetrader.transaq.core.model.internal;

import java.util.Date;

import ru.eclipsetrader.transaq.core.model.BoardType;
import ru.eclipsetrader.transaq.core.model.QuotationStatus;
import ru.eclipsetrader.transaq.core.model.TradingStatus;
import ru.eclipsetrader.transaq.core.util.Utils;

public class Quotation {

	String secid;
	BoardType board; //Идентификатор режима торгов по умолчанию
	String seccode; //Код инструмента
	double point_cost; //Стоимость пункта цены
	QuotationStatus status; // Статус «торговые операции разрешены/запрещены»
	int accruedintvalue; // НКД на дату торгов в расчете на одну бумагу, руб.
	double open; // Цена первой сделки
	double waprice; //Средневзвешенная цена
	int biddepth; // Кол-во лотов на покупку по лучшей цене
	int biddeptht; // Совокупный спрос 
	int numbids; // Заявок на покупку 
	int offerdepth; // Кол-во лотов на продажу по лучшей цене 
	int offerdeptht; // Совокупное предложение
	double bid; // Лучшая котировка на покупку
	double offer; // Лучшая котировка на продажу
	int numoffers; //Заявок на продажу
	int numtrades; // Сделок
	int voltoday; // Объем совершенных сделок в лотах
	int openpositions; // Общее количество открытых позиций(FORTS)
	int deltapositions; // Изм.открытых	позиций(FORTS) 
	double last; // Цена последней сделки
	int quantity; // Объем последней сделки, в лотах.
	Date time; // Время заключения последней сделки
	double change; // Изменение цены последней сделки по отношению к цене последней сделки предыдущего торгового дня 
	double priceminusprevwaprice; // Цена последней сделки к оценке	предыдущего дня
	double valtoday; // Объем совершенных сделок, млн. руб 
	double yield; // Доходность, по цене последней сделки
	double yieldatwaprice; // Доходность по средневзвешенной цене 
	double marketpricetoday; // Рыночная цена по результатам торгов	сегодняшнего дня 
	double highbid; // Наибольшая цена спроса в течение торговой сессии  
	double lowoffer; // Наименьшая цена предложения в течение торговой сессии 
	double high; // Максимальная цена сделки
	double low; // Минимальная цена сделки
	double closeprice; // Цена закрытия
	double closeyield; // Доходность по цене закрытия
	TradingStatus tradingstatus; // Состояние торговой сессии по инструменту
	double buydeposit; // ГО покупок/покр
	double selldeposit; // ГО продаж/непокр
	double volatility; // Волатильность 
	double theoreticalprice; // Теоретическая цена
	
	public String getKey() {
		return board+"_"+seccode;
	}
	
	public String getSecid() {
		return secid;
	}

	public void setSecid(String secid) {
		this.secid = secid;
	}

	public BoardType getBoard() {
		return board;
	}

	public void setBoard(BoardType board) {
		this.board = board;
	}

	public String getSeccode() {
		return seccode;
	}

	public void setSeccode(String seccode) {
		this.seccode = seccode;
	}

	public double getPoint_cost() {
		return point_cost;
	}

	public void setPoint_cost(double point_cost) {
		this.point_cost = point_cost;
	}

	public QuotationStatus getStatus() {
		return status;
	}

	public void setStatus(QuotationStatus status) {
		this.status = status;
	}

	public int getAccruedintvalue() {
		return accruedintvalue;
	}

	public void setAccruedintvalue(int accruedintvalue) {
		this.accruedintvalue = accruedintvalue;
	}

	public double getOpen() {
		return open;
	}

	public void setOpen(double open) {
		this.open = open;
	}

	public double getWaprice() {
		return waprice;
	}

	public void setWaprice(double waprice) {
		this.waprice = waprice;
	}

	public int getBiddepth() {
		return biddepth;
	}

	public void setBiddepth(int biddepth) {
		this.biddepth = biddepth;
	}

	public int getBiddeptht() {
		return biddeptht;
	}

	public void setBiddeptht(int biddeptht) {
		this.biddeptht = biddeptht;
	}

	public int getNumbids() {
		return numbids;
	}

	public void setNumbids(int numbids) {
		this.numbids = numbids;
	}

	public int getOfferdepth() {
		return offerdepth;
	}

	public void setOfferdepth(int offerdepth) {
		this.offerdepth = offerdepth;
	}

	public int getOfferdeptht() {
		return offerdeptht;
	}

	public void setOfferdeptht(int offerdeptht) {
		this.offerdeptht = offerdeptht;
	}

	public double getBid() {
		return bid;
	}

	public void setBid(double bid) {
		this.bid = bid;
	}

	public double getOffer() {
		return offer;
	}

	public void setOffer(double offer) {
		this.offer = offer;
	}

	public int getNumoffers() {
		return numoffers;
	}

	public void setNumoffers(int numoffers) {
		this.numoffers = numoffers;
	}

	public int getNumtrades() {
		return numtrades;
	}

	public void setNumtrades(int numtrades) {
		this.numtrades = numtrades;
	}

	public int getVoltoday() {
		return voltoday;
	}

	public void setVoltoday(int voltoday) {
		this.voltoday = voltoday;
	}

	public int getOpenpositions() {
		return openpositions;
	}

	public void setOpenpositions(int openpositions) {
		this.openpositions = openpositions;
	}

	public int getDeltapositions() {
		return deltapositions;
	}

	public void setDeltapositions(int deltapositions) {
		this.deltapositions = deltapositions;
	}

	public double getLast() {
		return last;
	}

	public void setLast(double last) {
		this.last = last;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public Date getTime() {
		return time;
	}

	public void setTime(Date time) {
		this.time = time;
	}

	public double getChange() {
		return change;
	}

	public void setChange(double change) {
		this.change = change;
	}

	public double getPriceminusprevwaprice() {
		return priceminusprevwaprice;
	}

	public void setPriceminusprevwaprice(double priceminusprevwaprice) {
		this.priceminusprevwaprice = priceminusprevwaprice;
	}

	public double getValtoday() {
		return valtoday;
	}

	public void setValtoday(double valtoday) {
		this.valtoday = valtoday;
	}

	public double getYield() {
		return yield;
	}

	public void setYield(double yield) {
		this.yield = yield;
	}

	public double getYieldatwaprice() {
		return yieldatwaprice;
	}

	public void setYieldatwaprice(double yieldatwaprice) {
		this.yieldatwaprice = yieldatwaprice;
	}

	public double getMarketpricetoday() {
		return marketpricetoday;
	}

	public void setMarketpricetoday(double marketpricetoday) {
		this.marketpricetoday = marketpricetoday;
	}

	public double getHighbid() {
		return highbid;
	}

	public void setHighbid(double highbid) {
		this.highbid = highbid;
	}

	public double getLowoffer() {
		return lowoffer;
	}

	public void setLowoffer(double lowoffer) {
		this.lowoffer = lowoffer;
	}

	public double getHigh() {
		return high;
	}

	public void setHigh(double high) {
		this.high = high;
	}

	public double getLow() {
		return low;
	}

	public void setLow(double low) {
		this.low = low;
	}

	public double getCloseprice() {
		return closeprice;
	}

	public void setCloseprice(double closeprice) {
		this.closeprice = closeprice;
	}

	public double getCloseyield() {
		return closeyield;
	}

	public void setCloseyield(double closeyield) {
		this.closeyield = closeyield;
	}

	public TradingStatus getTradingstatus() {
		return tradingstatus;
	}

	public void setTradingstatus(TradingStatus tradingstatus) {
		this.tradingstatus = tradingstatus;
	}

	public double getBuydeposit() {
		return buydeposit;
	}

	public void setBuydeposit(double buydeposit) {
		this.buydeposit = buydeposit;
	}

	public double getSelldeposit() {
		return selldeposit;
	}

	public void setSelldeposit(double selldeposit) {
		this.selldeposit = selldeposit;
	}

	public double getVolatility() {
		return volatility;
	}

	public void setVolatility(double volatility) {
		this.volatility = volatility;
	}

	public double getTheoreticalprice() {
		return theoreticalprice;
	}

	public void setTheoreticalprice(double theoreticalprice) {
		this.theoreticalprice = theoreticalprice;
	}
	
	@Override
	public String toString() {
		return Utils.toString(this);
	}
}
