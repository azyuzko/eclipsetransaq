package ru.eclipsetrader.transaq.core.interfaces;

import ru.eclipsetrader.transaq.core.model.BoardType;

/***
 * ��������� ��� ������������� ������ ������
 * @author Zyuzko-AA
 *
 */
public interface ITQSymbol {
	
	public BoardType getBoard();
	public String getSeccode();
	
}
