package ru.eclipsetrader.transaq.core.model.internal;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.List;

import javax.persistence.SynchronizationType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ru.eclipsetrader.transaq.core.model.BoardType;
import ru.eclipsetrader.transaq.core.model.QuotationStatus;
import ru.eclipsetrader.transaq.core.model.TQSymbol;
import ru.eclipsetrader.transaq.core.model.TradingStatus;
import ru.eclipsetrader.transaq.core.util.Utils;

public class Quotation {
	
	Logger logger;

	BoardType board; //������������� ������ ������ �� ���������
	String seccode; //��� �����������
	double point_cost; //��������� ������ ����
	QuotationStatus status; // ������ ��������� �������� ���������/����������
	int accruedintvalue; // ��� �� ���� ������ � ������� �� ���� ������, ���.
	double open; // ���� ������ ������
	double waprice; //���������������� ����
	int biddepth; // ���-�� ����� �� ������� �� ������ ����
	int biddeptht; // ���������� ����� 
	int numbids; // ������ �� ������� 
	int offerdepth; // ���-�� ����� �� ������� �� ������ ���� 
	int offerdeptht; // ���������� �����������
	double bid; // ������ ��������� �� �������
	double offer; // ������ ��������� �� �������
	int numoffers; //������ �� �������
	int numtrades; // ������
	int voltoday; // ����� ����������� ������ � �����
	int openpositions; // ����� ���������� �������� �������(FORTS)
	int deltapositions; // ���.��������	�������(FORTS) 
	double last; // ���� ��������� ������
	int quantity; // ����� ��������� ������, � �����.
	Date time; // ����� ���������� ��������� ������
	double change; // ��������� ���� ��������� ������ �� ��������� � ���� ��������� ������ ����������� ��������� ��� 
	double priceminusprevwaprice; // ���� ��������� ������ � ������	����������� ���
	double valtoday; // ����� ����������� ������, ���. ��� 
	double yield; // ����������, �� ���� ��������� ������
	double yieldatwaprice; // ���������� �� ���������������� ���� 
	double marketpricetoday; // �������� ���� �� ����������� ������	������������ ��� 
	double highbid; // ���������� ���� ������ � ������� �������� ������  
	double lowoffer; // ���������� ���� ����������� � ������� �������� ������ 
	double high; // ������������ ���� ������
	double low; // ����������� ���� ������
	double closeprice; // ���� ��������
	double closeyield; // ���������� �� ���� ��������
	TradingStatus tradingstatus; // ��������� �������� ������ �� �����������
	double buydeposit; // �� �������/����
	double selldeposit; // �� ������/������
	double volatility; // ������������� 
	double theoreticalprice; // ������������� ����
	
	public Quotation(){

	}
	
	public Quotation(TQSymbol symbol) {
		this.board = symbol.getBoard();
		this.seccode = symbol.getSeccode();
		logger = LogManager.getLogger("Quotation."+symbol.toString());
		logger.debug("Quotation for " + symbol +" created");
	}

	
	public void applyQuotationGap(List<SymbolGapMap> quotationGapList) {
		synchronized (this) {
			
			if (logger.isDebugEnabled()) {
				logger.debug("applyQuotationGap. size = " + quotationGapList.size());
			}
			
			for (SymbolGapMap gapMap : quotationGapList) {
	
				@SuppressWarnings("rawtypes")
				Class quotationclass = getClass();
				
				for (String attr : gapMap.keySet()) {
					Object value = null;
					String stringValue = gapMap.get(attr);
					
					
					if ("point_cost".equals(attr)||
					"open".equals(attr)||
					"waprice".equals(attr)||
					"bid".equals(attr)||
					"offer".equals(attr)||
					"last".equals(attr)||
					"change".equals(attr)||
					"priceminusprevwaprice".equals(attr)||
					"valtoday".equals(attr)||
					"yield".equals(attr)||
					"yieldatwaprice".equals(attr)||
					"marketpricetoday".equals(attr)||
					"highbid".equals(attr)||
					"lowoffer".equals(attr)||
					"high".equals(attr)||
					"low".equals(attr)||
					"closeprice".equals(attr)||
					"closeyield".equals(attr)||
					"buydeposit".equals(attr)||
					"selldeposit".equals(attr)||
					"volatility".equals(attr)||
					"theoreticalprice".equals(attr)) value = Double.valueOf(stringValue);
						
					else if ("accruedintvalue".equals(attr)||
					"biddepth".equals(attr)||
					"numbids".equals(attr)||
					"offerdepth".equals(attr)||
					"offerdeptht".equals(attr)||
					"numoffers".equals(attr)||				
					"numtrades".equals(attr)||				
					"voltoday".equals(attr)||				
					"openpositions".equals(attr)||				
					"deltapositions".equals(attr)||				
					"quantity".equals(attr)) value = Integer.valueOf(stringValue);
						
					else if ("time".equals(attr) && stringValue != null && !stringValue.isEmpty())  {
						if (stringValue.length() > 12) { // ���������� ���� ��� �����
							value = Utils.parseDate(stringValue);
						} else {
							value = Utils.parseTime(stringValue);
						}
					}
						
					else if ("status".equals(attr))	value = QuotationStatus.valueOf(stringValue);
		
					else if ("tradingstatus".equals(attr)) {
						switch (stringValue.toCharArray()[0]) {
						case '0': case '1': case '2': case '3': case '4': stringValue = "_" + stringValue;
						}
						value = TradingStatus.valueOf(stringValue);
					}
					
					else 
						continue;
		
					try {
						Field f = quotationclass.getDeclaredField(attr);
						if (!f.isAccessible()) {
							f.setAccessible(true);
						}
						f.set(this, value);
					} catch (Exception e) {
						e.printStackTrace();
						return;
					}
				}
			}
		}
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
