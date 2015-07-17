package ru.eclipsetrader.transaq.core.server.command;

import ru.eclipsetrader.transaq.core.model.BoardType;

public class GetHistoryDataCommand extends BaseCommand {

	String seccode;
	BoardType board;
	Integer periodId;
	int candleCount;
	boolean reset = true;

	@Override
	public String createSubscribeCommand() {
		/*
		 * <command id="gethistorydata">
		 *  <security>
		 *   <board> идентификатор режим торгов </board>
		 *   <seccode> код инструмента </seccode>
		 *  </security>
		 * 	<period>идентификатор периода</period>
		 *  <count>количество свечей</count>
		 *  <reset>true/false</reset>
		 * </command>
		 */

		StringBuilder sb = new StringBuilder();
		sb.append("<command id=\"gethistorydata\">");
		sb.append("<security>");
		sb.append("<board>" + board + "</board>");
		sb.append("<seccode>" + seccode + "</seccode>");
		sb.append("</security>");
		sb.append("<period>" + String.valueOf(periodId) + "</period>");
		sb.append("<count>" + candleCount + "</count>");
		sb.append("<reset>" + String.valueOf(reset) + "</reset>");
		sb.append("</command>");
		return sb.toString();
	}

	public String getSeccode() {
		return seccode;
	}

	public void setSeccode(String seccode) {
		this.seccode = seccode;
	}

	public BoardType getBoard() {
		return board;
	}

	public void setBoard(BoardType board) {
		this.board = board;
	}

	public Integer getPeriodId() {
		return periodId;
	}

	public void setPeriodId(Integer periodId) {
		this.periodId = periodId;
	}

	public int getCandleCount() {
		return candleCount;
	}

	public void setCandleCount(int candleCount) {
		this.candleCount = candleCount;
	}

	public boolean isReset() {
		return reset;
	}

	public void setReset(boolean reset) {
		this.reset = reset;
	}

}
