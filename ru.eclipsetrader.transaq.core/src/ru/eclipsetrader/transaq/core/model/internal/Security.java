package ru.eclipsetrader.transaq.core.model.internal;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import ru.eclipsetrader.transaq.core.data.DefaultJPAListener;
import ru.eclipsetrader.transaq.core.interfaces.ITQSecurity;
import ru.eclipsetrader.transaq.core.model.AssetType;
import ru.eclipsetrader.transaq.core.model.BoardType;
import ru.eclipsetrader.transaq.core.model.MarketType;
import ru.eclipsetrader.transaq.core.model.SecurityType;
import ru.eclipsetrader.transaq.core.model.TQSymbol;
import ru.eclipsetrader.transaq.core.util.Utils;

@Entity
@EntityListeners(DefaultJPAListener.class)
public class Security extends ServerObject implements ITQSecurity {

	@EmbeddedId
	TQSymbol symbol;

	@Enumerated(EnumType.STRING)
	MarketType market;
	
	Integer id;
	boolean active;
	String TZ;
	String shortName;
	@Enumerated(EnumType.STRING)
	SecurityType type;
	
	int decimals;
	double minStep;
	int lotSize;
	double point_cost;

	boolean useCredit;
	boolean byMarket;
	boolean noSplit;
	boolean immorCancel;
	boolean cancelBalance;
	
	public Security() {
		this(null);
	}
	
	public Security(String serverId) {
		super(serverId);
		this.symbol = new TQSymbol();
	}
	
	public TQSymbol getSymbol() {
		return symbol;
	}
	
	public String getKey() {
		return symbol.getKey();
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public String getTZ() {
		return TZ;
	}

	public void setTZ(String tZ) {
		TZ = tZ;
	}

	public String getShortName() {
		return shortName;
	}

	public void setShortName(String shortName) {
		this.shortName = shortName;
	}

	public SecurityType getType() {
		return type;
	}

	public void setType(SecurityType type) {
		this.type = type;
	}

	public boolean isUseCredit() {
		return useCredit;
	}

	public void setUseCredit(boolean useCredit) {
		this.useCredit = useCredit;
	}

	public boolean isByMarket() {
		return byMarket;
	}

	public void setByMarket(boolean byMarket) {
		this.byMarket = byMarket;
	}

	public boolean isNoSplit() {
		return noSplit;
	}

	public void setNoSplit(boolean noSplit) {
		this.noSplit = noSplit;
	}

	public boolean isImmorCancel() {
		return immorCancel;
	}

	public void setImmorCancel(boolean immorCancel) {
		this.immorCancel = immorCancel;
	}

	public boolean isCancelBalance() {
		return cancelBalance;
	}

	public void setCancelBalance(boolean cancelBalance) {
		this.cancelBalance = cancelBalance;
	}

	public String getSeccode() {
		return symbol.getSeccode();
	}

	public void setMarket(MarketType market) {
		this.market = market;
	}

	public int getDecimals() {
		return decimals;
	}

	public void setDecimals(int decimals) {
		this.decimals = decimals;
	}

	public double getMinStep() {
		return minStep;
	}

	public void setMinStep(double minStep) {
		this.minStep = minStep;
	}

	public int getLotSize() {
		return lotSize;
	}

	public void setLotSize(int lotSize) {
		this.lotSize = lotSize;
	}

	public double getPoint_cost() {
		return point_cost;
	}

	public void setPoint_cost(double point_cost) {
		this.point_cost = point_cost;
	}
	
	public String toString() {
		return Utils.toString(this);		
	}

	@Override
	public AssetType getAsset() {
		return null;
	}

	public BoardType getBoard() {
		return symbol.getBoard();
	}

	@Override
	public MarketType getMarket() {
		return market;
	}

	public void setSeccode(String value) {
		symbol.setSeccode(value);
		
	}

	public void setBoard(BoardType value) {
		symbol.setBoard(value);
	}

}
