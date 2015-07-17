package ru.eclipsetrader.transaq.core.services;

import java.util.List;

import ru.eclipsetrader.transaq.core.interfaces.ICustomStorage;
import ru.eclipsetrader.transaq.core.interfaces.IPersistable;
import ru.eclipsetrader.transaq.core.model.BoardType;
import ru.eclipsetrader.transaq.core.model.internal.Board;

public interface ITQBoardService extends ICustomStorage<String, Board>, IPersistable {

	public List<BoardType> getBoards();
	
}
