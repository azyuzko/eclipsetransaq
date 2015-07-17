package ru.eclipsetrader.transaq.core.interfaces;

import ru.eclipsetrader.transaq.core.model.BoardType;

/***
 * Интерфейс для идентификации ценной бумаги
 * @author Zyuzko-AA
 *
 */
public interface ITQSymbol {
	
	public BoardType getBoard();
	public String getSeccode();
	
}
