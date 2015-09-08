package ru.eclipsetrader.transaq.core.datastorage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.eclipsetrader.transaq.core.Constants;
import ru.eclipsetrader.transaq.core.data.DataManager;
import ru.eclipsetrader.transaq.core.event.Observer;
import ru.eclipsetrader.transaq.core.model.BoardType;
import ru.eclipsetrader.transaq.core.model.internal.Board;
import ru.eclipsetrader.transaq.core.services.ITQBoardService;

public class TQBoardService implements ITQBoardService, Observer<List<Board>> {
	
	Map<String, Board> objects = new HashMap<>();
	
	static TQBoardService instance;
	public static TQBoardService getInstance() {
		if (instance == null) {
			instance = new TQBoardService();
			instance.load();
		}
		return instance;
	}
	
	private TQBoardService() {
	}

	public void persist() {
		DataManager.mergeList(objects.values());
	}

	public void load() {
		clear();
		putList(DataManager.getList(Board.class));
	}

	@Override
	public void clear() {
		objects.clear();
	}

	@Override
	public void put(Board object) {
		objects.put(object.getId(), object);
	}

	@Override
	public void putList(List<Board> objects) {
		for (Board board : objects) {
			put(board);
		}
	}

	@Override
	public Board get(String id) {
		return objects.get(id);
	}

	@Override
	public List<Board> getAll() {
		return new ArrayList<Board>(objects.values());
	}

	@Override
	public List<BoardType> getBoards() {
		List<BoardType> result = new ArrayList<BoardType>();
		for (String id : objects.keySet()) {
			result.add(BoardType.valueOf(id));
		}
		return result;
	}

	@Override
	public void update(List<Board> boards) {
		putList(boards);
	}
}
