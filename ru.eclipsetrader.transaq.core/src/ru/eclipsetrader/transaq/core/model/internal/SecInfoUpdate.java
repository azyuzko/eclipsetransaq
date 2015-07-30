package ru.eclipsetrader.transaq.core.model.internal;

public class SecInfoUpdate {

	/*
	 * <sec_info_upd> <secid>������������� ������</secid> <market>���������� ���
	 * �����</market> <seccode> ��� �����������</seccode> <minprice>�����������
	 * ���� (������ FORTS)</minprice > <maxprice>������������ ���� (������
	 * FORTS)</maxprice > <buy_deposit>�� ���������� (�������� FORTS, ���.)
	 * </buy_deposit> <sell_deposit>�� �������� (�������� FORTS, ���.)
	 * </sell_deposit> <bgo_c>�� �������� ������� (������� FORTS, ���.)</bgo_c>
	 * <bgo_nc>�� ���������� ������� (������� FORTS, ���.) </bgo_nc>
	 * <bgo_buy>������� �� ��� ������� ������������ ������� </bgo_buy>
	 * <point_cost>��������� ������ ����</point_cost> </sec_info_upd>
	 */

	Integer secid;
	Integer market;
	String seccode;
	double minprice;
	double maxprice;
	double buy_deposit;
	double sell_deposit;
	double bgo_c;
	double bgo_nc;
	double bgo_buy;
	double point_cost;

	public Integer getSecid() {
		return secid;
	}

	public void setSecid(Integer secid) {
		this.secid = secid;
	}

	public Integer getMarket() {
		return market;
	}

	public void setMarket(Integer market) {
		this.market = market;
	}

	public String getSeccode() {
		return seccode;
	}

	public void setSeccode(String seccode) {
		this.seccode = seccode;
	}

	public double getMinprice() {
		return minprice;
	}

	public void setMinprice(double minprice) {
		this.minprice = minprice;
	}

	public double getMaxprice() {
		return maxprice;
	}

	public void setMaxprice(double maxprice) {
		this.maxprice = maxprice;
	}

	public double getBuy_deposit() {
		return buy_deposit;
	}

	public void setBuy_deposit(double buy_deposit) {
		this.buy_deposit = buy_deposit;
	}

	public double getSell_deposit() {
		return sell_deposit;
	}

	public void setSell_deposit(double sell_deposit) {
		this.sell_deposit = sell_deposit;
	}

	public double getBgo_c() {
		return bgo_c;
	}

	public void setBgo_c(double bgo_c) {
		this.bgo_c = bgo_c;
	}

	public double getBgo_nc() {
		return bgo_nc;
	}

	public void setBgo_nc(double bgo_nc) {
		this.bgo_nc = bgo_nc;
	}

	public double getBgo_buy() {
		return bgo_buy;
	}

	public void setBgo_buy(double bgo_buy) {
		this.bgo_buy = bgo_buy;
	}

	public double getPoint_cost() {
		return point_cost;
	}

	public void setPoint_cost(double point_cost) {
		this.point_cost = point_cost;
	}

}
